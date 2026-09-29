import "./RegistrationQR.css";

interface RegistrationQRProps {
    formUrl: string; // Registration link used by both the button and QR image.
}

function RegistrationQR({ formUrl }: RegistrationQRProps) {
    const qrUrl =
        `https://api.qrserver.com/v1/create-qr-code/?size=220x220&data=${encodeURIComponent(formUrl)}`;

    return (
        <section className="registration-qr">
            <div>
                <span className="registration-qr__eyebrow">
                    Not registered yet?
                </span>

                <h2 className="registration-qr__title">
                    Join the game
                </h2>

                <p className="registration-qr__text">
                    Scan the QR code to register your team.
                </p>
            </div>

            <a
                className="registration-qr__link"
                href={formUrl}
                target="_blank"
                rel="noreferrer"
            >
                <img
                    className="registration-qr__image"
                    src={qrUrl}
                    alt="QR code for Friends Like These team registration"
                />
            </a>
        </section>
    );
}

export default RegistrationQR;