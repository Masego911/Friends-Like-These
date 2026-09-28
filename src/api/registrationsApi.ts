import { request } from "./api";
export interface RegistrationStatus { automaticSyncEnabled: boolean; syncIntervalSeconds: number; lastSyncAt: string | null; lastSuccessfulSyncAt: string | null; nextExpectedSyncAt: string | null; lastSyncSuccessful: boolean; lastSyncMessage: string; rowsRead: number; teamsCreated: number; teamsUpdated: number; teamsUnchanged: number; rowsSkipped: number; conflicts: number; registrationOpen: boolean; registrationDeadline: string | null; registrationUrl: string; responseSheetUrl: string | null; googleIntegrationAvailable: boolean; }
export interface RegistrationImportResult { rowsRead: number; teamsCreated: number; teamsUpdated: number; teamsUnchanged: number; rowsSkipped: number; conflicts: number; }
export interface RegistrationPreview { teamName: string; members: string[]; status: string; }
export interface RegistrationPreviewResponse { rowsRead: number; validRegistrations: number; newTeams: number; existingTeams: number; conflicts: number; skippedRows: number; registrations: RegistrationPreview[]; }
export const getRegistrationStatus = () => request<RegistrationStatus>("/api/registrations/status");
export const importRegistrations = () => request<RegistrationImportResult>("/api/registrations/import", { method: "POST" });
export const previewRegistrations = () => request<RegistrationPreviewResponse>("/api/registrations/preview");
