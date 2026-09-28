import type { Team } from "../models/Team"; import { request } from "./api";
export const getTeams = () => request<Team[]>("/api/teams");
export const createTeam = (name: string, members: string[]) => request<Team>("/api/teams", { method: "POST", body: JSON.stringify({ name, members }) });
export const updateTeam = (id: string, name: string, members: string[]) => request<Team>(`/api/teams/${id}`, { method: "PUT", body: JSON.stringify({ name, members }) });
export const deleteTeam = (id: string) => request<void>(`/api/teams/${id}`, { method: "DELETE" });
export const restoreTeam = (id: string) => request<Team>(`/api/teams/${id}/restore`, { method: "POST" });
export const getLeaderboard = () => request<Array<{ teamId: string; teamName: string; score: number }>>("/api/teams/leaderboard")
    .then(rows => rows.map(row => ({ id: row.teamId, name: row.teamName, score: row.score, members: [] })));
