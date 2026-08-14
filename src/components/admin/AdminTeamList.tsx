import type { Team } from "../../models/Team";
import "./AdminTeamList.css";

interface AdminTeamListProps {
    teams: Team[];
    onRequestDeleteTeam: (team: Team) => void;
    onAddTeamRequest: () => void;
    onResetAllScoresRequest: () => void;
    onEditTeamRequest: (team: Team) => void;
    onScoreChange: (
        teamId: string,
        amount: number,
        reason?: string,
    ) => void;
    onCustomScoreRequest: (team: Team) => void;
}

function AdminTeamList({
                           teams,
                           onRequestDeleteTeam,
                           onAddTeamRequest,
                           onResetAllScoresRequest,
                           onEditTeamRequest,
                           onScoreChange,
                           onCustomScoreRequest,
                       }: AdminTeamListProps) {
    return (
        <section className="admin-team-list">
            <div className="admin-team-list__header">
                <div>
                    <span className="admin-team-list__eyebrow">
                        Teams
                    </span>

                    <h2>Manage Teams</h2>
                </div>

                <div className="admin-team-list__header-actions">
                    <button
                        className="admin-team-list__reset-button"
                        type="button"
                        onClick={onResetAllScoresRequest}
                    >
                        Reset All Scores
                    </button>

                    <button
                        className="admin-team-list__add-button"
                        type="button"
                        onClick={onAddTeamRequest}
                    >
                        Add Team
                    </button>
                </div>
            </div>

            <div className="admin-team-list__items">
                {teams.map((team) => (
                    <article
                        className="admin-team-list__row"
                        key={team.id}
                    >
                        <div className="admin-team-list__info">
                            <h3>{team.name}</h3>

                            <p>
                                {team.members.length} members
                            </p>
                        </div>

                        <div className="admin-team-list__score">
                            {team.score} pts
                        </div>

                        <div className="admin-team-list__score-controls">
                            <button
                                className="score-button score-button--5"
                                type="button"
                                onClick={() =>
                                    onScoreChange(
                                        team.id,
                                        5,
                                        "5 points awarded",
                                    )
                                }
                            >
                                +5
                            </button>

                            <button
                                className="score-button score-button--10"
                                type="button"
                                onClick={() =>
                                    onScoreChange(
                                        team.id,
                                        10,
                                        "10 points awarded",
                                    )
                                }
                            >
                                +10
                            </button>

                            <button
                                className="score-button score-button--15"
                                type="button"
                                onClick={() =>
                                    onScoreChange(
                                        team.id,
                                        15,
                                        "15 points awarded",
                                    )
                                }
                            >
                                +15
                            </button>

                            <button
                                className="score-button score-button--20"
                                type="button"
                                onClick={() =>
                                    onScoreChange(
                                        team.id,
                                        20,
                                        "20 points awarded",
                                    )
                                }
                            >
                                +20
                            </button>

                            <button
                                className="score-button score-button--deduct"
                                type="button"
                                onClick={() =>
                                    onScoreChange(
                                        team.id,
                                        -5,
                                        "5 points deducted",
                                    )
                                }
                            >
                                -5
                            </button>

                            <button
                                className="score-button score-button--custom"
                                type="button"
                                onClick={() =>
                                    onCustomScoreRequest(team)
                                }
                            >
                                Custom
                            </button>
                        </div>

                        <div className="admin-team-list__actions">
                            <button
                                type="button"
                                onClick={() =>
                                    onEditTeamRequest(team)
                                }
                            >
                                Edit
                            </button>

                            <button
                                className="admin-team-list__delete"
                                type="button"
                                aria-label={`Delete ${team.name}`}
                                onClick={() =>
                                    onRequestDeleteTeam(team)
                                }
                            >
                                Delete
                            </button>
                        </div>
                    </article>
                ))}
            </div>
        </section>
    );
}

export default AdminTeamList;