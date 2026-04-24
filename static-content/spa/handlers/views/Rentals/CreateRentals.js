import {
    createButton,
    createForm,
    createInput,
    createSelect,
    createDiv,
    createH2, createP, createH3
} from "../../../DSL.js";
import {
    handleFormSubmit, populateClubSelect,
    setupClubSelection,
    setupCourtSelection,
    setupDateSelection,
    setupHourSelection
} from "./Auxiliares.js";

export async function createReservationForm({clubs = null, club = null, court = null, onCreated = null}) {
    const isDirectMode = club && court;
    const currentAvailableHours = [];

    const container = createDiv({className: "home-container d-flex flex-column align-items-center justify-content-center p-5"});
    const title = isDirectMode
        ? createH2({textContent: `Rent the Court "${court.name}" of Club "${club.name}"`})
        : createH3({textContent: "Do you want to rent a court?"});
    const description = !isDirectMode ? createP({
        className: "text-center",
        html: "Choose a club and a court!<br>Get the best available hours and book your next match!"
    }) : null;

    const form = createForm({className: "d-flex flex-column align-items-center gap-3"});
    const submitButton = createButton({
        className: "btn btn-primary uniform-input",
        textContent: "Book",
        attributes: {type: "submit"}
    });
    submitButton.style.display = "none";

    const clubSelect = createSelect({className: "form-select form-select-sm uniform-input"});
    const courtSelect = createSelect({
        className: "form-select form-select-sm uniform-input",
        attributes: {disabled: true}
    });
    const dateInput = createInput({
        className: "form-control uniform-input",
        attributes: {type: "date", required: true}
    });
    const hourSelect = createSelect({
        className: "form-select form-select-sm uniform-input",
        attributes: {required: true}
    });
    const durationSelect = createSelect({
        className: "form-select form-select-sm uniform-input",
        attributes: {required: true}
    });

    [courtSelect, dateInput, hourSelect, durationSelect].forEach(el => el.style.display = "none");

    if (!isDirectMode) {
        populateClubSelect(clubSelect, clubs);
        setupClubSelection(clubSelect, courtSelect, dateInput, hourSelect, durationSelect, currentAvailableHours);
        setupCourtSelection(courtSelect, dateInput, hourSelect, durationSelect, currentAvailableHours);

        const row = createDiv({
            className: "d-flex gap-2 flex-wrap justify-content-center w-100",
            children: [clubSelect, courtSelect, dateInput, hourSelect, durationSelect]
        });
        form.appendChild(row);
    } else {
        form.append(dateInput, hourSelect, durationSelect);
        dateInput.style.display = "block";
    }

    setupDateSelection(dateInput, hourSelect, durationSelect, club, court, clubSelect, courtSelect, currentAvailableHours, isDirectMode);
    setupHourSelection(hourSelect, durationSelect, submitButton, currentAvailableHours);

    form.addEventListener("submit", (e) => handleFormSubmit(e, form, club, court, clubSelect, courtSelect, dateInput, hourSelect, durationSelect, isDirectMode, onCreated));

    form.appendChild(submitButton);
    container.append(title, ...(description ? [description] : []), form);
    return container;
}



