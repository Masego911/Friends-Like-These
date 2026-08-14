import { useState } from "react";
import type { scoreEvent } from "../../models/scoreEvent";
import "./ScoreHistory.css";

interface ScoreHistoryProps {
    scoreEvents: scoreEvent[];
}

function ScoreHistory({ scoreEvents }: ScoreHistoryProps) {
    const [expanded, setExpanded] = useState(false);

    return (
        <section
            className={
                expanded
                    ? "score-history score-history--expanded"
                    : "score-history"
            }
        >
            <header className="score-history__header">
                <div>
                    <span className="score-history__eyebrow">
                        Activity
                    </span>

                    <h2>Score History</h2>
                </div>

                <button
                    type="button"
                    className="score-history__expand"
                    onClick={() => setExpanded((current) => !current)}
                >
                    {expanded ? "Collapse" : "Expand"}
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

                                <p>{event.reason}</p>

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
        </section>
    );
}

export default ScoreHistory;