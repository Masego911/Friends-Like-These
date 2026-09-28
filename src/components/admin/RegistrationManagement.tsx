import { useState } from "react";
import type { RegistrationPreviewResponse, RegistrationStatus } from "../../api/registrationsApi";
import { previewRegistrations } from "../../api/registrationsApi";
import "./RegistrationManagement.css";

interface Props { status: RegistrationStatus | null; onSyncNow: () => Promise<void>; }
const when = (value: string | null | undefined) => value === undefined ? "Loading…" : value ? new Date(value).toLocaleString() : "Never";

export default function RegistrationManagement({ status, onSyncNow }: Props) {
    const [preview,setPreview] = useState<RegistrationPreviewResponse|null>(null), [previewError,setPreviewError] = useState(""), [syncing,setSyncing] = useState(false);
    const showPreview=async()=>{try{setPreview(await previewRegistrations());setPreviewError("");}catch(error){setPreviewError(error instanceof Error?error.message:"Preview unavailable");}};
    const sync=async()=>{setSyncing(true);try{await onSyncNow();}finally{setSyncing(false);}};
    const metrics = [["Responses detected",status?.rowsRead??"—"],["New teams",status?.teamsCreated??"—"],["Updated teams",status?.teamsUpdated??"—"],["Unchanged teams",status?.teamsUnchanged??"—"],["Skipped rows",status?.rowsSkipped??"—"],["Conflicts",status?.conflicts??"—"]];
    return <section className="registration-management" aria-labelledby="registration-management-title">
        <div className="registration-management__heading"><div><span>Registration Management</span><h2 id="registration-management-title">Google Form sync</h2></div><div className="registration-management__badges"><b className={status?.automaticSyncEnabled?"ok":"off"}>Automatic Sync: {status?status.automaticSyncEnabled?"ON":"OFF":"CHECKING"}</b><b className={status?.registrationOpen?"ok":"off"}>Registration {status?status.registrationOpen?"OPEN":"CLOSED":"CHECKING"}</b><b className={status?.googleIntegrationAvailable?"ok":"off"}>Google integration {status?status.googleIntegrationAvailable?"AVAILABLE":"UNAVAILABLE":"CHECKING"}</b></div></div>
        <dl className="registration-management__timing"><div><dt>Registration deadline</dt><dd>{when(status?.registrationDeadline)}</dd></div><div><dt>Last Sync</dt><dd>{when(status?.lastSyncAt)}</dd></div><div><dt>Last Successful Sync</dt><dd>{when(status?.lastSuccessfulSyncAt)}</dd></div><div><dt>Next expected sync</dt><dd>{when(status?.nextExpectedSyncAt)}</dd></div><div><dt>Interval</dt><dd>{status?.syncIntervalSeconds??60} seconds</dd></div></dl>
        <div className="registration-management__metrics">{metrics.map(([label,value])=><div key={label}><span>{label}</span><strong>{value}</strong></div>)}</div>
        <p className="registration-management__message">{status?.lastSyncMessage??"Loading sync status…"}</p>
        <div className="registration-management__actions"><button type="button" onClick={showPreview}>Preview Registrations</button><button type="button" disabled={syncing} onClick={sync}>{syncing?"Syncing…":"Sync Now"}</button>{status?.registrationUrl&&<a href={status.registrationUrl} target="_blank" rel="noreferrer">Open Google Form</a>}{status?.responseSheetUrl&&<a href={status.responseSheetUrl} target="_blank" rel="noreferrer">Open Response Sheet</a>}</div>
        {previewError&&<p role="alert">{previewError}</p>}{preview&&<div className="registration-management__preview"><h3>Read-only preview</h3><p>{preview.validRegistrations} valid responses · {preview.newTeams} new · {preview.existingTeams} existing · {preview.conflicts} conflicts · {preview.skippedRows} skipped</p><ul>{preview.registrations.map((row,index)=><li key={`${row.teamName}-${index}`}><strong>{row.teamName}</strong><span>{row.members.join(", ")||"No members"}</span><em>{row.status.replaceAll("_"," ")}</em></li>)}</ul></div>}
    </section>;
}
