/**
 * Describes the shared shape of a team throughout the frontend.
 * Centralising this domain model lets components and services agree on the
 * same data contract as the application grows.
 */
export interface Team {
  /** Stable identifier used to distinguish the team from every other team. */
  id: string
  /** Public team name shown in rankings and team views. */
  name: string
  /** Display names of the students who belong to the team. */
  members: string[]
  /** Current points total used when ranking teams. */
  score: number
}
