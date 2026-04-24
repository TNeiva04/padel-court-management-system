import {API_BASE_URL} from "../router.js";
import {currentUser, getCurrentToken, getCurrentUser} from "./session.js";
import {
    createForm,
    createDiv,
    createH1,
    createButton,
    createP,
    createA,
    createUl,
    createLi,
    createSelect,
    createInput,
    createH2,
    onClick, createDefaultOption, createOption
} from "../DSL.js";
import {
    createReservation,
    fetchAvailableHours,
    fetchRentalDetails,
    fetchRentals, fetchRentalsByClubCourtDate,
    fetchRentalsById,
    fetchRentalsByUser, updateRental
} from "./fetchers/rentalfetchers.js";
import {renderRentals} from "./views/Rentals/renderRentals.js";
import {getUserByToken} from "./fetchers/userfetcher.js";
import {setupDateSelection, setupHourSelection} from "./views/Rentals/Auxiliares.js";
import {createPaginationControls} from "./views/buttons.js";

function getRentals(mainContent) {
    const pageSize = 5;
    let currentPage = 0;

    async function fetchAndRender() {
        const skip = currentPage * pageSize;

        const rentals = await fetchRentals(pageSize, skip);
        console.log(rentals);
        const nextRentals = await fetchRentals(pageSize, skip + pageSize);
        const isLastPage = nextRentals.length === 0;

        if (rentals.length === 0 && currentPage > 0) {
            currentPage--;
            return fetchAndRender();
        }

        renderRentals(
            mainContent,
            "My Rentals",
            rentals,
            currentPage,
            isLastPage,
            () => {
                if (currentPage > 0) {
                    currentPage--;
                   return fetchAndRender();
                }
            },
            () => {
                currentPage++;
                return fetchAndRender();
            }
        );
    }
    return fetchAndRender();
}

async function getRentalsByUser(mainContent) {
    console.log("getRentalsByUser called",currentUser);

    const uid = await getUserByToken(currentUser);
    const pageSize = 2;
    let currentPage = 0;

    async function fetchAndRender() {
        const skip = currentPage * pageSize;

        try {
            const rentals = await fetchRentalsByUser(uid.uid, pageSize, skip);
            const nextRentals = await fetchRentalsByUser(uid.uid, pageSize, skip + pageSize);
            const isLastPage = nextRentals.length === 0;

            if (rentals.length === 0 && currentPage > 0) {
                currentPage--;
                return fetchAndRender();
            }

            const backLink = "uid";

            renderRentals(
                mainContent,
                "My Rentals",
                rentals,
                currentPage,
                isLastPage,
                () => {
                    if (currentPage > 0) {
                        currentPage--;
                        fetchAndRender();
                    }
                },
                () => {
                    currentPage++;
                    fetchAndRender();
                },
                fetchAndRender,
                backLink,
                uid.uid
            );
        } catch (err) {
            mainContent.textContent = "Error loading rentals.";
            console.error(err);
        }
    }

    fetchAndRender();
}

