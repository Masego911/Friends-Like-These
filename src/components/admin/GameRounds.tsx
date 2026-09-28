import { useState } from "react";

import type { GameRound } from "../../api/roundsApi";

import "./GameRounds.css";

interface Props {
    totalRounds: number;
    currentRound: number;
    rounds: GameRound[];
    eventStatus: string;
    onSave?: (numberOfRounds: number) => Promise<void>;
    onStart: (roundNumber: number) => Promise<void>;
    onEnd: (roundNumber: number) => Promise<void>;
    compact?: boolean;
}

export default function GameRounds({
    totalRounds,
    currentRound,
    rounds,
    eventStatus,
    onSave,
    onStart,
    onEnd,
    compact = false,
}: Props) {
    const [value, setValue] = useState(totalRounds);
    const [saving, setSaving] = useState(false);

    const change = (nextValue: number) => {
        setValue(Math.min(50, Math.max(1, nextValue)));
    };

    const activeRound = rounds.find(
        round => round.status === "IN_PROGRESS"
    );
    const completedRounds = rounds.filter(
        round => round.status === "COMPLETED"
    );
    const latestCompletedRound = completedRounds.at(-1);
    const nextRound = rounds.find(
        round => round.status === "NOT_STARTED"
    );
    const displayedRound =
        activeRound?.roundNumber
        ?? latestCompletedRound?.roundNumber
        ?? nextRound?.roundNumber
        ?? currentRound;

    const roundStatus = activeRound
        ? `ROUND ${activeRound.roundNumber} IN PROGRESS`
        : latestCompletedRound
            ? `ROUND ${latestCompletedRound.roundNumber} COMPLETE`
            : `ROUND ${displayedRound} READY TO START`;

    const roundAction = eventStatus === "LIVE" && activeRound
        ? {
            label: `End Round ${activeRound.roundNumber}`,
            run: () => onEnd(activeRound.roundNumber),
        }
        : eventStatus === "LIVE" && nextRound
            ? {
                label: `Start Round ${nextRound.roundNumber}`,
                run: () => onStart(nextRound.roundNumber),
            }
            : null;

    const runRoundAction = async () => {
        if (!roundAction) {
            return;
        }

        setSaving(true);

        try {
            await roundAction.run();
        } finally {
            setSaving(false);
        }
    };

    if (compact) {
        return (
            <section
                className="game-progress-bar"
                aria-labelledby="game-progress-heading"
            >
                <div className="game-progress-bar__heading">
                    <span id="game-progress-heading">
                        Game Progress
                    </span>
                    <strong>
                        Round {displayedRound} of {totalRounds}
                    </strong>
                </div>

                <div className="game-progress-bar__control">
                    <p>{roundStatus}</p>

                    {roundAction && (
                        <button
                            type="button"
                            disabled={saving}
                            onClick={runRoundAction}
                        >
                            {saving ? "Updating…" : roundAction.label}
                        </button>
                    )}
                </div>
            </section>
        );
    }

    return (
        <section className="game-rounds">
            <span>Game Progress</span>
            <h2>
                Round {displayedRound} of {totalRounds}
            </h2>
            <p>
                Current Round: <strong>{roundStatus}</strong>
            </p>
            <p>
                Round progress:{" "}
                <strong>
                    {completedRounds.length} / {totalRounds}
                </strong>
            </p>

            {eventStatus !== "LIVE" && onSave && (
                <>
                    <div className="game-rounds__control">
                        <button
                            type="button"
                            onClick={() => change(value - 1)}
                        >
                            −
                        </button>
                        <input
                            type="number"
                            min="1"
                            max="50"
                            value={value}
                            onChange={event =>
                                change(Number(event.target.value))
                            }
                        />
                        <button
                            type="button"
                            onClick={() => change(value + 1)}
                        >
                            +
                        </button>
                    </div>
                    <button
                        className="game-rounds__save"
                        disabled={saving || value === totalRounds}
                        onClick={async () => {
                            setSaving(true);

                            try {
                                await onSave(value);
                            } finally {
                                setSaving(false);
                            }
                        }}
                    >
                        {saving ? "Saving…" : "Save"}
                    </button>
                </>
            )}

            {roundAction && (
                <button
                    className="game-rounds__save"
                    type="button"
                    disabled={saving}
                    onClick={runRoundAction}
                >
                    {saving ? "Updating…" : roundAction.label}
                </button>
            )}
        </section>
    );
}
