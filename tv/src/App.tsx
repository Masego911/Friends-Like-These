import { useEffect, useRef, useState } from "react";
import "./App.css";
import Countdown from "./Countdown";
import RegistrationQR from "./RegistrationQR";

const API = "";

type EventInfo = {
  id: string;
  name: string;
  registrationDeadline: string;
  status: string;
  totalRounds: number;
  currentRound: number;
};

type Round = {
  roundNumber: number;
  status: "NOT_STARTED" | "IN_PROGRESS" | "COMPLETED";
};

type Standing = {
  teamId: string;
  teamName: string;
  score?: number;
  roundScore?: number;
};

type Transition =
  | { type: "START"; round: number }
  | { type: "WINNER"; round: number; name: string; score: number }
  | null;

export default function App() {
  const [event, setEvent] = useState<EventInfo | null>(null);
  const [rounds, setRounds] = useState<Round[]>([]);
  const [standings, setStandings] = useState<Standing[]>([]);
  const [transition, setTransition] = useState<Transition>(null);
  const [connected, setConnected] = useState(true);

  const previousRound = useRef<number | null | undefined>(undefined);
  const lastRoundScores = useRef<Standing[]>([]);

  async function load() {
    try {
      const eventResponse = await fetch(`${API}/api/events/current`);
      if (!eventResponse.ok) throw new Error();

      const currentEvent: EventInfo = await eventResponse.json();

      const roundsResponse = await fetch(`${API}/api/events/current/rounds`);
      const currentRounds: Round[] = roundsResponse.ok
        ? await roundsResponse.json()
        : [];

      const active = currentRounds.find(r => r.status === "IN_PROGRESS");

      let currentStandings: Standing[] = [];

      if (active) {
        const response = await fetch(
          `${API}/api/events/current/rounds/${active.roundNumber}/leaderboard`
        );

        if (response.ok) {
          currentStandings = await response.json();
          lastRoundScores.current = currentStandings;
        }
      } else {
        const response = await fetch(
          `${API}/api/events/${currentEvent.id}/leaderboard`
        );

        if (response.ok) currentStandings = await response.json();
      }

      const currentRound = active?.roundNumber ?? null;

      if (previousRound.current !== undefined) {
        if (previousRound.current === null && currentRound !== null) {
          setTransition({ type: "START", round: currentRound });
          setTimeout(() => setTransition(null), 2200);
        }

        if (previousRound.current !== null && currentRound === null) {
          const finalRound = [...lastRoundScores.current].sort(
            (a, b) => (b.roundScore ?? 0) - (a.roundScore ?? 0)
          );

          if (finalRound[0]) {
            setTransition({
              type: "WINNER",
              round: previousRound.current,
              name: finalRound[0].teamName,
              score: finalRound[0].roundScore ?? 0,
            });

            setTimeout(() => setTransition(null), 3600);
          }
        }
      }

      previousRound.current = currentRound;

      setEvent(currentEvent);
      setRounds(currentRounds);
      setStandings(currentStandings);
      setConnected(true);
    } catch {
      setConnected(false);
    }
  }

  useEffect(() => {
    load();
    const timer = setInterval(load, 1000);
    return () => clearInterval(timer);
  }, []);

  const activeRound = rounds.find(r => r.status === "IN_PROGRESS");

  const sorted = [...standings].sort((a, b) => {
    const aScore = activeRound ? a.roundScore ?? 0 : a.score ?? 0;
    const bScore = activeRound ? b.roundScore ?? 0 : b.score ?? 0;
    return bScore - aScore;
  });

  return (
    <main className="tv">
      {transition && (
        <section className={`transition transition--${transition.type.toLowerCase()}`}>
          <div className="brand">FRIENDS LIKE THESE</div>

          {transition.type === "START" ? (
            <>
              <div className="transition-small">GET READY</div>
              <div className="transition-big">ROUND {transition.round}</div>
              <div className="transition-status">ROUND IN PROGRESS</div>
            </>
          ) : (
            <>
              <div className="transition-small">ROUND {transition.round}</div>
              <div className="trophy">★</div>
              <div className="transition-status winner-label">WINNER</div>
              <div className="winner-name">{transition.name}</div>
              <div className="winner-score">{transition.score} PTS</div>
            </>
          )}

          <div className="colour-bar">
            <i/><i/><i/><i/><i/>
          </div>
        </section>
      )}

      <header>
        <div className="live">
          <span className="live-dot" />
          LIVE SCOREBOARD
        </div>

        <h1>Friends Like These</h1>

        <div className="colour-bar header-bar">
          <i/><i/><i/><i/><i/>
        </div>

        <div className="edition">CAMPUSKEY EDITION</div>

        <div className="round-label">
          {activeRound
            ? `ROUND ${activeRound.roundNumber} OF ${event?.totalRounds ?? rounds.length}`
            : event?.status === "COMPLETED"
              ? "FINAL STANDINGS"
              : "OVERALL STANDINGS"}
        </div>
      </header>

      {event && event.status === "REGISTRATION_OPEN" && (
        <section className="tv-registration">
          <Countdown deadline={new Date(event.registrationDeadline)} />
          <RegistrationQR
            formUrl="https://docs.google.com/forms/d/e/1FAIpQLScrIKTorkwZWmBsr-S8YPjJObn4nW5E6aViWcyPOp6zyTLsEg/viewform"
          />
        </section>
      )}

      <section className="leaderboard">
        {sorted.length === 0 ? (
          <div className="waiting">
            {connected ? "Waiting for teams..." : "Reconnecting to live scores..."}
          </div>
        ) : (
          sorted.map((team, index) => {
            const score = activeRound
              ? team.roundScore ?? 0
              : team.score ?? 0;

            return (
              <article
                className={`team ${index === 0 ? "team--first" : ""}`}
                key={team.teamId}
              >
                <div className="rank">
                  {index < 3 ? ["1", "2", "3"][index] : index + 1}
                </div>

                <div className="team-name">{team.teamName}</div>

                <div className="score">
                  <strong>{score}</strong>
                  <span>PTS</span>
                </div>
              </article>
            );
          })
        )}
      </section>

      <footer>
        <span>{connected ? "LIVE" : "RECONNECTING"}</span>
        <span>FRIENDS LIKE THESE</span>
      </footer>
    </main>
  );
}

