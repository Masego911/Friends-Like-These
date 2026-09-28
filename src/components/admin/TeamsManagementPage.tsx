import type { Team } from "../../models/Team";

interface Props { teams: Team[]; onAdd: () => void; onEdit: (team: Team) => void; onDelete: (team: Team) => void; }

export default function TeamsManagementPage({ teams, onAdd, onEdit, onDelete }: Props) {
    const ranked = [...teams].sort((a,b) => b.score-a.score || a.name.localeCompare(b.name));
    return <div className="admin-page"><div className="admin-page__heading"><div><span>Current game</span><h2>Teams</h2><p>Manage team identity and membership. Scores remain controlled from Live Scoring.</p></div><button className="button button--primary" type="button" onClick={onAdd}>Add Team</button></div>
        {ranked.length === 0 ? <div className="admin-empty"><h3>No teams yet</h3><p>Registration sync and manually added teams will appear here.</p><button className="button button--primary" onClick={onAdd}>Add first team</button></div> : <div className="teams-table" role="table" aria-label="Current teams"><div className="teams-table__head" role="row"><span>Team</span><span>Members</span><span>Score</span><span>Actions</span></div>{ranked.map((team,index) => <article key={team.id} role="row"><div><b>#{index+1} {team.name}</b><small>{team.members.join(", ") || "No members recorded"}</small></div><span>{team.members.length}</span><strong>{team.score} pts</strong><div><button type="button" onClick={() => onEdit(team)}>Edit</button><button type="button" className="danger-link" onClick={() => onDelete(team)}>Delete</button></div></article>)}</div>}
    </div>;
}
