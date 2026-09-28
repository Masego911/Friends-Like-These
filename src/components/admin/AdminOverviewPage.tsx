import type { EventSettingsResponse } from "../../api/eventsApi";
import type { RegistrationStatus } from "../../api/registrationsApi";
import type { GameRound } from "../../api/roundsApi";
import type { scoreEvent } from "../../models/scoreEvent";
import type { Team } from "../../models/Team";

interface Props { event:EventSettingsResponse|null; teams:Team[]; registration:RegistrationStatus|null; rounds:GameRound[]; activity:scoreEvent[]; onNavigate:(page:"scoring"|"teams"|"registration"|"setup")=>void; }
const pretty=(value?:string)=>value?value.replaceAll("_"," ").toLowerCase().replace(/\b\w/g,c=>c.toUpperCase()):"No current game";
const syncTime=(value?:string|null)=>value?new Date(value).toLocaleTimeString("en-ZA",{hour:"2-digit",minute:"2-digit"}):"Not yet";

export default function AdminOverviewPage({event,teams,registration,rounds,activity,onNavigate}:Props){
    const live=event?.status==="LIVE", completed=rounds.filter(round=>round.status==="COMPLETED").length;
    const syncHealthy=Boolean(registration?.googleIntegrationAvailable&&registration.lastSyncSuccessful);
    const sorted=[...teams].sort((a,b)=>b.score-a.score||a.name.localeCompare(b.name));
    return <div className="admin-page dashboard-page">
        <section className={`game-hero ${live?"game-hero--live":""}`}>
            <div className="game-hero__colour" aria-hidden="true"><i/><i/><i/><i/><i/></div>
            <div className="game-hero__content"><span className="game-hero__brand">Friends Like These</span><div className="game-hero__status">{live&&<i/>}{pretty(event?.status)}</div><h2>{event?.name??"Set up your next game"}</h2><p><strong>{teams.length}</strong> team{teams.length===1?"":"s"} registered <span/> Round {event?.currentRound??0} of {event?.totalRounds??0}</p><div>{live?<button onClick={()=>onNavigate("scoring")}>Open Live Scoring</button>:<button onClick={()=>onNavigate("registration")}>Manage Registration</button>}<button className="secondary" onClick={()=>onNavigate("setup")}>Game Setup</button></div></div>
        </section>
        <div className="workflow-grid">
            <section className={`workflow-section workflow-section--registration ${registration&&!syncHealthy?"needs-attention":""}`}><header><span>Registration</span><button onClick={()=>onNavigate("registration")}>View Registration</button></header><div className="workflow-section__value"><strong>{teams.length}</strong><span>team{teams.length===1?"":"s"}</span></div><p>{registration?.registrationOpen?"Registration open":"Registration closed"}</p><div className="sync-line"><i className={syncHealthy?"healthy":"unhealthy"}/><span>{syncHealthy?"Google sync healthy":"Google sync needs attention"}<small>Last synced: {syncTime(registration?.lastSuccessfulSyncAt)}</small></span></div></section>
            <section className="workflow-section"><header><span>Game</span><button onClick={()=>onNavigate("setup")}>Game Setup</button></header><div className="workflow-section__value"><strong>{event?.currentRound??0}</strong><span>of {event?.totalRounds??0} rounds</span></div><p>{live?"Game in progress":completed?`${completed} rounds completed`:"Not started"}</p></section>
            <section className="workflow-section workflow-section--teams"><header><span>Teams</span><button onClick={()=>onNavigate("teams")}>Manage Teams</button></header>{sorted.length?<ol>{sorted.slice(0,4).map((team,index)=><li key={team.id}><b>{index+1}</b><span><strong>{team.name}</strong><small>{team.members.length} member{team.members.length===1?"":"s"}</small></span><em>{team.score}</em></li>)}</ol>:<p className="workflow-empty">No teams registered yet.</p>}</section>
            <section className="workflow-section workflow-section--activity"><header><span>Recent Activity</span>{live&&<button onClick={()=>onNavigate("scoring")}>Live Scoring</button>}</header>{activity.length?<ul>{activity.slice(0,4).map(item=><li key={item.id}><span><strong>{item.teamName}</strong><small>{item.reason}</small></span><b className={item.amount>=0?"positive":"negative"}>{item.amount>0?"+":""}{item.amount}</b><time>{item.timestamp.toLocaleTimeString("en-ZA",{hour:"2-digit",minute:"2-digit"})}</time></li>)}</ul>:<p className="workflow-empty">No game activity yet.</p>}</section>
        </div>
    </div>;
}
