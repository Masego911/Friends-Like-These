interface MedalBadgeProps { // Defines the rank value required by the medal.
    rank: number; // Stores the leaderboard position.
}

function MedalBadge({ rank }: MedalBadgeProps) { // Creates the medal component.
    const isGold = rank === 1; // Checks whether this is first place.
    const isSilver = rank === 2; // Checks whether this is second place.


    const light = isGold ? "#FFF3A3" : isSilver ? "#FFFFFF" : "#FFD2A6"; // Sets the brightest metallic colour.
    const main = isGold ? "#F9C51C" : isSilver ? "#D7DCE3" : "#D9793F"; // Sets the main medal colour.
    const dark = isGold ? "#C88B00" : isSilver ? "#929AA5" : "#A74920"; // Sets the darker metallic colour.
    const deep = isGold ? "#8A5E00" : isSilver ? "#626A75" : "#713017"; // Sets the deepest shadow colour.

    return ( // Returns the medal SVG.
        <svg className="medal-badge" viewBox="0 0 140 140" role="img" aria-label={`Rank ${rank}`}> {/* Creates the scalable medal canvas. */}
            <defs> {/* Stores SVG gradients and filters. */}
                <linearGradient id={`outer-${rank}`} x1="0" y1="0" x2="1" y2="1"> {/* Creates the outer metallic gradient. */}
                    <stop offset="0%" stopColor={light} /> {/* Adds the top highlight. */}
                    <stop offset="45%" stopColor={main} /> {/* Adds the main metallic colour. */}
                    <stop offset="100%" stopColor={dark} /> {/* Adds the lower shadow. */}
                </linearGradient>

                <radialGradient id={`inner-${rank}`} cx="35%" cy="28%" r="75%"> {/* Creates the raised inner disc. */}
                    <stop offset="0%" stopColor="#FFFFFF" stopOpacity="0.9" /> {/* Adds the strongest reflection. */}
                    <stop offset="25%" stopColor={light} /> {/* Blends into the medal highlight. */}
                    <stop offset="70%" stopColor={main} /> {/* Adds the main centre colour. */}
                    <stop offset="100%" stopColor={dark} /> {/* Darkens the outer centre edge. */}
                </radialGradient>

                <filter id={`shadow-${rank}`} x="-30%" y="-30%" width="160%" height="160%"> {/* Creates depth around the medal. */}
                    <feDropShadow dx="0" dy="5" stdDeviation="4" floodColor="#0F172A" floodOpacity="0.28" /> {/* Adds a soft shadow beneath the medal. */}
                </filter>
            </defs>

            <g filter={`url(#shadow-${rank})`}> {/* Applies the drop shadow to the full medal. */}
                <polygon points="70,4 81,21 102,17 103,38 124,42 114,61 132,74 114,87 124,106 103,110 102,131 81,127 70,136 59,127 38,131 37,110 16,106 26,87 8,74 26,61 16,42 37,38 38,17 59,21" fill={`url(#outer-${rank})`} /> {/* Draws the decorative starburst backing. */}

                <circle cx="70" cy="72" r="49" fill={deep} /> {/* Creates the darkest outer rim. */}
                <circle cx="70" cy="69" r="47" fill={`url(#outer-${rank})`} /> {/* Creates the main outer medal ring. */}
                <circle cx="70" cy="69" r="39" fill={dark} opacity="0.55" /> {/* Adds recessed depth between the rings. */}
                <circle cx="70" cy="67" r="35" fill={`url(#inner-${rank})`} /> {/* Creates the raised metallic centre. */}

                <ellipse cx="56" cy="49" rx="18" ry="9" fill="#FFFFFF" opacity="0.28" transform="rotate(-25 56 49)" /> {/* Adds a glossy highlight across the medal face. */}

                <path d="M36 76 A35 35 0 0 0 104 76" fill="none" stroke={deep} strokeWidth="3" opacity="0.35" /> {/* Adds a subtle lower curved shadow. */}

                <text x="70" y="69" textAnchor="middle" dominantBaseline="middle" className="medal-badge__number"> {/* Centres the rank number. */}
                    {rank} {/* Displays the leaderboard rank. */}
                </text>

                {isGold && <path d="M70 20 L74 29 L84 30 L76 37 L78 47 L70 42 L62 47 L64 37 L56 30 L66 29 Z" fill="#FFF7C2" stroke="#D89A00" strokeWidth="1.5" />} {/* Adds an extra star to the gold medal. */}
            </g> {/* Ends the medal group. */}
        </svg> // Ends the SVG.
    ); // Ends the return statement.
} // Ends the MedalBadge component.

export default MedalBadge; // Makes the medal available to TeamCard.