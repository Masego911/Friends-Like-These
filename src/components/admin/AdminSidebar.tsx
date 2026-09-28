import { useEffect, useRef } from "react";
import AdminIcon from "./AdminIcon";
import campusKey from "../../assets/branding/campuskey.png";
import "./AdminConsole.css";

export type AdminPage = "dashboard" | "scoring" | "teams" | "registration" | "setup" | "history" | "settings";

interface Props {
    activePage: AdminPage;
    adminName: string;
    open: boolean;
    onNavigate: (page: AdminPage) => void;
    onClose: () => void;
    onLogout: () => Promise<void>;
}

const pages: Array<{ id: AdminPage; label: string }> = [
    { id: "dashboard", label: "Dashboard" }, { id: "scoring", label: "Live Scoring" },
    { id: "teams", label: "Teams" }, { id: "registration", label: "Registration" },
    { id: "setup", label: "Game Setup" }, { id: "history", label: "Previous Games" },
    { id: "settings", label: "Settings" },
];

export default function AdminSidebar({ activePage, adminName, open, onNavigate, onClose, onLogout }: Props) {
    const drawer = useRef<HTMLElement>(null);
    useEffect(() => { if (open) drawer.current?.focus(); }, [open]);
    const navigate = (page: AdminPage) => { onNavigate(page); onClose(); };
    return <>
        {open && <button className="admin-console__scrim" aria-label="Close navigation" onClick={onClose} />}
        <aside ref={drawer} tabIndex={-1} aria-label="Administrator menu" className={`admin-sidebar ${open ? "admin-sidebar--open" : ""}`}>
            <header className="admin-sidebar__brand"><strong>Friends Like These</strong><div className="flt-colour-bar" aria-hidden="true"><i/><i/><i/><i/><i/></div><span>Games Night Control Centre</span><img src={campusKey} alt="CampusKey"/></header>
            <nav aria-label="Administrator navigation">
                {pages.map(page => <button key={page.id} type="button" className={activePage === page.id ? "is-active" : ""} aria-current={activePage === page.id ? "page" : undefined} onClick={() => navigate(page.id)}><AdminIcon name={page.id}/>{page.label}</button>)}
            </nav>
            <footer><small>Signed in as</small><strong title={adminName}>{adminName}</strong><button type="button" className="admin-sidebar__logout" onClick={onLogout}>Logout</button></footer>
        </aside>
    </>;
}
