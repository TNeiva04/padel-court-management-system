import router from "./router.js";

import clubHandlers from "./handlers/clubHandlers.js";
import courtHandlers from "./handlers/courtHandlers.js";
import rentalsHandlers from "./handlers/rentalsHandlers.js";
import usersHandlers from "./handlers/userHandlers.js";
import homeHandlers, {showMessage} from "./handlers/homeHandlers.js";
import {currentUser, setCurrentUser} from "./handlers/session.js";
import {getUserByToken} from "./handlers/fetchers/userfetcher.js";
import {createA, onClick} from "./DSL.js";

window.addEventListener('load', loadHandler);
window.addEventListener('hashchange', hashChangeHandler);

window.addEventListener("DOMContentLoaded", () => {
    const loginDiv = document.getElementById("login");

    function updateViewFromHash() {
        const hash = window.location.hash;

        if (hash === "#home" || hash === "" || hash === "#") {
            if (loginDiv) loginDiv.style.display = "block";
            window.location.hash = "home";
        } else {
            if (loginDiv) loginDiv.style.display = "none";
        }
    }

    window.addEventListener("hashchange", updateViewFromHash);

    // Runs on initial load
    updateViewFromHash();
});

function loadHandler() {

    router.addRouteHandler("home", homeHandlers.getHome);

    router.addRouteHandler("clubs/create", clubHandlers.createClub);
    router.addRouteHandler("clubs", clubHandlers.getClubs);
    router.addRouteHandler("clubs/{cid}", clubHandlers.getClubDetails);
    router.addRouteHandler("clubs/{cid}/courts", courtHandlers.getCourtsByClub);
    router.addRouteHandler("clubsbyuser", clubHandlers.getUserClubs);
    router.addRouteHandler("clubs?name={name}", clubHandlers.getClubByName);

    router.addRouteHandler("courts/{crid}", courtHandlers.getCourtDetails);

    router.addRouteHandler("rentals/{rid}", rentalsHandlers.getRentalDetails);
    router.addRouteHandler("rentals", rentalsHandlers.getRentals);
    router.addRouteHandler("rentalsbyuser/{uid}", rentalsHandlers.getRentalsByUser);
    router.addRouteHandler("clubs/{cid}/courts/{crid}/rentals", rentalsHandlers.getRentalsByClubCourtDate);
    router.addRouteHandler("clubs/{cid}/courts/{crid}/availablehours", rentalsHandlers.getAvailableHours);
    router.addRouteHandler("editrental/{rid}", rentalsHandlers.EditReservationForm);

    router.addRouteHandler("users/{uid}", usersHandlers.getUserDetails);
    router.addRouteHandler("login", usersHandlers.Login);
    router.addRouteHandler("register", usersHandlers.Register);

    router.addDefaultNotFoundRouteHandler(() => window.location.hash = "home");

    hashChangeHandler();
}

async function hashChangeHandler() {
    const mainContent = document.getElementById("mainContent");
    const path = window.location.hash.replace("#", "");

    const handler = router.getRouteHandler(path);

    if (handler) {
        if (currentUser != null) {
            const username = await getUserByToken(currentUser);

            const welcomeContainer = document.getElementById("welcome");

            const welcomeLink = createA({
                textContent: `Welcome, ${username.name}`,
                attributes: {
                    href: `#users/${username.uid}`,
                    style: "color: black;"
                }
            });

            const logoutLink = createA({
                textContent: "Logout",
                attributes: { id: "logout" },
                style: { cursor: "pointer" },
                ...onClick(() => {
                    setCurrentUser(null);
                    showMessage("Session ended successfully!");
                    window.location.hash = "#home";
                    window.location.reload();
                })
            });

            welcomeContainer.replaceChildren(welcomeLink, logoutLink);

            // Remover a classe "link-success" do link de login
            const loginLink = document.querySelector('a[href="#login"]');
            const registerLink = document.querySelector('a[href="#register"]');
            if (loginLink) {
                loginLink.classList.remove("link-success");
                registerLink.classList.remove("link-danger");
                registerLink.style.display = "none";
                loginLink.style.display = "none";
            }
        }
        handler(mainContent);
    } else {
        console.log("No handler found for path:", path);
    }
}