async function getRentalDetails(mainContent, rid) {
    const linksContainer = createDiv({className: "links-container"});
    try {
        const rental = await fetchRentalDetails(rid);
        let container = createDiv({className: "rentaldetails-container"});
        const ul = createUl();
        const fields = [
            `Rental: ${rental.rid}`,
            `Start Date: ${new Date(rental.datein).toLocaleString()}`,
            `Duration: ${rental.duration} hour(s)`,
            `Court: ${rental.crid.name}`,
            `Club: ${rental.crid.club.name}`,
            `Booked by: ${rental.uid.name} (${rental.uid.email.email})`
        ];
        fields.forEach(text => {
            ul.appendChild(createLi({textContent: text}));
        });

        // Link to see available hours
        const availableHoursLink = createA({
            textContent: "See available hours on this day",
            attributes: {
                href: `#clubs/${rental.crid.club.cid}/courts/${rental.crid.crid}/availablehours?date=${rental.datein.replace(" ", "T").split(".")[0]}`
            }
        });
        ul.appendChild(createLi({children: [availableHoursLink]}));
        container.appendChild(ul);
        if(currentUser != null) {
            let user = await getUserByToken(currentUser);

            // List to group buttons
            const actionsList = createUl({
                className: "reservation-actions",
                attributes: {
                    style: "list-style: none; padding: 0; margin: 0;"
                }
            });

            const deleteReservationLink = createA({
                href: "#",
                textContent: "Cancel reservation",
                className: "delete-reservation-link",
                ...onClick(async (e) => {
                    e.preventDefault();

                    if (!confirm("Are you sure you want to cancel your reservation?")) return;

                    try {
                        const reservationRes = await fetch(`${API_BASE_URL}rentals/${rid}`, {
                            method: "DELETE",
                            headers: {
                                "Authorization": `Bearer ${currentUser}`,
                                "Content-Type": "application/json"
                            },
                        });

                        if (reservationRes.ok) {
                            alert("Your reservation has been cancelled successfully!");
                            window.location.hash = "#home";
                        } else {
                            const result = await reservationRes.json();
                            alert(`Error cancelling your rent: ${result.error || "Try again later."}`);
                        }
                    } catch (error) {
                        console.error("Error cancelling your rent:", error);
                        alert("Error cancelling your rent. Try again later.");
                    }
                })
            });

            const editReservationLink = createA({
                attributes: {href: `#editrental/${rid}`},
                textContent: "Edit reservation",
                className: "edit-reservation-link"
            });

            const deleteLi = createLi({children: [deleteReservationLink]});
            const editLi = createLi({children: [editReservationLink]});

            const ul3 = createA({
                                attributes: {href: `#rentalsbyuser/${user.uid}`},
                                textContent: "← Back to my rentals",
                                className: "back-link"
            });
            actionsList.appendChild(deleteLi);
            actionsList.appendChild(editLi);

            container.appendChild(actionsList);

            linksContainer.appendChild(ul3);
            container.appendChild(linksContainer);

        }

        // Navigation links
        const ul2 = createA({
                    attributes: {
                        href:
                            `#clubs/${rental.crid.club.cid}/courts/${rental.crid.crid}/rentals?date=${rental.datein.replace(" ", "T").split(".")[0]}`
                    },
                    textContent: "← Back to rentals of this day",
                    className: "back-link"
        });

        linksContainer.appendChild(ul2);
        container.appendChild(linksContainer);
        mainContent.replaceChildren(container);
    } catch (err) {
        mainContent.textContent = "Error getting rental details.";
        console.error("Error getting rental details:", err);
    }
}

