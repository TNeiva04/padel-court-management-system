import {createDefaultOption, createOption} from "../../../DSL.js";
import {fetchCourtsByClub} from "../../fetchers/courtfetchers.js";
import {API_BASE_URL} from "../../../router.js";
import {createReservation} from "../../fetchers/rentalfetchers.js";


export function populateClubSelect(select, clubs) {
    select.appendChild(createDefaultOption("Select a club", true, true));
    const list = Array.isArray(clubs) ? clubs : clubs?.clubs ?? [];
    if (list.length) {
        list.forEach(c => select.appendChild(createOption({attributes: {value: c.cid}, textContent: c.name})));
    } else {
        select.appendChild(createOption({textContent: "No club available", attributes: {disabled: true}}));
    }
}

export function setupClubSelection(clubSelect, courtSelect, dateInput, hourSelect, durationSelect, currentAvailableHours) {
    clubSelect.addEventListener("change", async () => {
        const clubId = clubSelect.value;
        courtSelect.style.display = "block";
        courtSelect.disabled = true;
        courtSelect.innerHTML = "<option>Getting courts…</option>";
        [dateInput, hourSelect, durationSelect].forEach(resetAndHideElement);
        currentAvailableHours.length = 0;

        try {
            const courts = await fetchCourtsByClub(clubId, 100, 0);
            courtSelect.innerHTML = "";
            if (courts.courts?.length) {
                courtSelect.appendChild(createDefaultOption("Select a court", true, true));
                courts.courts.forEach(ct => {
                    courtSelect.appendChild(createOption({attributes: {value: ct.crid}, textContent: ct.name}));
                });
                courtSelect.disabled = false;
            } else {
                courtSelect.appendChild(createOption({
                    textContent: "No court available",
                    attributes: {disabled: true}
                }));
            }
        } catch {
            courtSelect.innerHTML = "<option>Error getting courts</option>";
        }
    });
}

export function setupCourtSelection(courtSelect, dateInput, hourSelect, durationSelect, currentAvailableHours) {
    courtSelect.addEventListener("change", () => {
        [hourSelect, durationSelect].forEach(resetAndHideElement);
        currentAvailableHours.length = 0;
        dateInput.style.display = "block";
    });
}

export function setupDateSelection(dateInput, hourSelect, durationSelect, club, court, clubSelect, courtSelect, currentAvailableHours, isDirectMode) {
    dateInput.addEventListener("change", async () => {
        resetAndHideElement(durationSelect);
        hourSelect.style.display = "block";
        hourSelect.replaceChildren(createOption({textContent: "Getting available hours…"}));

        const date = dateInput.value;
        console.log(date);
        if (!date) return resetAndHideElement(hourSelect);

        const clubId = isDirectMode ? club.cid : clubSelect.value;
        const courtId = isDirectMode ? court.crid : courtSelect.value;
        if (!clubId || !courtId) return;

        try {
            const res = await fetch(`${API_BASE_URL}clubs/${clubId}/courts/${courtId}/availablehours?date=${date}T00:00:00`);
            const {hours} = await res.json();
            currentAvailableHours.length = 0;
            currentAvailableHours.push(...(hours || []));
            populateHourSelect(hourSelect, currentAvailableHours);
        } catch {
            hourSelect.replaceChildren(createOption({
                textContent: "Error loading hours",
                attributes: {disabled: "true"}
            }));
        }
    });
}

export function setupHourSelection(hourSelect, durationSelect, submitButton, currentAvailableHours) {
    hourSelect.addEventListener("change", () => {
        const selectedHour = hourSelect.value;
        const max = calculateMaxDuration(selectedHour, currentAvailableHours);

        if (max === 0) {
            resetAndHideElement(durationSelect);
            submitButton.style.display = "none";
        } else {
            populateDurationSelect(durationSelect, max);
            durationSelect.style.display = "block";
            submitButton.style.display = "inline-block";
        }
    });
}

export async function handleFormSubmit(
    e, form, club, court, clubSelect, courtSelect,
    dateInput, hourSelect, durationSelect,
    isDirectMode, onCreated
) {
    e.preventDefault();
    if (form.dataset.submitting === "true") return;

    form.dataset.submitting = "true";

    const clubId = isDirectMode ? club.cid : clubSelect.value;
    const courtId = isDirectMode ? court.crid : courtSelect.value;
    const date = dateInput.value;
    const start = hourSelect.value;
    const duration = durationSelect.value;

    if (!clubId || !courtId || !date || !start || !duration) {
        alert("You need to select a club, court, date, hour and duration!");
        form.dataset.submitting = "false";
        return;
    }

    const [hours, minutes] = start.split(":");
    const dateTimeString = `${date}T${hours}:${minutes}:00`;

    const data = {
        date: dateTimeString,
        Duration: {
            duration: parseInt(duration)
        }
    };
    console.log(data);
    try {
        const result = await createReservation(clubId, courtId, data);

        if (!result || result.error || result.success === false) {
            alert(`Error: ${result.message || result.error || "Error creating reservation."}`);
        } else {
            alert("Your reservation was created successfully!");
            if (onCreated) onCreated(result);
            window.location.href = `#rentals/${result.rid}`;
        }

    } catch (err) {
        alert(`Error submitting: ${err.message}`);
    } finally {
        form.dataset.submitting = "false";
    }
}


function resetAndHideElement(el) {
    el.style.display = "none";
    el.value = "";
    el.innerHTML = "";
}


function populateHourSelect(select, hours, placeholder = "Select an hour") {
    select.innerHTML = "";
    const defaultOption = createOption({
        textContent: placeholder,
        attributes: {
            disabled: true,
            selected: true,
            value: ""
        }
    });
    select.appendChild(defaultOption);
    if (hours.length === 0) {
        select.appendChild(createOption({
            textContent: "No hours available",
            attributes: {disabled: true}
        }));
    } else {
        hours.forEach(hour => {
            select.appendChild(createOption({
                value: hour,
                textContent: hour
            }));
        });
    }
}


function calculateMaxDuration(selectedHour, availableHours) {
    const index = availableHours.indexOf(selectedHour);
    if (index === -1) return 0;

    let maxDuration = 1;
    for (let i = index + 1; i < availableHours.length; i++) {
        const expected = parseInt(selectedHour) + maxDuration;
        if (parseInt(availableHours[i]) === expected) {
            maxDuration++;
        } else {
            break;
        }
    }

    return maxDuration;
}

function populateDurationSelect(select, maxDuration, labelSingular = "hour", labelPlural = "hours") {
    select.innerHTML = "";
    const defaultOption = createOption({
        textContent: "Select a duration",
        attributes: {
            disabled: true,
            selected: true,
            value: ""
        }
    });

    select.appendChild(defaultOption);

    for (let i = 1; i <= maxDuration; i++) {
        select.appendChild(createOption({
            value: i,
            textContent: `${i} ${i > 1 ? labelPlural : labelSingular}`
        }));
    }
}
