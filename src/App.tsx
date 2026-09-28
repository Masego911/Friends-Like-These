import { useCallback, useEffect, useRef, useState } from "react";

import "./styles/App.css";

import type { Team } from "./models/Team";
import type { scoreEvent } from "./models/scoreEvent";
import type {
    EventSettingsResponse,
    EventSummary,
} from "./api/eventsApi";
import type {
    GameRound,
    RoundStanding,
} from "./api/roundsApi";
import type { RegistrationStatus } from "./api/registrationsApi";
import type { AdminSession } from "./api/authApi";

import AppHeader from "./components/layout/AppHeader";
import Scoreboard from "./components/scoreboard/Scoreboard";
import AdminDashboard from "./components/admin/AdminDashboard";
import AdminLogin from "./components/admin/AdminLogin";
import DeleteTeamDialog from "./components/admin/DeleteTeamDialog";
import UndoDeleteToast from "./components/admin/UndoDeleteToast";
import PreviousGames from "./components/admin/PreviousGames";

import {
    getTeams,
    createTeam,
    updateTeam,
    deleteTeam,
    restoreTeam,
    getLeaderboard,
} from "./api/teamsApi";

import {
    getCurrentEvent,
    getEvent,
    getEventLeaderboard,
    updateRegistrationDeadline,
    updateTotalRounds,
    getEvents,
    createEvent,
    openRegistration,
    closeRegistration,
    startGame,
    completeGame,
    archiveEvent,
} from "./api/eventsApi";
import { ApiError } from "./api/api";

import {
    adjustScore,
    getScoreHistory,
    resetScores,
    resetScoreActivity,
    resetEntireGame,
} from "./api/scoresApi";

import {
    getCurrentRounds,
    getCurrentRoundStandings,
    getEventRounds,
    getEventRoundStandings,
    startRound,
    endRound,
} from "./api/roundsApi";

import {
    getRegistrationStatus,
    importRegistrations,
} from "./api/registrationsApi";

import {
    getSession,
    login,
    logout,
} from "./api/authApi";

import { getPublicRegistration } from "./api/publicApi";

type View = "scoreboard" | "admin" | "history";

type LifecycleAction =
    | "open"
    | "close"
    | "start"
    | "complete";

const ANONYMOUS_SESSION: AdminSession = {
    authenticated: false,
    username: null,
    email: null,
    displayName: null,
    role: null,
};

const PUBLIC_POLLING_INTERVAL_MS = 1_000;
const ADMIN_POLLING_INTERVAL_MS = 12_000;