export async function EditReservationForm(mainContent, rid) {
    try {
        console.log("22",rid)
        const rental = await fetchRentalsById(rid);
        console.log("Rental object keys count:", Object.keys(rental).length);

        console.log("22",rental)
        const currentAvailableHours = [];

        const container = createDiv({
            className: "home-container d-flex flex-column align-items-center justify-content-center p-5"
        });

        const title = createH2({
            textContent: `Edit rent ${rental.rid} - ${rental.crid.name} (${rental.crid.club.name})`
        });

        const form = createForm({
            className: "d-flex flex-column align-items-center gap-3"
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

        const submitButton = createButton({
            className: "btn btn-primary uniform-input",
            textContent: "Change Reservation",
            attributes: {type: "submit"}
        });
        submitButton.style.display = "none";

        // Initial state
        dateInput.style.display = "block";
        hourSelect.style.display = "none";
        durationSelect.style.display = "none";

        form.append(dateInput, hourSelect, durationSelect, submitButton);
        container.append(title, form);
        mainContent.replaceChildren(container);

        // Setup fields
        await setupDateSelection(
            dateInput,
            hourSelect,
            durationSelect,
            rental.crid.club,
            rental.crid,
            null,
            null,
            currentAvailableHours,
            true
        );

        setupHourSelection(hourSelect, durationSelect, submitButton, currentAvailableHours);

        form.addEventListener("submit", async (e) => {
            e.preventDefault();

            const selectedDate = dateInput.value;
            const selectedStart = hourSelect.value;
            const selectedDuration = durationSelect.value;

            if (!selectedDate || !selectedStart || !selectedDuration) {
                alert("You need to select a date, hour and duration!");
                return;
            }

            const [hours, minutes] = selectedStart.split(":");
            const dateTimeString = `${selectedDate}T${hours}:${minutes}:00`;

            const data = {
                date: dateTimeString,
                Duration: {
                    duration: parseInt(selectedDuration)
                }
            };

            try {
                await updateRental(rid, data);
                alert("Your reservation has been updated successfully!");
                window.location.hash = `#rentals/${rid}`;
            } catch (updateErr) {
                console.error("Error updating reservation:", updateErr);
                mainContent.textContent = "Error updating reservation.";
            }
        });

    } catch (err) {
        console.error("Error fetching rental data:", err);
        mainContent.textContent = "Error loading rental data.";
    }
}

function getRentalsByClubCourtDate(mainContent, cid, crid, date) {
    const formatted = date.split("T")[0];
    const pageSize = 2;
    let currentPage = 0;

    async function fetchAndRender() {
        const skip = currentPage * pageSize;

        const rentals = await fetchRentalsByClubCourtDate(cid, crid, date, pageSize, skip);
        const nextRentals = await fetchRentalsByClubCourtDate(cid, crid, date, pageSize, skip + pageSize);
        const isLastPage = nextRentals.length === 0;

        if (rentals.length === 0 && currentPage > 0) {
            currentPage--;
            return fetchAndRender();
        }

        const onPrev = () => {
            if (currentPage > 0) {
                currentPage--;
                fetchAndRender();
            }
        };

        const onNext = () => {
            if (!isLastPage) {
                currentPage++;
                fetchAndRender();
            }
        };

        const H1 = `Rentals on ${formatted} (Court ${crid}, Club ${cid})`;

        renderRentals(mainContent, H1, rentals, currentPage, isLastPage, onPrev, onNext, fetchAndRender, "crid", crid);
    }

    fetchAndRender();
}

function getAvailableHours(mainContent, cid, crid, date) {
    const pageSize = 1;
    let currentPage = 0;
    let currentAvailableHours = [];

    async function fetchAndRender() {
        const skip = currentPage * pageSize;
        const formattedDate = date.split("T")[0];
        const data = await fetchAvailableHours(cid, crid, date, pageSize, skip);
        if (!data.hours) {
            mainContent.textContent = "There are no available hours for this date.";
            return;
        }

        currentAvailableHours = currentAvailableHours.concat(data.hours);

        const nextData = await fetchAvailableHours(cid, crid, date, pageSize, skip + pageSize);
        const isLastPage = nextData.hours.length === 0;

        const div = createDiv({className: "rentaldetails-container"});
        div.appendChild(createH1({textContent: `Available Hours on ${formattedDate} (Court ${crid}, Club ${cid})`}));

        if (data.hours.length === 0) {
            if (currentPage > 0) {
                currentPage--;
                return fetchAndRender();
            }
            div.appendChild(createP({textContent: "No available hours for this date."}));
        } else {

            const ul = createUl();

            data.hours.forEach(hour => {
                const li = createLi({textContent: hour});
                if(currentUser != null) {
                    li.onclick = () => {
                        if (li.querySelector(".book-confirm")) return;

                        const confirmDiv = createDiv({className: "book-confirm"});
                        confirmDiv.appendChild(createP({textContent: "Book this hour? Yes/No"}));

                        const yesButton = createButton({
                            textContent: "Yes",
                            className: "btn btn-sm btn-success me-2"
                        });

                        const noButton = createButton({
                            textContent: "No",
                            className: "btn btn-sm btn-danger"
                        });

                        confirmDiv.appendChild(yesButton);
                        confirmDiv.appendChild(noButton);

                        li.appendChild(confirmDiv);

                        noButton.onclick = (e) => {
                            e.stopPropagation();
                            confirmDiv.remove();
                            console.log("Booking cancelled.");
                        };

                        yesButton.onclick = async (e) => {
                            e.stopPropagation();
                            confirmDiv.remove();

                            const result = await fetchAvailableHours(cid, crid, date, 100, 0);
                            const hourStrings = result.hours || [];

                            const allAvailableHours = hourStrings.map(h => parseInt(h.split(":")[0]));

                            const hourInt = parseInt(hour);
                            const index = allAvailableHours.indexOf(hourInt);

                            if (index === -1) {
                                console.warn("Start hour is not available.");
                                return;
                            }

                            let maxDuration = 1;
                            for (let i = index + 1; i < allAvailableHours.length; i++) {
                                const expectedHour = hourInt + maxDuration;
                                const nextHour = allAvailableHours[i];
                                if (nextHour === expectedHour) {
                                    maxDuration++;
                                } else {
                                    break;
                                }
                            }

                            const durationSelect = createSelect({
                                className: "duration-select form-select form-select-sm",
                                required: true
                            });

                            if (maxDuration > 0) {
                                durationSelect.appendChild(createDefaultOption("Select duration", true, true));
                                for (let i = 1; i <= maxDuration; i++) {
                                    durationSelect.appendChild(createOption({
                                        value: i,
                                        textContent: `${i} hour${i > 1 ? "s" : ""}`
                                    }));
                                }
                            } else {
                                durationSelect.appendChild(createDefaultOption("No available duration", true, true));
                            }

                            // ** Impedir que clique no select dispare o li.onclick **
                            durationSelect.onclick = (e) => {
                                e.stopPropagation();
                            };

                            li.appendChild(durationSelect);

                            const bookButton = createButton({
                                textContent: "Book",
                                className: "book-btn btn btn-sm btn-success"
                            });

                            const CancelButton = createButton({
                                textContent: "Cancel",
                                className: "btn btn-sm btn-danger"
                            });

                            // Impedir propagação dos cliques para o li
                            bookButton.onclick = async (e) => {
                                e.stopPropagation();

                                const selectedDuration = parseInt(durationSelect.value);
                                if (!selectedDuration) {
                                    alert("Select a duration before booking.");
                                    return;
                                }

                                const formatted = date.split('T')[0];
                                const datereserva = `${formatted}T${hour}`;

                                bookButton.disabled = true;
                                bookButton.textContent = "Booking...";

                                const bookingData = {
                                    date: datereserva,
                                    Duration: {duration: selectedDuration}
                                };

                                try {
                                    const res = await createReservation(cid, crid, bookingData);
                                    const rid = res.rid;
                                    console.log("Booking successful:", rid);
                                    alert("Booking successful!");
                                    window.location.hash = `#rentals/${rid}`;
                                } catch (err) {
                                    alert(`Error: ${err.message || "Unable to complete booking."}`);
                                    bookButton.disabled = false;
                                    bookButton.textContent = "Book";
                                }
                            };

                            CancelButton.onclick = (e) => {
                                e.stopPropagation();
                                li.removeChild(bookButton);
                                li.removeChild(CancelButton);
                                li.removeChild(durationSelect);
                            };

                            li.appendChild(bookButton);
                            li.appendChild(CancelButton);
                        };
                    };
                    const p = createP({
                        textContent: "Click on an hour to book it.",
                        className: "text-muted"
                    });
                    div.appendChild(p);

                }

                ul.appendChild(li);
            });

            div.appendChild(ul);
        }
        mainContent.replaceChildren(div);
        div.appendChild(createPaginationControls({
            currentPage,
            isLastPage,
            onPageChange: (newPage) => {
                currentPage = newPage;
                fetchAndRender();
            }
        }));


    }

    fetchAndRender();
}

export const rentalsHandlers = {
    getRentals,
    getRentalsByUser,
    getRentalDetails,
    getRentalsByClubCourtDate,
    EditReservationForm,
    getAvailableHours
}

export default rentalsHandlers








