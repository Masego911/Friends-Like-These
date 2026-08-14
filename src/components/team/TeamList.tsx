import type { Team } from "../../models/Team"; // Imports the shared Team type.
import TeamCard from "./TeamCard"; // Imports TeamCard so each team can be displayed.
import "./TeamList.css"; // Loads the list layout styles.

interface TeamListProps {
    teams: Team[]; // TeamList expects an array of Team objects.
}

function TeamList({ teams }: TeamListProps) {
    const sortedTeams = [...teams].sort((a, b) => b.score - a.score); // Creates a sorted copy without changing the original array.

    return (
        <section className="team-list">
            {sortedTeams.map((team, index) => (
                <TeamCard
                    key={team.id}
                    team={team}
                    rank={index + 1}
                />
            ))}
        </section>
    );
}

export default TeamList;
