import { useState } from "react";

import type { EventSettingsResponse } from "../../api/eventsApi";
import type { GameRound } from "../../api/roundsApi";

import "./EventLifecyclePanel.css";

interface Props {
    event: EventSettingsResponse;
    rounds: GameRound[];
    onComplete: () => Promise<void>;
    className?: string;
}

export default function CompleteGameControl({ event, rounds, onComplete, className }: Props) {
    const [confirming, setConfirming] = useState(false);
    const [busy, setBusy] = useState(false);
    const activeRound = rounds.find(round => round.status === "IN_PROGRESS");
    const completedRounds = rounds.filter(round => round.status === "COMPLETED").length;

    const complete = async () => {
        setBusy(true);

        try {
            await onComplete();
            setConfirming(false);
        } finally {
            setBusy(false);
        }
    };

    return <>
        <button
            type="button"
            className={className}
            disabled={busy || Boolean(activeRound)}
            title={activeRound ? `End Round ${activeRound.roundNumber} before completing the game.` : undefined}
            onClick={() => setConfirming(true)}
        >
            Complete Game
        </button>

        {confirming && <div className="event-lifecycle__modal" role="alertdialog" aria-modal="true" aria-labelledby="complete-game-title"><div>
            <h3 id="complete-game-title">Complete Game?</h3>
            <p>Completing this game preserves its final results and moves it into Previous Games. Normal scoring and registration changes will no longer be allowed.</p>
            {completedRounds < event.totalRounds && <p><strong>{completedRounds} of {event.totalRounds} configured rounds have been completed.</strong> You can still finish the event early.</p>}
            <footer>
                <button type="button" disabled={busy} onClick={() => setConfirming(false)}>Cancel</button>
                <button type="button" disabled={busy} onClick={complete}>{busy ? "Completing…" : "Complete Game"}</button>
            </footer>
        </div></div>}
    </>;
}
