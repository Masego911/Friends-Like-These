import { useState } from "react";

interface Props { teamCount: number; live: boolean; onResetScores: () => Promise<void>; onResetEntireGame: () => Promise<void>; }
export default function DangerZone({ teamCount, live, onResetScores, onResetEntireGame }: Props) {
    const [action,setAction] = useState<"round"|"game"|null>(null), [working,setWorking] = useState(false);
    const run = async () => { if (!action) return; setWorking(true); try { await (action === "round" ? onResetScores() : onResetEntireGame()); setAction(null); } finally { setWorking(false); } };
    return <section className="danger-zone"><span>Danger Zone</span><h3>Destructive game operations</h3><p>These controls affect scoring records for the current game. They do not remove teams or registrations.</p><div><button type="button" onClick={() => setAction("round")}>Reset Current Round Scores</button>{live && <button type="button" onClick={() => setAction("game")}>Reset Entire Game Scores</button>}</div>
        {action && <div className="admin-modal-backdrop"><div className="admin-confirm" role="alertdialog" aria-modal="true"><h3>{action === "round" ? "Reset current round scores?" : "Reset entire game scores?"}</h3><p>{action === "round" ? `This will clear current-round scoring for all ${teamCount} teams. Previous rounds remain unchanged.` : `This will clear all scoring progress for all ${teamCount} teams in the current game.`}</p><footer><button disabled={working} onClick={() => setAction(null)}>Cancel</button><button disabled={working} className="button--danger" onClick={run}>{working ? "Resetting…" : action === "round" ? "Reset Round" : "Reset Entire Game"}</button></footer></div></div>}
    </section>;
}
