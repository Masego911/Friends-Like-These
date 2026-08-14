import type { Team } from "../../models/Team"; // Imports the shared Team type.
import TeamList from "../team/TeamList"; // Displays the ranked teams.
import "./Scoreboard.css"; // Loads scoreboard styles.
import Countdown from "./Countdown"; // Displays the live registration countdown.
import RegistrationQR from "./RegistrationQR";
import { eventConfig } from "../../config/eventConfig";

interface ScoreboardProps {
    teams: Team[];
    registrationDeadline: Date;
}

function Scoreboard({ teams, registrationDeadline }: ScoreboardProps) {
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
                    Who knows their friends best?
                </p>

            </div>

            <div className="scoreboard__content">
                <TeamList teams={teams} />
            </div>

            <div className="scoreboard__tools">


                <Countdown deadline={registrationDeadline} />
                <RegistrationQR formUrl={eventConfig.registrationFormUrl} />
            </div>
        </section>
    );
}

export default Scoreboard;