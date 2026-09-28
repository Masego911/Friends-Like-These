import { useMemo, useState } from "react";

import type { EventSettingsResponse } from "../../api/eventsApi";
import type { GameRound } from "../../api/roundsApi";
import type { scoreEvent } from "../../models/scoreEvent";
import type { Team } from "../../models/Team";

import ScoreHistory from "./ScoreHistory";
import TeamScoringRow from "./TeamScoringRow";
import GameRounds from "./GameRounds";

interface Props {
    event: EventSettingsResponse | null;
    rounds: GameRound[];
    teams: Team[];
    activity: scoreEvent[];
    onScoreChange: (
        teamId: string,
        amount: number,
        reason?: string
    ) => Promise<void>;
    onResetActivity: () => Promise<void>;
    onStartRound: (roundNumber: number) => Promise<void>;
    onEndRound: (roundNumber: number) => Promise<void>;
}

function sortTeamsByScore(teams: Team[]): Team[] {
    return [...teams].sort(
        (first, second) =>
            second.score - first.score ||
            first.name.localeCompare(second.name)
    );
}

function calculateCompetitionPositions(
    teams: Team[]
): Map<string, number> {
    const positions = new Map<string, number>();

    let previousScore: number | null = null;
    let previousPosition = 0;

    teams.forEach((team, index) => {
        const position =
            previousScore === team.score
                ? previousPosition
                : index + 1;

        positions.set(team.id, position);

        previousScore = team.score;
        previousPosition = position;
    });

    return positions;
}

function isScoringEnabled(
    event: EventSettingsResponse | null,
    rounds: GameRound[]
): boolean {
    if (event?.status !== "LIVE") {
        return false;
    }

    return rounds.some(
        round => round.status === "IN_PROGRESS"
    );
}

function getGameStatusText(
    event: EventSettingsResponse | null
): string {
    if (!event) {
        return "Game status: unavailable";
    }

    if (event.status === "LIVE") {
        return `Round ${event.currentRound} of ${event.totalRounds}`;
    }

    return `Game status: ${event.status.replaceAll("_", " ")}`;
}

export default function LiveScoringPage({
                                            event,
                                            rounds,
                                            teams,
                                            activity,
                                            onScoreChange,
                                            onResetActivity,
                                            onStartRound,
                                            onEndRound,
                                        }: Props) {
    const [pendingTeams, setPendingTeams] = useState<Set<string>>(
        () => new Set()
    );

    const [expandedTeamId, setExpandedTeamId] =
        useState<string | null>(null);

    const [scoringError, setScoringError] =
        useState<string | null>(null);

    const scoringEnabled = isScoringEnabled(event, rounds);

    const rankedTeams = useMemo(
        () => sortTeamsByScore(teams),
        [teams]
    );

    const positions = useMemo(
        () => calculateCompetitionPositions(rankedTeams),
        [rankedTeams]
    );

    const latestActivityByTeam = useMemo(() => {
        const latest = new Map<string, scoreEvent>();

        for (const item of activity) {
            if (!latest.has(item.teamId)) {
                latest.set(item.teamId, item);
            }
        }

        return latest;
    }, [activity]);

    const setTeamPending = (
        teamId: string,
        pending: boolean
    ) => {
        setPendingTeams(current => {
            const next = new Set(current);

            if (pending) {
                next.add(teamId);
            } else {
                next.delete(teamId);
            }

            return next;
        });
    };

    const handleScoreChange = async (
        teamId: string,
        amount: number,
        reason?: string
    ) => {
        if (!scoringEnabled) {
            return;
        }

        if (pendingTeams.has(teamId)) {
            return;
        }

        setTeamPending(teamId, true);
        setScoringError(null);

        try {
            await onScoreChange(
                teamId,
                amount,
                reason
            );
        } catch (error) {
            const message =
                error instanceof Error
                    ? error.message
                    : "Unable to update the score.";

            setScoringError(message);
        } finally {
            setTeamPending(teamId, false);
        }
    };

    const handleToggleTeam = (teamId: string) => {
        setExpandedTeamId(current =>
            current === teamId ? null : teamId
        );
    };

    return (
        <div className="admin-page live-scoring-page">
            <header className="admin-page__heading live-scoring-heading">
                <div>
                    <span>
                        {event?.status === "LIVE" ? (
                            <>
                                <i className="live-scoring-heading__dot" />
                                Live game control
                            </>
                        ) : (
                            "Game control"
                        )}
                    </span>

                    <h2>Live Scoring</h2>

                    <p>{getGameStatusText(event)}</p>
                </div>
            </header>

            {event && (
                <GameRounds
                    compact
                    totalRounds={event.totalRounds}
                    currentRound={event.currentRound}
                    rounds={rounds}
                    eventStatus={event.status}
                    onStart={onStartRound}
                    onEnd={onEndRound}
                />
            )}

            {scoringError && (
                <p
                    className="scoring-error"
                    role="alert"
                >
                    {scoringError}
                </p>
            )}

            {rankedTeams.length === 0 ? (
                <div className="admin-empty">
                    <h3>No teams to score</h3>

                    <p>
                        Teams will appear here after they are
                        added or imported.
                    </p>
                </div>
            ) : (
                <section
                    className="team-scoring-list"
                    aria-label="Team scoring controls"
                >
                    <div
                        className="team-scoring-list__head"
                        aria-hidden="true"
                    >
                        <span>Rank</span>
                        <span>Team</span>
                        <span>Members</span>
                        <span>Score</span>
                        <span>Quick Score</span>
                        <span>Actions</span>
                    </div>

                    {rankedTeams.map(team => (
                        <TeamScoringRow
                            key={team.id}
                            team={team}
                            position={
                                positions.get(team.id) ?? 1
                            }
                            latest={
                                latestActivityByTeam.get(
                                    team.id
                                )
                            }
                            pending={pendingTeams.has(
                                team.id
                            )}
                            scoringEnabled={
                                scoringEnabled
                            }
                            expanded={
                                expandedTeamId === team.id
                            }
                            onToggle={() =>
                                handleToggleTeam(team.id)
                            }
                            onScore={(amount, reason) =>
                                handleScoreChange(
                                    team.id,
                                    amount,
                                    reason
                                )
                            }
                        />
                    ))}
                </section>
            )}

            <ScoreHistory
                scoreEvents={activity}
                onResetActivity={onResetActivity}
            />
        </div>
    );
}
