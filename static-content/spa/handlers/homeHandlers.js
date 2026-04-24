import {currentUser, setCurrentUser} from "./session.js";
import {
    createDiv,
    createH1,
    createP,
    createButton,
    createInput,
    createSelect,
    createOption,
    createDefaultOption,
    onClick,
    onKeyPress, createA,
} from "../DSL.js";

import {createReservationForm} from "./views/Rentals/CreateRentals.js";
import {fetchUsers} from "./fetchers/userfetcher.js";
import {fetchClubs} from "./fetchers/clubfetchers.js";


export function showMessage(msg) {
    alert(msg);
}

async function getHome(mainContent, titleText = "Chelas Padel") {
    if (!(mainContent instanceof HTMLElement)) return;

    try {
        const [users, clubs] = await Promise.all([fetchUsers(), fetchClubs()]);

        const linksContainer = document.getElementById("login");

        if (linksContainer) {
            const userSelection = createUserSelection(users);

            let userDisplay = null;

            if (currentUser !== null) {
                const welcomeText = document.createElement("span");

                userDisplay = createDiv({
                    className: "user-container ms-3 d-flex align-items-center gap-2",
                    children: [welcomeText]
                });
            }
        }

        const container = createHomeContainer(titleText);
        const searchBar = createSearchBar();
        let reservationForm ;

        if (currentUser !== null) {
            reservationForm = await createReservationForm(clubs);
        } else {
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

        container.appendChild(searchBar);

        const centerDiv = createDiv({
            className: "home-main",
            children: [container, reservationForm],
        });

        mainContent.replaceChildren(centerDiv);

    } catch (error) {
        console.error("Error loading data for the home page:", error);
    }
}

function createHomeContainer(titleText) {
    return createDiv({
        className: "home-container d-flex flex-column align-items-center justify-content-center p-5",
        children: [
            createH1({
                textContent: titleText,
                className: "mb-4 fw-bold",
            }),
            createP({
                html: "Your ideal destination to discover the best clubs, courts, and schedules to play padel.<br>Take advantage of our platform and book your next match now!",
                className: "text-center",
            }),
        ],
    });
}

function createSearchBar() {
    const input = createInput({
        className: "form-control",
        attributes: {type: "text", placeholder: "Search clubs..."},
        ...onKeyPress(e => e.key === "Enter" && button.click())
    });

    const button = createButton({
        className: "btn btn-primary",
        textContent: "Search",
        ...onClick(() => {
            const query = input.value.trim();
            location.hash = query ? `#clubs?name=${encodeURIComponent(query)}` : "#clubs";
        }),
    });

    return createDiv({
        className: "input-group w-50 mb-3",
        children: [input, button],
    });
}

function createUserSelection(users) {
    const select = createSelect({
        className: "form-select form-select-sm",
        children: [createDefaultOption("Select your user")],
    });

    select.appendChild(createDefaultOption("Select your user", true, true));
    if (users.length) {
        users.forEach(user => {
            select.appendChild(
                createOption({
                    attributes: {value: user.token},
                    textContent: user.username || user.name || user,
                })
            );
        });
    } else {
        select.appendChild(createDefaultOption("Error loading users"));
    }

    const button = createButton({
        className: "btn btn-success btn-sm ms-2",
        textContent: "Login",
        ...onClick(() => {
            const token = select.value;
            const name = select.options[select.selectedIndex].textContent;
            if (token) {
                setCurrentUser(token);
                showMessage(`Welcome, ${name}!`);
                window.location.hash = "#home";
                window.location.reload();
            }
        }),
    });

    return createDiv({
        className: "d-flex align-items-center gap-2 user-container",
        children: [select, button],
    });
}


export const homeHandlers = {
    getHome
};

export default homeHandlers;
