import type { Team } from "../../models/Team"; // Imports the shared Team type.
import TeamList from "../team/TeamList"; // Displays the ranked teams.
import "./Scoreboard.css"; // Loads scoreboard styles.
import Countdown from "./Countdown"; // Displays the live registration countdown.
import RegistrationQR from "./RegistrationQR";
import { eventConfig } from "../../config/eventConfig";
import type {GameRound,RoundStanding} from "../../api/roundsApi";

interface ScoreboardProps {
    teams: Team[];
    registrationDeadline: Date;
    rounds: GameRound[];
    roundStandings: RoundStanding[];
    registrationFormUrl?: string;
}

function Scoreboard({ teams, registrationDeadline,rounds,roundStandings,registrationFormUrl }: ScoreboardProps) {
    const active=rounds.find(r=>r.status==="IN_PROGRESS"),last=[...rounds].reverse().find(r=>r.status==="COMPLETED");
    const roundTeams:Team[]=roundStandings.map(r=>({id:r.teamId,name:r.teamName,members:[],score:r.roundScore}));
    return (
        <section className="scoreboard">

            <div className="scoreboard__hero">

                <div className="scoreboard__live">
                    <span className="scoreboard__live-dot" />
                    LIVE SCOREBOARD
                </div>

                <h1 className="scoreboard__title">
                    Friends Like These
                </h1>

                <div className="scoreboard__colour-bar" aria-hidden="true">
                    <span className="scoreboard__colour-block scoreboard__colour-block--orange" />
                    <span className="scoreboard__colour-block scoreboard__colour-block--red" />
                    <span className="scoreboard__colour-block scoreboard__colour-block--purple" />
                    <span className="scoreboard__colour-block scoreboard__colour-block--blue" />
                    <span className="scoreboard__colour-block scoreboard__colour-block--teal" />
                </div>

                <div className="scoreboard__campuskey">
                    <span className="scoreboard__campuskey-name">
                        CampusKey
                    </span>

                    <span className="scoreboard__campuskey-edition">
                        EDITION
                    </span>
                </div>

                <p className="scoreboard__message">
                    {active?`ROUND ${active.roundNumber} OF ${rounds.length}`:last?`ROUND ${last.roundNumber} COMPLETE`:"Who knows their friends best?"}
                </p>

            </div>

            <div className="scoreboard__content">
                {(active||last)&&<><h2>{active?"Current Round":"Round Results"}</h2><TeamList teams={roundTeams} /><h2>Overall Standings</h2></>}
                <TeamList teams={teams} />
            </div>

            <div className="scoreboard__tools">


                <Countdown deadline={registrationDeadline} />
                <RegistrationQR formUrl={registrationFormUrl||eventConfig.registrationFormUrl} />
            </div>
        </section>
    );
}

export default Scoreboard;
