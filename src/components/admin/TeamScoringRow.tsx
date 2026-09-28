import { useState } from "react";
import type { Team } from "../../models/Team";
import type { scoreEvent } from "../../models/scoreEvent";
import MedalBadge from "../team/MedalBadge";

interface Props { team:Team; position:number; latest?:scoreEvent; pending:boolean; scoringEnabled:boolean; expanded:boolean; onToggle:()=>void; onScore:(amount:number,reason:string)=>Promise<void>; }

export default function TeamScoringRow({team,position,latest,pending,scoringEnabled,expanded,onToggle,onScore}:Props){
    const [custom,setCustom]=useState(""),[validation,setValidation]=useState("");
    const submit=async(direction:1|-1)=>{const points=Number(custom);if(!Number.isInteger(points)||points<=0||points>100000){setValidation("Enter a positive whole number.");return;}setValidation("");await onScore(direction*points,`Custom ${direction>0?"award":"deduction"}: ${points} points`);setCustom("");};
    return <article className={`team-scoring-row ${expanded?"is-expanded":""}`} aria-busy={pending}>
        <div className="team-scoring-row__main">
            <div className={`team-scoring-row__rank ${position<=3?"is-medal":""}`}>{position<=3?<MedalBadge rank={position}/>:<span>#{position}</span>}</div>
            <div className="team-scoring-row__team"><h3>{team.name}</h3><small>{team.members.length} member{team.members.length===1?"":"s"}</small></div>
            <div className="team-scoring-row__members">{team.members.length}</div>
            <div className="team-scoring-row__score"><strong>{team.score}</strong><span>pts</span></div>
            <div className="team-scoring-row__quick" aria-label={`Quick scoring for ${team.name}`}>{[-10,-5,-1,1,5,10].map(amount=><button key={amount} type="button" className={amount<0?"negative":"positive"} disabled={pending||!scoringEnabled} onClick={()=>onScore(amount,`${Math.abs(amount)} points ${amount>0?"awarded":"deducted"}`)}>{amount>0?"+":""}{amount}</button>)}</div>
            <button type="button" className="team-scoring-row__more" aria-expanded={expanded} onClick={onToggle}>{expanded?"Less":"More"}<span aria-hidden="true">⌄</span></button>
        </div>
        {expanded&&<div className="team-scoring-row__details"><div className="team-scoring-row__custom"><label htmlFor={`custom-${team.id}`}>Custom score</label><div><input id={`custom-${team.id}`} type="number" inputMode="numeric" min="1" step="1" placeholder="Points" value={custom} disabled={pending||!scoringEnabled} onChange={event=>setCustom(event.target.value)}/><button type="button" disabled={pending||!scoringEnabled} onClick={()=>submit(1)}>Add</button><button type="button" className="deduct" disabled={pending||!scoringEnabled} onClick={()=>submit(-1)}>Deduct</button></div>{validation&&<small role="alert">{validation}</small>}</div><div className="team-scoring-row__latest"><span>Latest activity</span>{latest?<><strong className={latest.amount>=0?"positive":"negative"}>{latest.amount>0?"+":""}{latest.amount}</strong><p>{latest.reason}</p></>:<p>No score activity yet.</p>}</div></div>}
    </article>;
}
