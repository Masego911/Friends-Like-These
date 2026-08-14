import { useState } from "react";
import type { Team } from "../../models/Team";
import "./ScoreAdjustmentForm.css";

interface ScoreAdjustmentFormProps {
    team: Team;
    onSubmit: (teamId: string, amount: number, reason: string) => void;
    onCancel: () => void;
}

function ScoreAdjustmentForm({
                                 team,
                                 onSubmit,
                                 onCancel,
                             }: ScoreAdjustmentFormProps) {
    const [amount, setAmount] = useState("");
    const [reason, setReason] = useState("");
    const [error, setError] = useState("");

    function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
        event.preventDefault();

        const parsedAmount = Number(amount);
        const cleanedReason = reason.trim();

        if (!Number.isFinite(parsedAmount) || parsedAmount === 0) {
            setError("Enter a valid score adjustment.");
            return;
        }

        if (!cleanedReason) {
            setError("Enter a reason for the score adjustment.");
            return;
        }

        setError("");
        onSubmit(team.id, parsedAmount, cleanedReason);
    }

    return (
        <form
            className="score-adjustment-form"
            onSubmit={handleSubmit}
        >
            <header>
                <span className="score-adjustment-form__eyebrow">
                    Score Adjustment
                </span>

                <h2>{team.name}</h2>

                <p>Current score: {team.score} pts</p>
            </header>

            {error && (
                <p
                    className="score-adjustment-form__error"
                    role="alert"
                >
                    {error}
                </p>
            )}

            <div className="score-adjustment-form__field">
                <label htmlFor="score-amount">
                    Points
                </label>

                <input
                    id="score-amount"
                    type="number"
                    value={amount}
                    onChange={(event) => setAmount(event.target.value)}
                    placeholder="e.g. 15 or -10"
                />
            </div>

            <div className="score-adjustment-form__field">
                <label htmlFor="score-reason">
                    Reason
                </label>

                <input
                    id="score-reason"
                    type="text"
                    value={reason}
                    onChange={(event) => setReason(event.target.value)}
                    placeholder="e.g. Won challenge 3"
                />
            </div>

            <div className="score-adjustment-form__actions">
                <button
                    type="button"
                    className="score-adjustment-form__cancel"
                    onClick={onCancel}
                >
                    Cancel
                </button>

                <button
                    type="submit"
                    className="score-adjustment-form__save"
                >
                    Apply Score
                </button>
            </div>
        </form>
    );
}

export default ScoreAdjustmentForm;