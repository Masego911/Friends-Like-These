import { useState } from "react";
import type { EventDetail, EventSummary, LeaderboardRow } from "../../api/eventsApi";
import { getEvent, getEventLeaderboard, getEventScoreEvents } from "../../api/eventsApi";
import { getEventRounds, getEventRoundStandings, type GameRound, type RoundStanding } from "../../api/roundsApi";
import "./PreviousGames.css";

interface Props {
    events: EventSummary[];
    onArchive: (id: string) => Promise<void>;
}

export default function PreviousGames({ events, onArchive }: Props) {
    const [detail, setDetail] = useState<EventDetail | null>(null);
    const [board, setBoard] = useState<LeaderboardRow[]>([]);
    const [activity, setActivity] = useState<any[]>([]);
    const [rounds, setRounds] = useState<GameRound[]>([]);
    const [roundBoards, setRoundBoards] = useState<Record<number, RoundStanding[]>>({});
    const historical = events.filter(event => event.status === "COMPLETED" || event.status === "ARCHIVED");

    const view = async (id: string) => {
        const [eventDetail, leaderboard, scoreActivity, eventRounds] = await Promise.all([
            getEvent(id), getEventLeaderboard(id), getEventScoreEvents(id), getEventRounds(id),
        ]);
        setDetail(eventDetail);
        setBoard(leaderboard);
        setActivity(scoreActivity);
        setRounds(eventRounds);
        const pairs = await Promise.all(eventRounds.map(async round => [
            round.roundNumber, await getEventRoundStandings(id, round.roundNumber),
        ] as const));
        setRoundBoards(Object.fromEntries(pairs));
    };

    if (detail) return <section className="previous-games">
        <button onClick={() => setDetail(null)}>← Previous Games</button>
        <header><span>{detail.status}</span><h1>{detail.name}</h1><p>{new Date(detail.eventDate).toLocaleDateString("en-ZA")} · {detail.totalRounds} rounds · {detail.teamCount} teams</p></header>
        <h2>Final Overall Results</h2>
        <ol className="previous-games__board">{board.map(row => <li key={row.teamId}><b>#{row.position} {row.teamName}</b><strong>{row.score}</strong></li>)}</ol>
        <h2>Round Results</h2>
        {rounds.map(round => <article key={round.id}><h3>{round.name} · {round.status}</h3><ol>{(roundBoards[round.roundNumber] ?? []).map(row => <li key={row.teamId}>#{row.position} {row.teamName}: {row.roundScore}</li>)}</ol></article>)}
        <h2>Teams and members</h2>
        {detail.teams.map(team => <article key={team.id}><b>{team.name}</b><p>{team.members.join(", ") || "No members recorded"}</p></article>)}
        <h2>Score Activity</h2>
        {activity.length ? activity.map(item => <article key={item.id}><b>{item.teamName}</b><p>{item.reason} · {item.amount > 0 ? "+" : ""}{item.amount} · {item.roundName ?? "Legacy / unassigned"}</p></article>) : <p>No score activity recorded.</p>}
    </section>;

    return <section className="previous-games">
        <header><span>Game History</span><h1>Previous Games</h1><p>Completed results are read-only.</p></header>
        {historical.length === 0 ? <p>No completed games yet.</p> : <div className="previous-games__grid">{historical.map(event => <article key={event.id}>
            <time>{new Date(event.eventDate).toLocaleDateString("en-ZA")}</time><h2>{event.name}</h2><b>{event.status}</b><p>{event.teamCount} teams · {event.totalRounds} rounds</p>
            <button onClick={() => view(event.id)}>View Game</button>
            {event.status === "COMPLETED" && <button onClick={() => onArchive(event.id)}>Archive Game</button>}
        </article>)}</div>}
    </section>;
}
