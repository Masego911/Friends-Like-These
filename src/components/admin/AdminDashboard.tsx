import { useEffect, useState, type ReactNode } from "react";

import type { Team } from "../../models/Team";
import type { eventSettings } from "../../models/eventSettings";
import type { scoreEvent } from "../../models/scoreEvent";
import type { RegistrationStatus } from "../../api/registrationsApi";
import type {
    EventSettingsResponse,
    EventSummary,
} from "../../api/eventsApi";
import type {
    GameRound,
    RoundStanding,
} from "../../api/roundsApi";

import AddTeamForm from "./AddTeamForm";
import EditTeamForm from "./EditTeamForm";
import AdminEventSettings from "./AdminEventSettings";
import GameRounds from "./GameRounds";
import EventLifecyclePanel from "./EventLifecyclePanel";
import RegistrationManagement from "./RegistrationManagement";
import PreviousGames from "./PreviousGames";
import AdminSidebar, { type AdminPage } from "./AdminSidebar";
import GameContextHeader from "./GameContextHeader";
import AdminOverviewPage from "./AdminOverviewPage";
import LiveScoringPage from "./LiveScoringPage";
import TeamsManagementPage from "./TeamsManagementPage";
import DangerZone from "./DangerZone";

import "./AdminDashboard.css";

interface Props {
    settings: eventSettings;
    teams: Team[];
    scoreEvents: scoreEvent[];
    registrationStatus: RegistrationStatus | null;
    rounds: GameRound[];
    roundStandings?: RoundStanding[];
    adminName: string;
    currentEvent: EventSettingsResponse | null;
    events: EventSummary[];

    onDeadlineChange: (deadline: Date) => Promise<void>;
    onRequestDeleteTeam: (team: Team) => void;
    onAddTeam: (
        team: Pick<Team, "name" | "members">
    ) => Promise<void>;
    onUpdateTeam: (
        id: string,
        team: Pick<Team, "name" | "members">
    ) => Promise<void>;

    onResetAllScores: () => Promise<void>;
    onScoreChange: (
        id: string,
        amount: number,
        reason?: string
    ) => Promise<void>;

    onSyncRegistrations: () => Promise<void>;
    onRoundsChange: (numberOfRounds: number) => Promise<void>;
    onResetScoreActivity: () => Promise<void>;
    onResetEntireGame: () => Promise<void>;
    onStartRound: (roundNumber: number) => Promise<void>;
    onEndRound: (roundNumber: number) => Promise<void>;
    onLogout: () => Promise<void>;

    onCreateEvent: (value: {
        name: string;
        eventDate: string;
        registrationDeadline: string;
        totalRounds: number;
    }) => Promise<void>;

    onLifecycleAction: (
        action: "open" | "close" | "start" | "complete"
    ) => Promise<void>;

    onArchive: (id: string) => Promise<void>;
}

const PAGE_TITLES: Record<AdminPage, string> = {
    dashboard: "Dashboard",
    scoring: "Live Scoring",
    teams: "Teams",
    registration: "Registration",
    setup: "Game Setup",
    history: "Previous Games",
    settings: "Settings",
};

