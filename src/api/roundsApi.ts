import {request} from "./api";
export type RoundStatus="NOT_STARTED"|"IN_PROGRESS"|"COMPLETED";
export interface GameRound{id:string;eventId:string;roundNumber:number;name:string;status:RoundStatus;startedAt:string|null;completedAt:string|null}
export interface RoundStanding{position:number;teamId:string;teamName:string;roundScore:number;overallScore:number}
export const getCurrentRounds=()=>request<GameRound[]>("/api/events/current/rounds");
export const startRound=(number:number)=>request<GameRound>(`/api/events/current/rounds/${number}/start`,{method:"POST"});
export const endRound=(number:number)=>request<GameRound>(`/api/events/current/rounds/${number}/complete`,{method:"POST"});
export const getCurrentRoundStandings=(number:number)=>request<RoundStanding[]>(`/api/events/current/rounds/${number}/leaderboard`);
export const getEventRounds=(id:string)=>request<GameRound[]>(`/api/events/${id}/rounds`);
export const getEventRoundStandings=(id:string,number:number)=>request<RoundStanding[]>(`/api/events/${id}/rounds/${number}/leaderboard`);
