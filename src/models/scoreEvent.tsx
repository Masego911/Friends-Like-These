export interface scoreEvent {
    id: string;
    teamId: string;
    teamName: string; // Preserves the team name at the time of the score change.
    amount: number;
    reason: string;
    timestamp: Date;
}