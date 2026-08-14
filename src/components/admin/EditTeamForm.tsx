import { useState } from "react";
import type { Team } from "../../models/Team";
import "./EditTeamForm.css";

interface EditTeamFormProps {
    team: Team;
    existingTeams: Team[];
    onSave: (team: Team) => void;
    onCancel: () => void;
}

const MAX_MEMBERS = 6;

function EditTeamForm({
                          team,
                          existingTeams,
                          onSave,
                          onCancel,
                      }: EditTeamFormProps) {
    const [teamName, setTeamName] = useState(team.name);
    const [members, setMembers] = useState<string[]>([...team.members]);
    const [error, setError] = useState("");

    function handleMemberChange(index: number, value: string) {
        setMembers((currentMembers) =>
            currentMembers.map((member, memberIndex) =>
                memberIndex === index ? value : member,
            ),
        );
    }

    function handleAddMemberField() {
        if (members.length >= MAX_MEMBERS) {
            return;
        }

        setMembers((currentMembers) => [...currentMembers, ""]);
    }

    function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
        event.preventDefault();

        const cleanedName = teamName.trim();

        const cleanedMembers = members
            .map((member) => member.trim())
            .filter((member) => member.length > 0);

        if (!cleanedName) {
            setError("Enter a team name.");
            return;
        }

        if (cleanedMembers.length === 0) {
            setError("Add at least one team member.");
            return;
        }

        const duplicateTeam = existingTeams.some(
            (existingTeam) =>
                existingTeam.id !== team.id &&
                existingTeam.name.toLowerCase() === cleanedName.toLowerCase(),
        );

        if (duplicateTeam) {
            setError("A team with this name already exists.");
            return;
        }

        const updatedTeam: Team = {
            ...team,
            name: cleanedName,
            members: cleanedMembers,
        };

        setError("");
        onSave(updatedTeam);
    }

    return (
        <form
            className="edit-team-form"
            onSubmit={handleSubmit}
        >
            <header className="edit-team-form__header">
                <div>
                    <span className="edit-team-form__eyebrow">
                        Teams
                    </span>

                    <h2>Edit Team</h2>
                </div>
            </header>

            {error && (
                <p
                    className="edit-team-form__error"
                    role="alert"
                >
                    {error}
                </p>
            )}

            <div className="edit-team-form__field">
                <label htmlFor="edit-team-name">
                    Team name
                </label>

                <input
                    id="edit-team-name"
                    type="text"
                    value={teamName}
                    onChange={(event) =>
                        setTeamName(event.target.value)
                    }
                />
            </div>

            <div className="edit-team-form__members">
                <div className="edit-team-form__members-header">
                    <label>
                        Team members
                    </label>

                    <button
                        type="button"
                        onClick={handleAddMemberField}
                        disabled={members.length >= MAX_MEMBERS}
                    >
                        Add Member
                    </button>
                </div>

                {members.map((member, index) => (
                    <input
                        key={index}
                        type="text"
                        value={member}
                        onChange={(event) =>
                            handleMemberChange(index, event.target.value)
                        }
                        placeholder={`Member ${index + 1}`}
                    />
                ))}

                <span className="edit-team-form__member-count">
                    {members.length} / {MAX_MEMBERS} member fields
                </span>
            </div>

            <div className="edit-team-form__actions">
                <button
                    className="edit-team-form__cancel"
                    type="button"
                    onClick={onCancel}
                >
                    Cancel
                </button>

                <button
                    className="edit-team-form__save"
                    type="submit"
                >
                    Save Changes
                </button>
            </div>
        </form>
    );
}

export default EditTeamForm;