import Countdown from "./Countdown";
import RegistrationQR from "./RegistrationQR";
import banner from "./assets/friends-like-these-banner.png";

type LandingPageProps = {
  registrationDeadline: Date;
};

const FORM_URL =
  "https://docs.google.com/forms/d/e/1FAIpQLScrIKTorkwZWmBsr-S8YPjJObn4nW5E6aViWcyPOp6zyTLsEg/viewform";

export default function LandingPage({
  registrationDeadline,
}: LandingPageProps) {
  return (
    <main className="tv-landing">
      <section className="tv-landing-card">

        <div className="tv-landing-card__banner">
          <img
            src={banner}
            alt="Friends Like These CampusKey Edition"
          />
        </div>

        <div className="tv-landing-card__body">

          <div className="tv-landing-info">
            <div className="tv-landing-label">
              TEAM REGISTRATION
            </div>

            <h1>Get your team together.</h1>

            <p className="tv-landing-subtitle">
              Four players. One team. One chance to prove how well
              you know your friends.
            </p>

            <div className="tv-landing-facts">
              <strong>4 PLAYERS</strong>
              <strong>1 TEAM LEADER</strong>
              <strong>FRIENDS WELCOME</strong>
            </div>

            <div className="tv-landing-rules">
              <h2>Basic Rules</h2>

              <p>1. One Team Leader registers the team.</p>
              <p>2. Every team must have 4 members.</p>
              <p>3. Mixed teams are welcome.</p>
              <p>4. Non-CampusKey residents may join a team.</p>
              <p>5. Submit the full team by 30 September at 18:00.</p>
              <p>6. Incomplete teams will not be considered.</p>
              <p>7. One submission per team.</p>
            </div>
          </div>

          <aside className="tv-landing-register">
            <Countdown deadline={registrationDeadline} />

            <a
              className="tv-landing-qr-link"
              href={FORM_URL}
              target="_blank"
              rel="noreferrer"
            >
              <RegistrationQR formUrl={FORM_URL} />

              <div className="tv-landing-scan">
                SCAN OR CLICK TO REGISTER TEAM
              </div>
            </a>
          </aside>

        </div>

      </section>
    </main>
  );
}