export default function AdminDashboard(props: Props) {
    const [page, setPage] = useState<AdminPage>("dashboard");
    const [navOpen, setNavOpen] = useState(false);

    const [showAddTeam, setShowAddTeam] = useState(false);
    const [editingTeam, setEditingTeam] = useState<Team | null>(null);

    /*
     * Navigation drawer behaviour.
     *
     * AdminDashboard owns this because the drawer belongs to the
     * Admin Console shell rather than an individual page.
     */
    useEffect(() => {
        if (!navOpen) {
            document.body.classList.remove("admin-drawer-open");
            return;
        }

        document.body.classList.add("admin-drawer-open");

        const handleEscape = (event: KeyboardEvent) => {
            if (event.key === "Escape") {
                setNavOpen(false);
            }
        };

        document.addEventListener("keydown", handleEscape);

        return () => {
            document.body.classList.remove("admin-drawer-open");
            document.removeEventListener("keydown", handleEscape);
        };
    }, [navOpen]);

    const navigateTo = (nextPage: AdminPage) => {
        setPage(nextPage);
        setNavOpen(false);
    };

    const handleAddTeam = async (
        team: Pick<Team, "name" | "members">
    ) => {
        await props.onAddTeam(team);
        setShowAddTeam(false);
    };

    const handleUpdateTeam = async (
        team: Pick<Team, "name" | "members">
    ) => {
        if (!editingTeam) {
            return;
        }

        await props.onUpdateTeam(editingTeam.id, team);
        setEditingTeam(null);
    };

    const renderDashboard = (): ReactNode => (
        <AdminOverviewPage
            event={props.currentEvent}
            teams={props.teams}
            registration={props.registrationStatus}
            rounds={props.rounds}
            activity={props.scoreEvents}
            onNavigate={navigateTo}
        />
    );

    const renderLiveScoring = (): ReactNode => (
        <LiveScoringPage
            event={props.currentEvent}
            rounds={props.rounds}
            teams={props.teams}
            activity={props.scoreEvents}
            onScoreChange={props.onScoreChange}
            onResetActivity={props.onResetScoreActivity}
            onStartRound={props.onStartRound}
            onEndRound={props.onEndRound}
        />
    );

    const renderTeams = (): ReactNode => (
        <TeamsManagementPage
            teams={props.teams}
            onAdd={() => setShowAddTeam(true)}
            onEdit={setEditingTeam}
            onDelete={props.onRequestDeleteTeam}
        />
    );

    const renderRegistration = (): ReactNode => (
        <div className="admin-page">
            <RegistrationManagement
                status={props.registrationStatus}
                onSyncNow={props.onSyncRegistrations}
            />
        </div>
    );

    const renderGameSetup = (): ReactNode => (
        <div className="admin-page">
            <div className="admin-page__heading">
                <div>
                    <span>Current game</span>
                    <h2>Game Setup</h2>
                    <p>
                        Configure the current event using the existing
                        game settings.
                    </p>
                </div>
            </div>

            <EventLifecyclePanel
                event={props.currentEvent}
                onCreate={props.onCreateEvent}
                onAction={props.onLifecycleAction}
            />

            {props.currentEvent && (
                <div className="setup-grid">
                    <AdminEventSettings
                        registrationDeadline={
                            props.settings.registrationDeadline
                        }
                        onDeadlineChange={props.onDeadlineChange}
                    />

                <GameRounds
                    key={props.settings.totalRounds}
                    totalRounds={props.settings.totalRounds}
                        currentRound={props.settings.currentRound}
                        rounds={props.rounds}
                        eventStatus={props.currentEvent.status}
                        onSave={props.onRoundsChange}
                        onStart={props.onStartRound}
                        onEnd={props.onEndRound}
                    />
                </div>
            )}
        </div>
    );

    const renderHistory = (): ReactNode => (
        <div className="admin-page">
            <PreviousGames
                events={props.events}
                onArchive={props.onArchive}
            />
        </div>
    );

    const renderSettings = (): ReactNode => (
        <div className="admin-page">
            <div className="admin-page__heading">
                <div>
                    <span>Administration</span>
                    <h2>Settings</h2>
                    <p>
                        Your authenticated session and game safety
                        controls.
                    </p>
                </div>
            </div>

            <section className="settings-card">
                <h3>Administrator session</h3>

                <dl>
                    <div>
                        <dt>Signed in as</dt>
                        <dd>{props.adminName}</dd>
                    </div>

                    <div>
                        <dt>Data source</dt>
                        <dd>Friends Like These API and SQL Server</dd>
                    </div>

                    <div>
                        <dt>Refresh</dt>
                        <dd>
                            Administrator data refreshes every 12 seconds
                        </dd>
                    </div>
                </dl>
            </section>

            <DangerZone
                teamCount={props.teams.length}
                live={props.currentEvent?.status === "LIVE"}
                onResetScores={props.onResetAllScores}
                onResetEntireGame={props.onResetEntireGame}
            />
        </div>
    );

    /*
     * Page selection is kept in one place.
     *
     * AdminDashboard decides WHICH page is active while each page
     * component remains responsible for its own presentation.
     */
    const renderPage = (): ReactNode => {
        switch (page) {
            case "dashboard":
                return renderDashboard();

            case "scoring":
                return renderLiveScoring();

            case "teams":
                return renderTeams();

            case "registration":
                return renderRegistration();

            case "setup":
                return renderGameSetup();

            case "history":
                return renderHistory();

            case "settings":
                return renderSettings();

            default:
                return renderDashboard();
        }
    };

    return (
        <section className="admin-console">
            <AdminSidebar
                activePage={page}
                adminName={props.adminName}
                open={navOpen}
                onNavigate={navigateTo}
                onClose={() => setNavOpen(false)}
                onLogout={props.onLogout}
            />

            <div
                className="admin-console__main"
                inert={navOpen ? true : undefined}
            >
                <GameContextHeader
                    event={props.currentEvent}
                    teamCount={props.teams.length}
                    onMenu={() => setNavOpen(true)}
                />

                <main aria-label={PAGE_TITLES[page]}>
                    {renderPage()}
                </main>
            </div>

            {showAddTeam && (
                <div className="admin-modal-backdrop">
                    <div
                        className="admin-modal"
                        role="dialog"
                        aria-modal="true"
                        aria-label="Add Team"
                    >
                        <AddTeamForm
                            existingTeams={props.teams}
                            onAddTeam={handleAddTeam}
                            onCancel={() => setShowAddTeam(false)}
                        />
                    </div>
                </div>
            )}

            {editingTeam && (
                <div className="admin-modal-backdrop">
                    <div
                        className="admin-modal"
                        role="dialog"
                        aria-modal="true"
                        aria-label={`Edit ${editingTeam.name}`}
                    >
                        <EditTeamForm
                            team={editingTeam}
                            existingTeams={props.teams}
                            onSave={handleUpdateTeam}
                            onCancel={() => setEditingTeam(null)}
                        />
                    </div>
                </div>
            )}
        </section>
    );
}
