import "./AppHeader.css";

interface AppHeaderProps {
    currentView: "scoreboard" | "admin";
    onViewChange: (view: "scoreboard" | "admin") => void;
}

function AppHeader({
                       currentView,
                       onViewChange,
                   }: AppHeaderProps) {
    return (
        <header className="app-header">
            <div className="app-header__branding">

                <div className="app-header__friends">
                    <span className="app-header__friends-name">
                        Friends Like These
                    </span>

                    <div className="app-header__colour-bar" aria-hidden="true">
                        <span className="app-header__colour app-header__colour--orange" />
                        <span className="app-header__colour app-header__colour--red" />
                        <span className="app-header__colour app-header__colour--purple" />
                        <span className="app-header__colour app-header__colour--blue" />
                        <span className="app-header__colour app-header__colour--teal" />
                    </div>
                </div>

                <div className="app-header__campuskey">
                    <span className="app-header__campuskey-name">
                        CampusKey
                    </span>

                    <span className="app-header__campuskey-edition">
                        EDITION
                    </span>
                </div>

            </div>

            <nav className="app-header__nav" aria-label="Main navigation">
                <button
                    className={
                        currentView === "scoreboard"
                            ? "app-header__scoreboard-button app-header__button--active"
                            : "app-header__scoreboard-button"
                    }
                    type="button"
                    onClick={() => onViewChange("scoreboard")}
                >
                    Scoreboard
                </button>

                <button
                    className="app-header__admin-button"
                    type="button"
                    onClick={() => onViewChange("admin")}
                >
                    Admin
                </button>
            </nav>
        </header>
    );
}

export default AppHeader;