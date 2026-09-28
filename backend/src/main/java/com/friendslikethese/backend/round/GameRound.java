package com.friendslikethese.backend.round;

import com.friendslikethese.backend.event.Event;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name="rounds", uniqueConstraints=@UniqueConstraint(name="uq_round_event_number", columnNames={"event_id","round_number"}),
       indexes=@Index(name="ix_round_event_status", columnList="event_id,status"))
public class GameRound {
 @Id @GeneratedValue(strategy=GenerationType.UUID) @Column(name="round_id",nullable=false,updatable=false) private UUID id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="event_id",nullable=false) private Event event;
 @Column(name="round_number",nullable=false) private int roundNumber;
 @Column(name="name",nullable=false,length=100) private String name;
 @Enumerated(EnumType.STRING) @Column(name="status",nullable=false,length=20) private RoundStatus status=RoundStatus.NOT_STARTED;
 @Column(name="started_at") private OffsetDateTime startedAt;
 @Column(name="completed_at") private OffsetDateTime completedAt;
 @Column(name="created_at",nullable=false) private OffsetDateTime createdAt;
 @Column(name="updated_at",nullable=false) private OffsetDateTime updatedAt;
 protected GameRound(){}
 public GameRound(Event event,int number){this.event=event;this.roundNumber=number;this.name="Round "+number;}
 @PrePersist void create(){createdAt=updatedAt=OffsetDateTime.now();}
 @PreUpdate void update(){updatedAt=OffsetDateTime.now();}
 public UUID getId(){return id;} public Event getEvent(){return event;} public int getRoundNumber(){return roundNumber;}
 public String getName(){return name;} public RoundStatus getStatus(){return status;} public OffsetDateTime getStartedAt(){return startedAt;}
 public OffsetDateTime getCompletedAt(){return completedAt;}
 public void start(){status=RoundStatus.IN_PROGRESS;startedAt=OffsetDateTime.now();completedAt=null;}
 public void complete(){status=RoundStatus.COMPLETED;completedAt=OffsetDateTime.now();}
}
