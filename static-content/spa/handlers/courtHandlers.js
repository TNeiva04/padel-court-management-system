import {
    createA, createButton,
    createDiv,
    createH1,
    createInput,
    createLi,
    createP,
    createUl, onChange
} from '../DSL.js';
import { createCourtForm } from "./views/CreateCourts.js";
import { createReservationForm } from "./views/Rentals/CreateRentals.js";
import {
    fetchCourtDetails,
    fetchCourtsByClub
} from "./fetchers/courtfetchers.js";
import {
    fetchClubByID,
    fetchClubs
} from "./fetchers/clubfetchers.js";
import { createPaginationControls } from "./views/buttons.js";
import {currentUser} from "./session.js";

export function getCourtsByClub(mainContent, cid) {

    const fetchCourts =  fetchCourtsByClub;
    const getClub =  fetchClubByID;

    const pageSize = 1;
    let currentPage = 0;

    async function fetchAndRender() {
        const skip = currentPage * pageSize;

        try {
            const { courts } = await fetchCourts(cid, pageSize, skip);
            const { courts: nextPageCourts } = await fetchCourts(cid, pageSize, skip + pageSize);
            const club = await getClub(cid);
            const isLastPage = !nextPageCourts?.length;

            console.log(courts);

            mainContent.replaceChildren();

            const courtsSection = createDiv({
                className: "courts-container",
                children: [
                    createH1({
                        textContent: courts.length > 0
                            ? `Courts of Club "${club.name}"`
                            : "This club has no courts yet."
                    }),
                    createDiv({
                        children: courts.map(court =>
                            createP({
                                children: [
                                    createA({
                                        textContent: court.name,
                                        attributes: { href: `#courts/${court.crid}` },
                                        className: "court-link"
                                    })
                                ]
                            })
                        )
                    }),
                    createPaginationControls({
                        currentPage,
                        isLastPage,
                        onPageChange: (newPage) => {
                            currentPage = newPage;
                            fetchAndRender();
                        }
                    })
                ]
            });


            const createSection = createCourt(currentPage, cid, fetchAndRender);


            mainContent.appendChild(
                createDiv({ children: [courtsSection, createSection] })
            );
        } catch (err) {
            mainContent.textContent = `Error: ${err.message}`;
        }
    }

    return fetchAndRender();
}

async function getCourtDetails(mainContent, crid) {
    mainContent.textContent = "Loading court details...";

    try {
        const fetchCourt = fetchCourtDetails;
        const getClubs = fetchClubs;

        const court = await fetchCourt(crid);

        if (!court || !court.club || !court.club.owner) {
            mainContent.textContent = "Failed to load court details.";
            return;
        }

        const container = createDiv({ className: "courtsdetails-wrapper" });

        const detailsDiv = createDiv({
            className: "courtsdetails-container",
            children: [
                createH1({ textContent: `Court ${court.name}` }),
                createUl({
                    children: [
                        createLi({ textContent: `ID: ${court.crid}` }),
                        createLi({ textContent: `Name: ${court.name}` }),
                        createLi({ textContent: `Club: ${court.club.name}` }),
                        createLi({ textContent: `Owner: ${court.club.owner.name}` })
                    ]
                }),
                createA({
                    textContent: "← Back to Courts of this Club",
                    attributes: { href: `#clubs/${court.club.cid}/courts` },
                    className: "back-link"
                })
            ]
        });

        // Input de data
        const inputField = createInput({
            attributes: {
                type: "date",
                placeholder: "Choose a date"
            }
        });

        // Botão para ver alugueres
        const rentalsButton = createButton({
            textContent: "See rentals on this date",
            className: "disabled-link"
        });

        // Evento: ativa botão com base no input
        inputField.addEventListener("input", () => {
            if (inputField.value) {
                rentalsButton.classList.remove("disabled-link");
            } else {
                rentalsButton.classList.add("disabled-link");
            }
        });

        // Evento: clique no botão
        rentalsButton.addEventListener("click", () => {
            const date = inputField.value;
            if (!date) {
                alert("Please select a date first.");
                return;
            }
            window.location.href = generateRentalsLink(court.club.cid, court.crid, date);
        });



        const controlsDiv = createDiv({
            className: "courtsdetails-controls",
            children: [inputField, rentalsButton]
        });

        const clubs = await getClubs();
        let reservationForm;
        if(currentUser != null) {
            reservationForm = await createReservationForm({
                clubs,
                club: court.club,
                court,
                onCreated: (rental) => {
                    window.location.href = `#rentals/${rental.rid}`;
                }
            });
        }else{
            reservationForm = createDiv({
                className: 'reservation-warning',
                children: [
                    createP({
                        textContent: 'You need to log in to make a reservation.'
                    }),
                    createA({
                        textContent: 'Log in',
                        attributes: {
                            href: '#login'
                        },
                        className: 'login-home-link-reservation'
                    })
                ]
            });
        }
        container.replaceChildren(detailsDiv, controlsDiv, reservationForm);
        mainContent.replaceChildren(container);
    } catch (error) {
        console.error("Error fetching court details:", error);
        mainContent.textContent = "Error loading court details.";
    }
}



function createCourt(currentPage, cid, fetchAndRender) {
    if (currentUser != null) {
        return createDiv({
            className: "create-court-container mt-4",
            children: [
                createH1({textContent: "Do you want to create a court for this club? Do it now!"}),
                createCourtForm(cid, async () => {
                    currentPage = 0;
                    await fetchAndRender();
                })
            ]
        });
    } else {
        return createDiv({
            className: "create-court-container",
            children: [
                createP({
                    textContent: "You need to log in to create a court.",
                    className: "login-warning"
                }),
                createA({
                    textContent: "Log in",
                    attributes: {href: "#login"},
                    className: "login-link"
                })
            ]
        });
    }
}

export function generateRentalsLink(clubCid, courtCrid, date) {
    return `#clubs/${clubCid}/courts/${courtCrid}/rentals?date=${date}`;
}

export const courtHandlers = {
    createCourt,
    getCourtsByClub,
    getCourtDetails
};

export default courtHandlers;