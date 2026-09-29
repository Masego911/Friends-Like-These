import { useEffect, useState } from "react"; // Provides state and timer lifecycle support.
import "./Countdown.css"; // Loads countdown styling.

interface CountdownProps {
    deadline: Date; // Date and time when registration closes.
}

interface TimeRemaining {
    days: number;
    hours: number;
    minutes: number;
    seconds: number;
    expired: boolean;
}

function getTimeRemaining(deadline: Date): TimeRemaining {
    const difference = deadline.getTime() - Date.now();

    if (difference <= 0) {
        return {
            days: 0,
            hours: 0,
            minutes: 0,
            seconds: 0,
            expired: true,
        };
    }

    return {
        days: Math.floor(difference / 86_400_000),
        hours: Math.floor((difference % 86_400_000) / 3_600_000),
        minutes: Math.floor((difference % 3_600_000) / 60_000),
        seconds: Math.floor((difference % 60_000) / 1_000),
        expired: false,
    };
}

function Countdown({ deadline }: CountdownProps) {
    const [timeRemaining, setTimeRemaining] = useState<TimeRemaining>(
        getTimeRemaining(deadline),
    );

    useEffect(() => {
        const timer = window.setInterval(() => {
            setTimeRemaining(getTimeRemaining(deadline));
        }, 1000);

        return () => {
            window.clearInterval(timer);
        };
    }, [deadline]);

    const formatNumber = (value: number) =>
        String(value).padStart(2, "0");

    return (
        <section className="countdown">
            <span className="countdown__label">
                Registration closes in
            </span>

            {timeRemaining.expired ? (
                <strong className="countdown__closed">
                    Registration Closed
                </strong>
            ) : (
                <div className="countdown__time">
                    <div className="countdown__unit">
                        <strong>{formatNumber(timeRemaining.days)}</strong>
                        <span>DAYS</span>
                    </div>

                    <span className="countdown__separator">:</span>

                    <div className="countdown__unit">
                        <strong>{formatNumber(timeRemaining.hours)}</strong>
                        <span>HRS</span>
                    </div>

                    <span className="countdown__separator">:</span>

                    <div className="countdown__unit">
                        <strong>{formatNumber(timeRemaining.minutes)}</strong>
                        <span>MINS</span>
                    </div>

                    <span className="countdown__separator">:</span>

                    <div className="countdown__unit">
                        <strong>{formatNumber(timeRemaining.seconds)}</strong>
                        <span>SECS</span>
                    </div>
                </div>
            )}
        </section>
    );
}

export default Countdown;