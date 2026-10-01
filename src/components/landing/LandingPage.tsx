import Countdown from "../scoreboard/Countdown";
import RegistrationQR from "../scoreboard/RegistrationQR";
import { eventConfig } from "../../config/eventConfig";
import banner from "../../assets/friends-like-these-banner.png";

import "./LandingPage.css";

interface LandingPageProps {
    registrationDeadline: Date;
    registrationFormUrl?: string;
}

function LandingPage({
    registrationDeadline,
    registrationFormUrl,
}: LandingPageProps) {

    const formUrl =
        registrationFormUrl ||
        eventConfig.registrationFormUrl;

    return (
        <section className="landing-page">

            <div className="landing-page__banner">
                <img
                    src={banner}
                    alt="Friends Like These CampusKey Edition"
                />
            </div>

            <div className="landing-page__content">

                <section className="landing-registration">

                    <div className="landing-registration__intro">
                        <span>TEAM REGISTRATION</span>

                        <h1>Get your team together.</h1>

                        <p>
                            Four players. One team. One chance to
                            prove how well you know your friends.
                        </p>

                        <div className="landing-registration__facts">
                            <strong>4 PLAYERS</strong>
                            <strong>1 TEAM LEADER</strong>
                            <strong>FRIENDS WELCOME</strong>
                        </div>
                    </div>

                    <Countdown
                        deadline={registrationDeadline}
                    />

                </section>

                <section className="landing-rules">

                    <div className="landing-rules__content">

                        <span className="landing-rules__eyebrow">
                            BEFORE YOU REGISTER
                        </span>

                        <h2>Basic Rules</h2>

                        <ol>
                            <li>
                                <span>1</span>
                                <p>
                                    Only <strong>one person (the Team Leader)</strong>  must
                                    register the team.
                                </p>
                            </li>

                            <li>
                                <span>2</span>
                                <p>
                                    Each team must have <strong>4 members</strong>,
                                    including the Team Leader.
                                </p>
                            </li>

                            <li>
                                <span>3</span>
                                <p>
                                    Teams may include both girls and boys.
                                    <strong> Mixed teams are welcome.</strong>
                                </p>
                            </li>

                            <li>
                                <span>4</span>
                                <p>
                                    <strong>Non-CampusKey residents</strong> are
                                    welcome to join a team.
                                </p>
                            </li>

                            <li>
                                <span>5</span>
                                <p>
                                    Team Leaders must submit the full list of
                                    all 4 members by
                                    <strong> 30 September at 18:00.</strong>
                                </p>
                            </li>

                            <li>
                                <span>6</span>
                                <p>
                                    <strong>Incomplete teams will not be considered.</strong>
                                </p>
                            </li>

                            <li>
                                <span>7</span>
                                <p>
                                    Only <strong>one submission per team</strong>
                                    is allowed.
                                </p>
                            </li>
                        </ol>

                    </div>

                    <div className="landing-rules__registration">

                        <RegistrationQR
                            formUrl={formUrl}
                        />

                        <a
                            className="landing-register-button"
                            href={formUrl}
                            target="_blank"
                            rel="noreferrer"
                        >
                            Register Your Team
                        </a>

                    </div>

                </section>

            </div>

        </section>
    );
}

export default LandingPage;
