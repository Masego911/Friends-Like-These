import { useState } from "react";
import type { scoreEvent } from "../../models/scoreEvent";
import "./ScoreHistory.css";

interface ScoreHistoryProps {
    scoreEvents: scoreEvent[];
    onResetActivity: () => Promise<void>;
}

function ScoreHistory({ scoreEvents, onResetActivity }: ScoreHistoryProps) {
    const [expanded, setExpanded] = useState(false);
    const [confirmingReset, setConfirmingReset] = useState(false);
    const [resetting, setResetting] = useState(false);

    return (
        <section
            className={
                expanded
                    ? "score-history score-history--expanded"
                    : scoreEvents.length === 0 ? "score-history score-history--empty" : "score-history"
            }
        >
            <header className="score-history__header">
                <div>
                    <span className="score-history__eyebrow">
                        Activity
                    </span>

                    <h2>All Game Activity</h2>
                </div>

                <button
                    type="button"
                    className="score-history__expand"
                    onClick={() => setExpanded((current) => !current)}
                >
                    {expanded ? "Collapse" : "Expand"}
                </button>
                <button type="button" className="score-history__reset" onClick={() => setConfirmingReset(true)} disabled={resetting}>
                    Reset Current Round Activity
                </button>
            </header>

            {scoreEvents.length === 0 ? (
                <p className="score-history__empty">
                    No score changes yet.
                </p>
            ) : (
                <div className="score-history__list">
                    {scoreEvents.map((event) => (
                        <article
                            className="score-history__item"
                            key={event.id}
                        >
                            <div className="score-history__details">
                                <h3>{event.teamName}</h3>

                                <p>{event.reason}</p><p>{event.roundName ?? "Legacy / unassigned"}</p>

                                <time dateTime={event.timestamp.toISOString()}>
                                    {event.timestamp.toLocaleString(
                                        "en-ZA",
                                        {
                                            day: "2-digit",
                                            month: "short",
                                            hour: "2-digit",
                                            minute: "2-digit",
                                        },
                                    )}
                                </time>
                            </div>

                            <strong
                                className={
                                    event.amount > 0
                                        ? "score-history__amount score-history__amount--positive"
                                        : "score-history__amount score-history__amount--negative"
                                }
                            >
                                {event.amount > 0 ? "+" : ""}
                                {event.amount}
                            </strong>
                        </article>
                    ))}
                </div>
            )}
            {confirmingReset && <div className="score-history__confirm" role="alertdialog" aria-modal="true" aria-labelledby="reset-activity-title"><div><h3 id="reset-activity-title">Reset Current Round Activity?</h3><p>This clears activity and scoring for the active round only. Previous rounds remain unchanged.</p><footer><button type="button" onClick={() => setConfirmingReset(false)} disabled={resetting}>Cancel</button><button type="button" className="score-history__confirm-button" disabled={resetting} onClick={async()=>{setResetting(true);try{await onResetActivity();setConfirmingReset(false);}finally{setResetting(false);}}}>{resetting?"Resetting…":"Reset Round Activity"}</button></footer></div></div>}
        </section>
    );
}

export default ScoreHistory;
