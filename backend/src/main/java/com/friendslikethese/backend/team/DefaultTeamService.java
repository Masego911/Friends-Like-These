package com.friendslikethese.backend.team;

import com.friendslikethese.backend.common.BusinessRuleException;
import com.friendslikethese.backend.common.ResourceNotFoundException;
import com.friendslikethese.backend.event.Event;
import com.friendslikethese.backend.event.EventRepository;
import com.friendslikethese.backend.event.CurrentEventProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

@Service
public class DefaultTeamService implements TeamService {

    private final TeamRepository teamRepository;
    private final CurrentEventProvider currentEvents;

    public DefaultTeamService(TeamRepository teamRepository, CurrentEventProvider currentEvents) {
        this.teamRepository = teamRepository;
        this.currentEvents = currentEvents;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamResponse> getTeams() {
        Event event = findCurrentEvent();
        return teamRepository.findByEventIdAndDeletedFalseOrderByScoreDescNameAsc(event.getId())
                .stream()
                .map(TeamResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TeamResponse getTeam(UUID teamId) {
        Team team = findActiveTeam(teamId);
        if (!team.getEvent().getId().equals(findCurrentEvent().getId())) throw new ResourceNotFoundException("Team not found.");
        return TeamResponse.from(team);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaderboardEntryResponse> getLeaderboard() {
        Event event = findCurrentEvent();
        var ordered = teamRepository.findByEventIdAndDeletedFalseOrderByScoreDescNameAsc(event.getId());
        var result = new ArrayList<LeaderboardEntryResponse>();
        Integer priorScore = null; int position = 0;
        for (int index = 0; index < ordered.size(); index++) {
            Team team = ordered.get(index);
            if (priorScore == null || team.getScore() != priorScore) position = index + 1;
            result.add(new LeaderboardEntryResponse(position, team.getId(), team.getName(), team.getScore()));
            priorScore = team.getScore();
        }
        return result;
    }

    @Override
    @Transactional
    public TeamResponse createTeam(CreateTeamRequest request) {
        Event event = currentEvents.requireMutable();
        String teamName = request.name().trim();

        if (teamRepository.existsByEventIdAndNameIgnoreCaseAndDeletedFalse(event.getId(), teamName)) {
            throw new BusinessRuleException("A team with this name already exists.");
        }

        Team team = new Team(event, teamName);
        team.replaceMembers(request.members());
        return TeamResponse.from(teamRepository.save(team));
    }

    @Override
    @Transactional
    public TeamResponse updateTeam(UUID teamId, UpdateTeamRequest request) {
        Team team = findActiveTeam(teamId);
        requireCurrentTeam(team);
        String teamName = request.name().trim();

        if (teamRepository.existsByEventIdAndNameIgnoreCaseAndIdNotAndDeletedFalse(
                team.getEvent().getId(), teamName, teamId)) {
            throw new BusinessRuleException("A team with this name already exists.");
        }

        team.rename(teamName);
        team.replaceMembers(request.members());
        return TeamResponse.from(teamRepository.save(team));
    }

    @Override
    @Transactional
    public void deleteTeam(UUID teamId) {
        Team team = findActiveTeam(teamId);
        requireCurrentTeam(team);
        team.softDelete();
        teamRepository.save(team);
    }

    @Override
    @Transactional
    public TeamResponse restoreTeam(UUID teamId) {
        Event current = currentEvents.requireMutable();
        Team team = teamRepository.findByIdAndDeletedTrue(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Deleted team not found."));

        if (!team.getEvent().getId().equals(current.getId())) throw new BusinessRuleException("Historical teams are read-only.");
        if (teamRepository.existsByEventIdAndNameIgnoreCaseAndDeletedFalse(
                team.getEvent().getId(), team.getName())) {
            throw new BusinessRuleException(
                    "This team cannot be restored because another active team uses the same name."
            );
        }

        team.restore();
        return TeamResponse.from(teamRepository.save(team));
    }

    @Override
    @Transactional
    public TeamImportOutcome importRegistration(String name, List<String> members) {
        Event event = currentEvents.requireRegistrationOpen();
        String teamName = name.trim().replaceAll("\\s+", " ");
        List<String> cleaned = members.stream().map(String::trim).map(member -> member.replaceAll("\\s+", " "))
                .filter(member -> !member.isBlank()).toList();

        // Lookup must happen before constructing or saving a new entity. This avoids an
        // invalid pending INSERT being auto-flushed by a later query.
        Team team = teamRepository.findByEventIdAndNameIgnoreCase(event.getId(), teamName)
                .orElse(null);
        if (team == null) {
            team = new Team(event, teamName);
            team.replaceMembers(cleaned);
            teamRepository.save(team);
            return TeamImportOutcome.CREATED;
        }
        boolean changed = team.isDeleted() || !canonicalMembers(team.getMembers().stream().map(TeamMember::getFullName).toList())
                .equals(canonicalMembers(cleaned));
        if (changed) {
            if (team.isDeleted()) team.restore();
            team.replaceMembers(cleaned);
            teamRepository.save(team);
            return TeamImportOutcome.UPDATED;
        }
        return TeamImportOutcome.UNCHANGED;
    }

    private List<String> canonicalMembers(List<String> members) {
        return members.stream().map(String::trim).sorted().toList();
    }

    private Team findActiveTeam(UUID teamId) {
        return teamRepository.findByIdAndDeletedFalse(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found."));
    }

    private void requireCurrentTeam(Team team) {
        Event current = currentEvents.requireMutable();
        if (!team.getEvent().getId().equals(current.getId())) throw new BusinessRuleException("Historical teams are read-only.");
    }

    private Event findCurrentEvent() {
        return currentEvents.requireCurrent();
    }
}