export default function App() {

    /*
     * ---------------------------------------------------------
     * Navigation and authentication
     * ---------------------------------------------------------
     */

    const [view, setView] =
        useState<View>("scoreboard");

    const [session, setSession] =
        useState<AdminSession | null>(null);

    const [checkingSession, setCheckingSession] =
        useState(false);

    /*
     * ---------------------------------------------------------
     * Public game state
     * ---------------------------------------------------------
     */

    const [currentEvent, setCurrentEvent] =
        useState<EventSettingsResponse | null>(null);

    const [scoreboardEvent, setScoreboardEvent] =
        useState<EventSettingsResponse | null>(null);

    const [leaderboard, setLeaderboard] =
        useState<Team[]>([]);

    const [rounds, setRounds] =
        useState<GameRound[]>([]);

    const [roundStandings, setRoundStandings] =
        useState<RoundStanding[]>([]);

    const [registrationDeadline, setRegistrationDeadline] =
        useState<Date>(new Date());

    const [registrationFormUrl, setRegistrationFormUrl] =
        useState("");

    /*
     * ---------------------------------------------------------
     * Administrator state
     * ---------------------------------------------------------
     */

    const [teams, setTeams] =
        useState<Team[]>([]);

    const [scoreEvents, setScoreEvents] =
        useState<scoreEvent[]>([]);

    const [registrationStatus, setRegistrationStatus] =
        useState<RegistrationStatus | null>(null);

    const [events, setEvents] =
        useState<EventSummary[]>([]);

    /*
     * ---------------------------------------------------------
     * UI state
     * ---------------------------------------------------------
     */

    const [error, setError] =
        useState("");

    const [teamPendingDeletion, setTeamPendingDeletion] =
        useState<Team | null>(null);

    const [recentlyDeletedTeam, setRecentlyDeletedTeam] =
        useState<Team | null>(null);

    /*
     * Prevent an older administrator refresh from overwriting
     * a score mutation that completed after that refresh began.
     */
    const scoreMutationEpoch = useRef(0);
    const displayedEventId = useRef<string | null>(null);
    const publicLoadEpoch = useRef(0);

    /*
     * ---------------------------------------------------------
     * Public data
     * ---------------------------------------------------------
     */

    const loadPublicData = useCallback(async () => {
        const loadEpoch = ++publicLoadEpoch.current;

        try {
            let eventResult: EventSettingsResponse | null;
            let leaderboardResult: Team[] = [];
            let roundsResult: GameRound[] = [];
            let historicalEvent = false;

            try {
                eventResult = await getCurrentEvent();
            } catch (error) {
                if (!(error instanceof ApiError) || error.status !== 404) {
                    throw error;
                }

                const rememberedId = displayedEventId.current;

                if (!rememberedId) {
                    eventResult = null;
                } else {
                    const rememberedEvent = await getEvent(rememberedId);

                    if (rememberedEvent.status !== "COMPLETED") {
                        return;
                    }

                    eventResult = {
                        ...rememberedEvent,
                        current: false,
                    };
                    historicalEvent = true;
                }
            }

            if (eventResult) {
                displayedEventId.current = eventResult.id;

                if (historicalEvent) {
                    const [historicalLeaderboard, historicalRounds] =
                        await Promise.all([
                            getEventLeaderboard(eventResult.id),
                            getEventRounds(eventResult.id),
                        ]);

                    leaderboardResult = historicalLeaderboard.map(row => ({
                        id: row.teamId,
                        name: row.teamName,
                        members: [],
                        score: row.score,
                    }));
                    roundsResult = historicalRounds;
                } else {
                    [leaderboardResult, roundsResult] = await Promise.all([
                        getLeaderboard(),
                        getCurrentRounds(),
                    ]);
                }
            }

            const registrationResult =
                await getPublicRegistration().catch(() => null);

            if (loadEpoch !== publicLoadEpoch.current) {
                return;
            }

            setCurrentEvent(historicalEvent ? null : eventResult);
            setScoreboardEvent(eventResult);
            setLeaderboard(leaderboardResult);
            setRounds(roundsResult);

            if (registrationResult) {

                setRegistrationDeadline(
                    new Date(
                        registrationResult.registrationDeadline
                        ?? Date.now()
                    )
                );

                setRegistrationFormUrl(
                    registrationResult.registrationFormUrl
                );
            }

            /*
             * -------------------------------------------------
             * Determine which round standings should be loaded
             * -------------------------------------------------
             */

            const activeRound =
                roundsResult.find(
                    round =>
                        round.status ===
                        "IN_PROGRESS"
                );

            const latestCompletedRound =
                [...roundsResult]
                    .reverse()
                    .find(
                        round =>
                            round.status ===
                            "COMPLETED"
                    );

            const displayedRound =
                activeRound ??
                latestCompletedRound;

            if (displayedRound) {

                const standings = historicalEvent && eventResult
                    ? await getEventRoundStandings(
                        eventResult.id,
                        displayedRound.roundNumber
                    )
                    : await getCurrentRoundStandings(
                        displayedRound.roundNumber
                    );

                if (loadEpoch !== publicLoadEpoch.current) {
                    return;
                }

                setRoundStandings(
                    standings
                );

            } else {

                setRoundStandings([]);
            }

            setError("");

        } catch {

            setError(
                "Public scoreboard is temporarily unavailable."
            );
        }

    }, []);

    /*
     * ---------------------------------------------------------
     * Administrator data
     * ---------------------------------------------------------
     */

    const loadAdminData =
        useCallback(async () => {

            const teamEpochAtRequestStart =
                scoreMutationEpoch.current;

            const [
                teamsResult,
                historyResult,
                registrationResult,
                eventsResult,
            ] = await Promise.allSettled([
                getTeams(),
                getScoreHistory(),
                getRegistrationStatus(),
                getEvents(),
            ]);

            /*
             * Do not allow an older polling request to overwrite
             * a score mutation that occurred while it was running.
             */
            if (
                teamsResult.status ===
                "fulfilled"
                &&
                teamEpochAtRequestStart ===
                scoreMutationEpoch.current
            ) {

                setTeams(
                    teamsResult.value
                );
            }

            if (
                historyResult.status ===
                "fulfilled"
            ) {

                setScoreEvents(
                    historyResult.value
                );
            }

            if (
                registrationResult.status ===
                "fulfilled"
            ) {

                setRegistrationStatus(
                    registrationResult.value
                );
            }

            if (
                eventsResult.status ===
                "fulfilled"
            ) {

                setEvents(
                    eventsResult.value
                );
            }

            const primaryDataLoaded =
                teamsResult.status ===
                "fulfilled"
                &&
                registrationResult.status ===
                "fulfilled";

            if (primaryDataLoaded) {
                setError("");
            }

            const hasFailure = [
                teamsResult,
                historyResult,
                registrationResult,
                eventsResult,
            ].some(
                result =>
                    result.status ===
                    "rejected"
            );

            if (hasFailure) {

                throw new Error(
                    "Some administrator data is temporarily unavailable."
                );
            }

        }, []);

    /*
     * ---------------------------------------------------------
     * Refresh
     * ---------------------------------------------------------
     */

    const refreshApplication =
        useCallback(async () => {

            await loadPublicData();

            if (session?.authenticated) {
                await loadAdminData();
            }

        }, [
            session?.authenticated,
            loadPublicData,
            loadAdminData,
        ]);

    /*
     * ---------------------------------------------------------
     * Public polling
     * ---------------------------------------------------------
     */

    useEffect(() => {

        void loadPublicData();

        const timer =
            window.setInterval(
                () =>
                    void loadPublicData(),
                PUBLIC_POLLING_INTERVAL_MS
            );

        return () => {

            window.clearInterval(
                timer
            );
        };

    }, [loadPublicData]);

    /*
     * ---------------------------------------------------------
     * Session verification
     * ---------------------------------------------------------
     */

    useEffect(() => {

        if (
            view ===
            "scoreboard"
        ) {
            return;
        }

        let cancelled = false;

        const verifySession =
            async () => {

                setCheckingSession(true);

                try {

                    const currentSession =
                        await getSession();

                    if (cancelled) {
                        return;
                    }

                    setSession(
                        currentSession
                    );

                    if (
                        currentSession.authenticated
                    ) {

                        try {

                            await loadAdminData();

                        } catch {

                            if (!cancelled) {

                                setError(
                                    "Administrator data is temporarily unavailable."
                                );
                            }
                        }
                    }

                } catch {

                    if (!cancelled) {

                        setSession(
                            ANONYMOUS_SESSION
                        );
                    }

                } finally {

                    if (!cancelled) {

                        setCheckingSession(
                            false
                        );
                    }
                }
            };

        void verifySession();

        return () => {

            cancelled = true;
        };

    }, [
        view,
        loadAdminData,
    ]);

    /*
     * ---------------------------------------------------------
     * Administrator polling
     * ---------------------------------------------------------
     */

    useEffect(() => {

        if (
            view !== "admin"
            ||
            !session?.authenticated
        ) {
            return;
        }

        const timer =
            window.setInterval(
                () => {

                    void loadAdminData()
                        .catch(() => {

                            setError(
                                "Administrator data is temporarily unavailable."
                            );
                        });

                },
                ADMIN_POLLING_INTERVAL_MS
            );

        return () => {

            window.clearInterval(
                timer
            );
        };

    }, [
        view,
        session?.authenticated,
        loadAdminData,
    ]);

    /*
     * ---------------------------------------------------------
     * Authentication
     * ---------------------------------------------------------
     */

    const handleLogin =
        async (
            username: string,
            password: string
        ) => {

            const currentSession =
                await login(
                    username,
                    password
                );

            setSession(
                currentSession
            );

            await loadAdminData();
        };

    const handleLogout =
        async () => {

            await logout();

            setSession(
                ANONYMOUS_SESSION
            );

            setView(
                "scoreboard"
            );

            setTeams([]);
            setScoreEvents([]);
            setRegistrationStatus(null);
        };

    /*
     * ---------------------------------------------------------
     * Generic mutation helper
     * ---------------------------------------------------------
     */

    const mutateAndRefresh =
        async (
            action: () =>
                Promise<unknown>
        ) => {

            await action();

            await refreshApplication();
        };

    /*
     * ---------------------------------------------------------
     * Scoring
     * ---------------------------------------------------------
     */

    const handleScoreChange =
        async (
            teamId: string,
            amount: number,
            reason =
            "Score adjustment"
        ) => {

            /*
             * Mark the beginning of a score mutation so an older
             * polling response cannot overwrite its result.
             */
            scoreMutationEpoch.current += 1;

            try {

                const updatedTeam =
                    await adjustScore(
                        teamId,
                        amount,
                        reason
                    );

                /*
                 * Increment again after the server mutation
                 * succeeds.
                 */
                scoreMutationEpoch.current += 1;

                setTeams(
                    currentTeams =>
                        currentTeams.map(
                            team =>
                                team.id ===
                                updatedTeam.id
                                    ? updatedTeam
                                    : team
                        )
                );

                const updatedHistory =
                    await getScoreHistory();

                setScoreEvents(
                    updatedHistory
                );

                setError("");

            } catch (error) {

                scoreMutationEpoch.current += 1;

                throw error;
            }
        };

    /*
     * ---------------------------------------------------------
     * Event lifecycle
     * ---------------------------------------------------------
     */

    const handleLifecycleAction = async (
        action: LifecycleAction
    ) => {

        /*
         * Complete Game is slightly different from the other
         * lifecycle transitions.
         *
         * The backend correctly:
         *
         * LIVE -> COMPLETED
         *
         * and then clears is_current.
         *
         * Therefore GET /api/events/current no longer returns the
         * event after completion.
         *
         * We preserve the successful completion response locally
         * so the public scoreboard can see COMPLETED and launch
         * the Grand Winner sequence.
         */
        const lifecycleActions: Record<
            LifecycleAction,
            () => Promise<unknown>
        > = {
            open:
            openRegistration,

            close:
            closeRegistration,

            start:
            startGame,

            complete:
            completeGame,
        };

        await mutateAndRefresh(
            lifecycleActions[action]
        );
    };


    /*
     * ---------------------------------------------------------
     * Team deletion / restoration
     * ---------------------------------------------------------
     */

    const handleConfirmDeleteTeam =
        async () => {

            if (
                !teamPendingDeletion
            ) {
                return;
            }

            const teamToDelete =
                teamPendingDeletion;

            await mutateAndRefresh(
                () =>
                    deleteTeam(
                        teamToDelete.id
                    )
            );

            setRecentlyDeletedTeam(
                teamToDelete
            );

            setTeamPendingDeletion(
                null
            );
        };

    const handleRestoreTeam =
        async () => {

            if (
                !recentlyDeletedTeam
            ) {
                return;
            }

            await mutateAndRefresh(
                () =>
                    restoreTeam(
                        recentlyDeletedTeam.id
                    )
            );

            setRecentlyDeletedTeam(
                null
            );
        };

    /*
     * ---------------------------------------------------------
     * Derived settings
     * ---------------------------------------------------------
     */

    const settings = {

        registrationDeadline,

        totalRounds:
            currentEvent?.totalRounds ??
            5,

        currentRound:
            currentEvent?.currentRound ??
            1,
    };

    /*
     * ---------------------------------------------------------
     * View rendering
     * ---------------------------------------------------------
     */

    const renderContent = () => {

        /*
         * -----------------------------------------------------
         * PUBLIC SCOREBOARD
         * -----------------------------------------------------
         */

        if (
            view ===
            "scoreboard"
        ) {

            return (

                <Scoreboard
                    teams={
                        leaderboard
                    }
                    registrationDeadline={
                        registrationDeadline
                    }
                    rounds={
                        rounds
                    }
                    roundStandings={
                        roundStandings
                    }
                    registrationFormUrl={
                        registrationFormUrl
                    }

                    /*
                     * The public scoreboard now receives the
                     * real event lifecycle state.
                     *
                     * This will allow it to distinguish:
                     *
                     * End Round
                     *      from
                     * Complete Game.
                     */
                    eventStatus={
                        scoreboardEvent?.status ??
                        null
                    }
                />

            );
        }

        /*
         * -----------------------------------------------------
         * SESSION CHECK
         * -----------------------------------------------------
         */

        if (checkingSession) {

            return (

                <p className="verification-screen">
                    Checking administrator session…
                </p>

            );
        }

        /*
         * -----------------------------------------------------
         * ADMIN LOGIN
         * -----------------------------------------------------
         */

        if (
            !session?.authenticated
        ) {

            return (

                <AdminLogin
                    onLogin={
                        handleLogin
                    }
                />

            );
        }

        /*
         * -----------------------------------------------------
         * PREVIOUS GAMES
         * -----------------------------------------------------
         */

        if (
            view ===
            "history"
        ) {

            return (

                <PreviousGames
                    events={
                        events
                    }
                    onArchive={
                        eventId =>
                            mutateAndRefresh(
                                () =>
                                    archiveEvent(
                                        eventId
                                    )
                            )
                    }
                />

            );
        }

        /*
         * -----------------------------------------------------
         * ADMIN DASHBOARD
         * -----------------------------------------------------
         */

        return (

            <AdminDashboard
                settings={
                    settings
                }

                currentEvent={
                    currentEvent
                }

                rounds={
                    rounds
                }

                roundStandings={
                    roundStandings
                }

                teams={
                    teams
                }

                scoreEvents={
                    scoreEvents
                }

                registrationStatus={
                    registrationStatus
                }

                events={
                    events
                }

                adminName={
                    session.displayName
                    ??
                    session.username
                    ??
                    "Administrator"
                }

                onLogout={
                    handleLogout
                }

                onArchive={
                    eventId =>
                        mutateAndRefresh(
                            () =>
                                archiveEvent(
                                    eventId
                                )
                        )
                }

                onCreateEvent={
                    value =>
                        mutateAndRefresh(
                            () =>
                                createEvent(
                                    value
                                )
                        )
                }

                onLifecycleAction={
                    handleLifecycleAction
                }

                onDeadlineChange={
                    date =>
                        mutateAndRefresh(
                            () =>
                                updateRegistrationDeadline(
                                    date.toISOString()
                                )
                        )
                }

                onRoundsChange={
                    count =>
                        mutateAndRefresh(
                            () =>
                                updateTotalRounds(
                                    count
                                )
                        )
                }

                onStartRound={
                    roundNumber =>
                        mutateAndRefresh(
                            () =>
                                startRound(
                                    roundNumber
                                )
                        )
                }

                onEndRound={
                    roundNumber =>
                        mutateAndRefresh(
                            () =>
                                endRound(
                                    roundNumber
                                )
                        )
                }

                onResetScoreActivity={
                    () =>
                        mutateAndRefresh(
                            resetScoreActivity
                        )
                }

                onResetEntireGame={
                    () =>
                        mutateAndRefresh(
                            resetEntireGame
                        )
                }

                onRequestDeleteTeam={
                    setTeamPendingDeletion
                }

                onAddTeam={
                    team =>
                        mutateAndRefresh(
                            () =>
                                createTeam(
                                    team.name,
                                    team.members
                                )
                        )
                }

                onUpdateTeam={
                    (
                        teamId,
                        team
                    ) =>
                        mutateAndRefresh(
                            () =>
                                updateTeam(
                                    teamId,
                                    team.name,
                                    team.members
                                )
                        )
                }

                onScoreChange={
                    handleScoreChange
                }

                onResetAllScores={
                    () =>
                        mutateAndRefresh(
                            resetScores
                        )
                }

                onSyncRegistrations={
                    () =>
                        mutateAndRefresh(
                            importRegistrations
                        )
                }
            />

        );
    };

    /*
     * ---------------------------------------------------------
     * Application
     * ---------------------------------------------------------
     */

    return (
        <>

            <div
                inert={
                    teamPendingDeletion
                        ? true
                        : undefined
                }
            >

                {view !== "admin" && (

                    <AppHeader
                        currentView={
                            view
                        }
                        onViewChange={
                            setView
                        }
                    />

                )}

                {error && (

                    <p role="alert">
                        {error}
                    </p>

                )}

                <main>
                    {renderContent()}
                </main>

                {recentlyDeletedTeam && (

                    <UndoDeleteToast
                        teamName={
                            recentlyDeletedTeam.name
                        }
                        onUndo={
                            handleRestoreTeam
                        }
                    />

                )}

            </div>

            {teamPendingDeletion && (

                <DeleteTeamDialog
                    team={
                        teamPendingDeletion
                    }
                    onCancel={
                        () =>
                            setTeamPendingDeletion(
                                null
                            )
                    }
                    onConfirm={
                        handleConfirmDeleteTeam
                    }
                />

            )}

        </>
    );
}
