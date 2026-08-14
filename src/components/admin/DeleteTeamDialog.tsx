import { useId } from "react";
import type { KeyboardEvent } from "react";
import type { Team } from "../../models/Team";
import "./DeleteTeamDialog.css";

interface DeleteTeamDialogProps {
    team: Team;
    onCancel: () => void;
    onConfirm: () => void;
}

function DeleteTeamDialog({
    team,
    onCancel,
    onConfirm,
}: DeleteTeamDialogProps) {
    const headingId = useId();
    const descriptionId = useId();

    function handleKeyDown(event: KeyboardEvent<HTMLDivElement>) {
        if (event.key === "Escape") {
            event.preventDefault();
            onCancel();
        }
    }

    return (
        <div className="delete-team-dialog__backdrop">
            <div
                className="delete-team-dialog"
                role="dialog"
                aria-modal="true"
                aria-labelledby={headingId}
                aria-describedby={descriptionId}
                onKeyDown={handleKeyDown}
            >
                <span className="delete-team-dialog__eyebrow">
                    Confirm deletion
                </span>

                <h2 id={headingId}>Delete &ldquo;{team.name}&rdquo;?</h2>

                <p id={descriptionId}>
                    This team will be removed from the scoreboard and admin list.
                </p>

                <div className="delete-team-dialog__actions">
                    <button
                        className="delete-team-dialog__cancel"
                        type="button"
                        onClick={onCancel}
                        autoFocus
                    >
                        Cancel
                    </button>

                    <button
                        className="delete-team-dialog__confirm"
                        type="button"
                        onClick={onConfirm}
                    >
                        Delete Team
                    </button>
                </div>
            </div>
        </div>
    );
}

export default DeleteTeamDialog;
