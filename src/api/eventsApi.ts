import { request } from "./api";
export interface EventSettingsResponse { id: string; name: string; eventDate: string; registrationDeadline: string | null; status: string; totalRounds: number; currentRound: number; current: boolean; }
export interface EventSummary { id:string; name:string; eventDate:string; status:string; totalRounds:number; currentRound:number; teamCount:number; current:boolean; }
export interface HistoricalTeam { id:string; name:string; members:string[]; score:number; }
export interface EventDetail extends EventSettingsResponse { teamCount:number; teams:HistoricalTeam[]; }
export interface LeaderboardRow { position:number; teamId:string; teamName:string; score:number; }
export const getCurrentEvent = () => request<EventSettingsResponse>("/api/events/current");
export const updateRegistrationDeadline = (registrationDeadline: string) => request<EventSettingsResponse>("/api/events/current/settings", { method: "PATCH", body: JSON.stringify({ registrationDeadline }) });
export const updateTotalRounds = (totalRounds: number) => request<EventSettingsResponse>("/api/events/current/settings", { method: "PATCH", body: JSON.stringify({ totalRounds }) });
export const getEvents = () => request<EventSummary[]>("/api/events");
export const createEvent = (body:{name:string;eventDate:string;registrationDeadline:string;totalRounds:number}) => request<EventSettingsResponse>("/api/events",{method:"POST",body:JSON.stringify(body)});
export const openRegistration = () => request<EventSettingsResponse>("/api/events/current/open-registration",{method:"POST"});
export const closeRegistration = () => request<EventSettingsResponse>("/api/events/current/close-registration",{method:"POST"});
export const startGame = () => request<EventSettingsResponse>("/api/events/current/start",{method:"POST"});
export const completeGame = () => request<EventSettingsResponse>("/api/events/current/complete",{method:"POST"});
export const archiveEvent = (id:string) => request<EventSettingsResponse>(`/api/events/${id}/archive`,{method:"POST"});
export const getEvent = (id:string) => request<EventDetail>(`/api/events/${id}`);
export const getEventLeaderboard = (id:string) => request<LeaderboardRow[]>(`/api/events/${id}/leaderboard`);
export const getEventScoreEvents = (id:string) => request<any[]>(`/api/events/${id}/score-events`);
