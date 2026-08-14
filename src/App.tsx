import { useEffect, useState } from "react";
import "./styles/App.css";
import type { Team } from "./models/Team";
import type { eventSettings } from "./models/eventSettings";
import AppHeader from "./components/layout/AppHeader";
import Scoreboard from "./components/scoreboard/Scoreboard";
import AdminDashboard from "./components/admin/AdminDashboard";
import DeleteTeamDialog from "./components/admin/DeleteTeamDialog";
import UndoDeleteToast from "./components/admin/UndoDeleteToast";
import type { scoreEvent } from "./models/scoreEvent.tsx";

type AppView = "scoreboard" | "admin";

interface DeletedTeamState {
    team: Team;
    index: number;
}

const UNDO_TIMEOUT_MS = 6000;

function App() {
    const [view, setView] = useState<AppView>("scoreboard");

    const [eventSettings, setEventSettings] = useState<eventSettings>({
        registrationDeadline: new Date("2026-09-30T20:00:00+02:00"),
    });

    const [scoreEvents, setScoreEvents] = useState<scoreEvent[]>([]);

    const [teams, setTeams] = useState<Team[]>([
        {
            id: "team-001",
            name: "The All-Stars",
            members: [
                "Mihle Ngcobo",
                "Timothy Sherwood",
                "Khaya Mpangele",
                "Alex Mapfumo",
                "Anashe Chipamaunga",
                "Bulumko Tilayi",
            ],
            score: 120,
        },
        {
            id: "team-002",
            name: "Code Warriors",
            members: [
                "Aisha Khan",
                "Thando Mbeki",
                "Li Wei",
            ],
            score: 95,
        },
        {
            id: "team-003",
            name: "Debug Demons",
            members: [
                "Priya Naidoo",
                "Lwazi Dlamini",
                "Jordan Daniels",
            ],
            score: 80,
        },
        {
            id: "team-004",
            name: "Dream Team",
            members: [
                "Naledi Mokoena",
                "Jade Williams",
                "Sipho Ndlovu",
            ],
            score: 70,
        },
    ]);

    const [teamPendingDeletion, setTeamPendingDeletion] =
        useState<Team | null>(null);

    const [deletedTeam, setDeletedTeam] =
        useState<DeletedTeamState | null>(null);

    useEffect(() => {
        if (!deletedTeam) {
            return;
        }

        const timeoutId = window.setTimeout(() => {
            setDeletedTeam(null);
        }, UNDO_TIMEOUT_MS);

        return () => window.clearTimeout(timeoutId);
    }, [deletedTeam]);

    function handleAddTeam(team: Team) {
        setTeams((currentTeams) => [...currentTeams, team]);
    }

    function handleRequestDeleteTeam(team: Team) {
        setTeamPendingDeletion(team);
    }

    function handleConfirmDeleteTeam() {
        if (!teamPendingDeletion) {
            return;
        }

        const teamIndex = teams.findIndex(
            (team) => team.id === teamPendingDeletion.id,
        );

        if (teamIndex === -1) {
            setTeamPendingDeletion(null);
            return;
        }

        const teamToDelete = teams[teamIndex];

        setTeams((currentTeams) =>
            currentTeams.filter((team) => team.id !== teamToDelete.id),
        );

        setDeletedTeam({
            team: teamToDelete,
            index: teamIndex,
        });

        setTeamPendingDeletion(null);
    }

    function handleUndoDeleteTeam() {
        if (!deletedTeam) {
            return;
        }

        setTeams((currentTeams) => {
            const teamAlreadyExists = currentTeams.some(
                (team) => team.id === deletedTeam.team.id,
            );

            if (teamAlreadyExists) {
                return currentTeams;
            }

            const restoredTeams = [...currentTeams];

            const insertionIndex = Math.min(
                deletedTeam.index,
                restoredTeams.length,
            );

            restoredTeams.splice(
                insertionIndex,
                0,
                deletedTeam.team,
            );

            return restoredTeams;
        });

        setDeletedTeam(null);
    }

    function handleDeadlineChange(deadline: Date) {
        setEventSettings({
            registrationDeadline: deadline,
        });
    }


    function handleUpdateTeam(updatedTeam: Team) {
        setTeams((currentTeams) =>
            currentTeams.map((team) =>
                team.id === updatedTeam.id ? updatedTeam : team,
            ),
        );
    }

    function handleResetAllScores() {
        const resetEvents: scoreEvent[] = teams
            .filter((team) => team.score > 0)
            .map((team) => ({
                id: crypto.randomUUID(),
                teamId: team.id,
                teamName: team.name,
                amount: -team.score,
                reason: "All scores reset",
                timestamp: new Date(),
            }));

        setTeams((currentTeams) =>
            currentTeams.map((team) => ({
                ...team,
                score: 0,
            })),
        );

        setScoreEvents((currentEvents) => [
            ...resetEvents,
            ...currentEvents,
        ]);
    }


    function handleScoreChange(
        teamId: string,
        amount: number,
        reason = "Score adjustment",
    ) {
        const team = teams.find(
            (currentTeam) => currentTeam.id === teamId,
        );

        if (!team) {
            return;
        }

        const newScore = Math.max(0, team.score + amount);
        const appliedAmount = newScore - team.score;

        if (appliedAmount === 0) {
            return;
        }

        setTeams((currentTeams) =>
            currentTeams.map((currentTeam) =>
                currentTeam.id === teamId
                    ? {
                        ...currentTeam,
                        score: newScore,
                    }
                    : currentTeam,
            ),
        );

        // Stores a permanent record of this score change.
        const scoreEvent: scoreEvent = {
            id: crypto.randomUUID(),
            teamId: team.id,
            teamName: team.name,
            amount: appliedAmount,
            reason,
            timestamp: new Date(),
        };

        setScoreEvents((currentEvents) => [
            scoreEvent,
            ...currentEvents,
        ]);
    }

    return (
        <>
            <div inert={teamPendingDeletion ? true : undefined}>
                <AppHeader
                    currentView={view}
                    onViewChange={setView}
                />

                <main>
                    {view === "scoreboard" ? (
                        <Scoreboard
                            teams={teams}
                            registrationDeadline={
                                eventSettings.registrationDeadline
                            }
                        />
                    ) : (
                        <AdminDashboard
                            settings={eventSettings}
                            teams={teams}
                            scoreEvents={scoreEvents}
                            onDeadlineChange={handleDeadlineChange}
                            onRequestDeleteTeam={handleRequestDeleteTeam}
                            onAddTeam={handleAddTeam}
                            onUpdateTeam={handleUpdateTeam}
                            onScoreChange={handleScoreChange}
                            onResetAllScores={handleResetAllScores}
                        />
                    )}
                </main>

                {deletedTeam && (
                    <UndoDeleteToast
                        teamName={deletedTeam.team.name}
                        onUndo={handleUndoDeleteTeam}
                    />
                )}
            </div>

            {teamPendingDeletion && (
                <DeleteTeamDialog
                    team={teamPendingDeletion}
                    onCancel={() => setTeamPendingDeletion(null)}
                    onConfirm={handleConfirmDeleteTeam}
                />
            )}
        </>
    );
}

export default App;
