import type { Team } from "../../models/Team"; // Imports the shared Team type.
import "./TeamCard.css"; // Loads the styles for this component.
import MedalBadge from "./MedalBadge"; // Imports the reusable game-style medal.

interface TeamCardProps {
    team: Team; // Team information to display.
    rank: number; // Team's leaderboard position.
}

function TeamCard({ team, rank }: TeamCardProps) {

    const isTopThree = rank <= 3; // Checks whether this team qualifies for a podium medal.
    const rankClass = rank === 1 ? "team-card--first" : rank === 2 ? "team-card--second" : rank === 3 ? "team-card--third" : "";
    return (


        <article className={`team-card ${rankClass}`}>


            <div className="team-card__rank">
                {isTopThree ? (
                    <MedalBadge rank={rank} />
                ) : (
                    <span className="team-card__rank-number">
                        {rank}
                    </span>
                )}
            </div>

            <div className="team-card__content">

                <h2 className="team-card__name">
                    {team.name}
                </h2>

                <p className="team-card__members">
                    {team.members.join(" • ")}
                </p>

            </div>

            <div className="team-card__score">

                <span className="team-card__score-value">
                    {team.score}
                </span>

                <span className="team-card__score-label">
                    pts
                </span>

            </div>

        </article>
    );
}

export default TeamCard;