import { useState } from "react";
import type { Team } from "../../models/Team";
import type { eventSettings } from "../../models/eventSettings";
import type { scoreEvent } from "../../models/scoreEvent";
import AdminEventSettings from "./AdminEventSettings";
import AdminTeamList from "./AdminTeamList";
import AddTeamForm from "./AddTeamForm";
import EditTeamForm from "./EditTeamForm";
import ScoreAdjustmentForm from "./ScoreAdjustmentForm";
import ScoreHistory from "./ScoreHistory";
import "./AdminDashboard.css";

interface AdminDashboardProps {
    settings: eventSettings;
    teams: Team[];
    scoreEvents: scoreEvent[];
    onDeadlineChange: (deadline: Date) => void;
    onRequestDeleteTeam: (team: Team) => void;
    onAddTeam: (team: Team) => void;
    onUpdateTeam: (team: Team) => void;
    onResetAllScores: () => void;
    onScoreChange: (
        teamId: string,
        amount: number,
        reason?: string,
    ) => void;
}

function AdminDashboard({
                            settings,
                            teams,
                            scoreEvents,
                            onDeadlineChange,
                            onRequestDeleteTeam,
                            onAddTeam,
                            onUpdateTeam,
                            onResetAllScores,
                            onScoreChange,
                        }: AdminDashboardProps) {
    const [showAddTeamForm, setShowAddTeamForm] = useState(false);

    const [teamBeingEdited, setTeamBeingEdited] =
        useState<Team | null>(null);

    const [teamForScoreAdjustment, setTeamForScoreAdjustment] =
        useState<Team | null>(null);

    const [showResetConfirmation, setShowResetConfirmation] =
        useState(false);

    function closeAllForms() {
        setShowAddTeamForm(false);
        setTeamBeingEdited(null);
        setTeamForScoreAdjustment(null);
    }

    return (
        <section className="admin-dashboard">
            <header className="admin-dashboard__header">
                <div>
                    <span className="admin-dashboard__eyebrow">
                        Friends Like These
                    </span>

                    <h1>Admin Dashboard</h1>

                    <p>
                        Manage event settings, teams and live scores.
                    </p>
                </div>
            </header>

            <div className="admin-dashboard__overview">
                <AdminEventSettings
                    registrationDeadline={
                        settings.registrationDeadline
                    }
                    onDeadlineChange={onDeadlineChange}
                />

                <ScoreHistory
                    scoreEvents={scoreEvents}
                />
            </div>

            <div className="admin-dashboard__teams">
                <AdminTeamList
                    teams={teams}
                    onRequestDeleteTeam={onRequestDeleteTeam}
                    onAddTeamRequest={() => {
                        closeAllForms();
                        setShowAddTeamForm(true);
                    }}
                    onResetAllScoresRequest={() => {
                        closeAllForms();
                        setShowResetConfirmation(true);
                    }}
                    onEditTeamRequest={(team) => {
                        closeAllForms();
                        setTeamBeingEdited(team);
                    }}
                    onScoreChange={onScoreChange}
                    onCustomScoreRequest={(team) => {
                        closeAllForms();
                        setTeamForScoreAdjustment(team);
                    }}
                />
            </div>

            {showAddTeamForm && (
                <div
                    className="admin-dashboard__modal-backdrop"
                    role="presentation"
                >
                    <div
                        className="admin-dashboard__modal"
                        role="dialog"
                        aria-modal="true"
                        aria-label="Add Team"
                    >
                        <AddTeamForm
                            existingTeams={teams}
                            onAddTeam={(team) => {
                                onAddTeam(team);
                                setShowAddTeamForm(false);
                            }}
                            onCancel={() =>
                                setShowAddTeamForm(false)
                            }
                        />
                    </div>
                </div>
            )}

            {teamBeingEdited && (
                <div
                    className="admin-dashboard__modal-backdrop"
                    role="presentation"
                >
                    <div
                        className="admin-dashboard__modal"
                        role="dialog"
                        aria-modal="true"
                        aria-label={`Edit ${teamBeingEdited.name}`}
                    >
                        <EditTeamForm
                            team={teamBeingEdited}
                            existingTeams={teams}
                            onSave={(updatedTeam) => {
                                onUpdateTeam(updatedTeam);
                                setTeamBeingEdited(null);
                            }}
                            onCancel={() =>
                                setTeamBeingEdited(null)
                            }
                        />
                    </div>
                </div>
            )}

            {teamForScoreAdjustment && (
                <div
                    className="admin-dashboard__modal-backdrop"
                    role="presentation"
                >
                    <div
                        className="admin-dashboard__modal"
                        role="dialog"
                        aria-modal="true"
                        aria-label={`Adjust score for ${teamForScoreAdjustment.name}`}
                    >
                        <ScoreAdjustmentForm
                            team={teamForScoreAdjustment}
                            onSubmit={(teamId, amount, reason) => {
                                onScoreChange(
                                    teamId,
                                    amount,
                                    reason,
                                );

                                setTeamForScoreAdjustment(null);
                            }}
                            onCancel={() =>
                                setTeamForScoreAdjustment(null)
                            }
                        />
                    </div>
                </div>
            )}

            {showResetConfirmation && (
                <div
                    className="admin-dashboard__modal-backdrop"
                    role="presentation"
                >
                    <div
                        className="admin-dashboard__reset-dialog"
                        role="alertdialog"
                        aria-modal="true"
                        aria-labelledby="reset-scores-title"
                    >
                        <h2 id="reset-scores-title">
                            Reset All Scores?
                        </h2>

                        <p>
                            This will set every team's score back to zero.
                        </p>

                        <div className="admin-dashboard__reset-actions">
                            <button
                                type="button"
                                onClick={() =>
                                    setShowResetConfirmation(false)
                                }
                            >
                                Cancel
                            </button>

                            <button
                                className="admin-dashboard__confirm-reset"
                                type="button"
                                onClick={() => {
                                    onResetAllScores();
                                    setShowResetConfirmation(false);
                                }}
                            >
                                Reset Scores
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </section>
    );
}

export default AdminDashboard;