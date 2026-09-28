import type { Team } from "../models/Team"; import type { scoreEvent } from "../models/scoreEvent"; import { request } from "./api";
export const adjustScore = (id: string, amount: number, reason: string) => request<Team>(`/api/teams/${id}/score`, { method: "POST", body: JSON.stringify({ amount, reason }) });
export const getScoreHistory = () => request<any[]>("/api/score-events").then(rows => rows.map(row => ({ ...row, timestamp: new Date(row.timestamp) })) as scoreEvent[]);
export const resetScores = () => request<void>("/api/teams/scores/reset", { method: "POST" });
export const resetScoreActivity = () => request<{scoreEventsRemoved:number}>("/api/score-events/current", { method: "DELETE" });
export const resetEntireGame = () => request<{scoreEventsRemoved:number}>("/api/score-events/current-game", { method: "DELETE" });
