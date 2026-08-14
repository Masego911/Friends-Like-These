import "./UndoDeleteToast.css";

interface UndoDeleteToastProps {
    teamName: string;
    onUndo: () => void;
}

function UndoDeleteToast({ teamName, onUndo }: UndoDeleteToastProps) {
    return (
        <aside
            className="undo-delete-toast"
            role="status"
            aria-live="polite"
        >
            <p>
                <strong>{teamName}</strong> was deleted.
            </p>

            <button type="button" onClick={onUndo}>
                Undo
            </button>
        </aside>
    );
}

export default UndoDeleteToast;
