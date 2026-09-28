package com.friendslikethese.backend.team;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "team_members")
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "team_member_id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    protected TeamMember() {
    }

    TeamMember(Team team, String fullName) {
        this.team = team;
        this.fullName = fullName;
    }

    public UUID getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }
}
