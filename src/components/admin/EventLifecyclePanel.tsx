import { useState } from "react";
import type { EventSettingsResponse } from "../../api/eventsApi";
import "./EventLifecyclePanel.css";

type LifecycleAction = "open" | "close" | "start" | "complete";
interface Props {
    event: EventSettingsResponse | null;
    onCreate: (value: { name: string; eventDate: string; registrationDeadline: string; totalRounds: number }) => Promise<void>;
    onAction: (action: LifecycleAction) => Promise<void>;
}

const actionFor = (status: string): [LifecycleAction, string] | null => {
    if (status === "DRAFT") return ["open", "Open Registration"];
    if (status === "REGISTRATION_OPEN") return ["close", "Close Registration"];
    if (status === "REGISTRATION_CLOSED") return ["start", "Start Game"];
    if (status === "LIVE") return ["complete", "Complete Game"];
    return null;
};

export default function EventLifecyclePanel({ event, onCreate, onAction }: Props) {
    const [showCreate, setShowCreate] = useState(false);
    const [confirmComplete, setConfirmComplete] = useState(false);
    const [busy, setBusy] = useState(false);
    const [name, setName] = useState("Friends Like These");
    const [date, setDate] = useState(new Date().toISOString().slice(0, 10));
    const [deadline, setDeadline] = useState("");
    const [rounds, setRounds] = useState(5);
    const action = event ? actionFor(event.status) : null;

    const execute = async (next: LifecycleAction) => {
        setBusy(true);
        try { await onAction(next); setConfirmComplete(false); }
        finally { setBusy(false); }
    };

    return <section className="event-lifecycle">
        <div><span>Current Game</span><h2>{event ? event.name : "No current game"}</h2>
            {event && <p>{new Date(event.eventDate).toLocaleDateString("en-ZA")} · <strong>{event.status.replaceAll("_", " ")}</strong></p>}
        </div>
        {!event && <button onClick={() => setShowCreate(true)}>Create Game</button>}
        {action && <button disabled={busy} onClick={() => action[0] === "complete" ? setConfirmComplete(true) : execute(action[0])}>{action[1]}</button>}
        {showCreate && <div className="event-lifecycle__modal" role="dialog" aria-modal="true">
            <form onSubmit={async submission => {
                submission.preventDefault(); setBusy(true);
                try { await onCreate({ name, eventDate: date, registrationDeadline: new Date(deadline).toISOString(), totalRounds: rounds }); setShowCreate(false); }
                finally { setBusy(false); }
            }}>
                <h3>Create Game</h3>
                <label>Game name<input required maxLength={150} value={name} onChange={e => setName(e.target.value)} /></label>
                <label>Game date<input required type="date" value={date} onChange={e => setDate(e.target.value)} /></label>
                <label>Registration deadline<input required type="datetime-local" value={deadline} onChange={e => setDeadline(e.target.value)} /></label>
                <label>Number of rounds<input required type="number" min="1" max="50" value={rounds} onChange={e => setRounds(Number(e.target.value))} /></label>
                <footer><button type="button" onClick={() => setShowCreate(false)}>Cancel</button><button disabled={busy}>Create Game</button></footer>
            </form>
        </div>}
        {confirmComplete && <div className="event-lifecycle__modal" role="alertdialog" aria-modal="true"><div>
            <h3>Complete Game?</h3><p>Completing this game preserves its final results and moves it into Previous Games. Normal scoring and registration changes will no longer be allowed.</p>
            <footer><button disabled={busy} onClick={() => setConfirmComplete(false)}>Cancel</button><button disabled={busy} onClick={() => execute("complete")}>Complete Game</button></footer>
        </div></div>}
    </section>;
}
