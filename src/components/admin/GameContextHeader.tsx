import type { EventSettingsResponse } from "../../api/eventsApi";

interface Props { event: EventSettingsResponse | null; teamCount: number; onMenu: () => void; }
const label = (status?: string) => status ? status.replaceAll("_", " ").toLowerCase().replace(/\b\w/g, c => c.toUpperCase()) : "No current game";

export default function GameContextHeader({ event, teamCount, onMenu }: Props) {
    return <header className="game-context">
        <button type="button" className="game-context__menu" onClick={onMenu} aria-label="Open administrator navigation"><span /><span /><span /></button>
        <strong className="game-context__mobile-brand">Friends Like These</strong>
        <div><span>Current Game</span><h1>{event?.name ?? "No current game"}</h1></div>
        <dl>
            <div><dt>Status</dt><dd className={`game-context__status game-context__status--${event?.status.toLowerCase() ?? "none"}`}>{label(event?.status)}</dd></div>
            {event && <div><dt>Round</dt><dd>{event.currentRound} of {event.totalRounds}</dd></div>}
            <div><dt>Teams</dt><dd>{teamCount}</dd></div>
        </dl>
    </header>;
}
