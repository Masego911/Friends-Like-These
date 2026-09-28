package com.friendslikethese.backend.team;

import com.friendslikethese.backend.event.Event;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "teams",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_team_event_name",
                columnNames = {"event_id", "name"}
        )
)
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "team_id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "score", nullable = false)
    private int score;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @OneToMany(
            mappedBy = "team",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<TeamMember> members = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Team() {
    }

    public Team(Event event, String name) {
        this.event = event;
        this.name = name;
        this.score = 0;
        this.deleted = false;
    }

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public Event getEvent() {
        return event;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public List<TeamMember> getMembers() {
        return List.copyOf(members);
    }

    public void rename(String name) {
        this.name = name;
    }

    public void replaceMembers(List<String> memberNames) {
        members.clear();

        memberNames.stream()
                .map(String::trim)
                .filter(member -> !member.isBlank())
                .forEach(member -> members.add(new TeamMember(this, member)));
    }

    public void applyScore(int newScore) {
        if (newScore < 0) {
            throw new IllegalArgumentException("A team score cannot be negative.");
        }

        score = newScore;
    }

    public void softDelete() {
        deleted = true;
    }

    public void restore() {
        deleted = false;
    }
}
