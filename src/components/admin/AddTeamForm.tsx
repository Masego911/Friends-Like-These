import { useState } from "react";
import type { Team } from "../../models/Team";
import "./AddTeamForm.css";

interface AddTeamFormProps {
    existingTeams: Team[];
    onAddTeam: (team: Team) => void;
    onCancel: () => void;
}

const MAX_MEMBERS = 6;

function AddTeamForm({
                         existingTeams,
                         onAddTeam,
                         onCancel,
                     }: AddTeamFormProps) {
    const [teamName, setTeamName] = useState("");
    const [members, setMembers] = useState<string[]>([""]);
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
            (team) =>
                team.name.toLowerCase() === cleanedName.toLowerCase(),
        );

        if (duplicateTeam) {
            setError("A team with this name already exists.");
            return;
        }

        const newTeam: Team = {
            id: crypto.randomUUID(),
            name: cleanedName,
            members: cleanedMembers,
            score: 0,
        };

        setError("");
        onAddTeam(newTeam);
    }

    return (
        <form
            className="add-team-form"
            onSubmit={handleSubmit}
        >
            <header className="add-team-form__header">
                <div>
                    <span className="add-team-form__eyebrow">
                        Teams
                    </span>

                    <h2>Add Team</h2>
                </div>
            </header>

            {error && (
                <p
                    className="add-team-form__error"
                    role="alert"
                >
                    {error}
                </p>
            )}

            <div className="add-team-form__field">
                <label htmlFor="team-name">
                    Team name
                </label>

                <input
                    id="team-name"
                    type="text"
                    value={teamName}
                    onChange={(event) =>
                        setTeamName(event.target.value)
                    }
                    placeholder="Enter team name"
                />
            </div>

            <div className="add-team-form__members">
                <div className="add-team-form__members-header">
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
                            handleMemberChange(
                                index,
                                event.target.value,
                            )
                        }
                        placeholder={`Member ${index + 1}`}
                    />
                ))}

                <span className="add-team-form__member-count">
                    {members.length} / {MAX_MEMBERS} member fields
                </span>
            </div>

            <div className="add-team-form__actions">
                <button
                    className="add-team-form__cancel"
                    type="button"
                    onClick={onCancel}
                >
                    Cancel
                </button>

                <button
                    className="add-team-form__save"
                    type="submit"
                >
                    Add Team
                </button>
            </div>
        </form>
    );
}

export default AddTeamForm;