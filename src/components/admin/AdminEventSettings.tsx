import { useState } from "react";
import "./AdminEventSettings.css";

interface AdminEventSettingsProps {
    registrationDeadline: Date;
    onDeadlineChange: (deadline: Date) => void;
}

function AdminEventSettings({
                                registrationDeadline,
                                onDeadlineChange,
                            }: AdminEventSettingsProps) {
    const [date, setDate] = useState(
        registrationDeadline.toISOString().slice(0, 10),
    );

    const [time, setTime] = useState(
        registrationDeadline.toTimeString().slice(0, 5),
    );

    function handleSave() {
        const newDeadline = new Date(`${date}T${time}:00`);

        onDeadlineChange(newDeadline);
    }

    return (
        <section className="admin-event-settings">
            <h2>Event Settings</h2>

            <div>
                <label htmlFor="registration-date">
                    Registration closing date
                </label>

                <input
                    id="registration-date"
                    type="date"
                    value={date}
                    onChange={(event) => setDate(event.target.value)}
                />
            </div>

            <div>
                <label htmlFor="registration-time">
                    Registration closing time
                </label>

                <input
                    id="registration-time"
                    type="time"
                    value={time}
                    onChange={(event) => setTime(event.target.value)}
                />
            </div>

            <button type="button" onClick={handleSave}>
                Save Changes
            </button>
        </section>
    );
}

export default AdminEventSettings;