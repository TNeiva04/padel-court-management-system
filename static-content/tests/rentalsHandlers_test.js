import rentalsHandlers from "../spa/handlers/rentalsHandlers.js";
import * as session from "../spa/handlers/session.js";

const expect = chai.expect;

describe("rentalsHandlers tests", function() {
    let mainContent;
    let originalFetch;

    const mockRentals = [
        {
            rid: "r1",
            datein: "2024-06-01T10:00:00",
            crid: { crid: "cr1", name: "Court 1", club: { cid: "c1", name: "Club 1" } },
            uid: { uid: "u1", name: "User 1", email: { email: "user1@example.com" }, token: "token123" },
            duration: { duration: 2 }
        }
    ];

    const mockRentalDetails = {
        rid: "r1",
        datein: "2024-06-01T10:00:00",
        crid: { crid: "cr1", name: "Court 1", club: { cid: "c1", name: "Club 1" } },
        uid: { uid: "u1", name: "User 1", email: { email: "user1@example.com" }, token: "token123" },
        duration: { duration: 2 }
    };

    //const mockUser = { uid: "u1", name: "User 1", token: "token123" };
    const mockUser = {
        uid: "u1",
        name: "User 1",  // token que será retornado por getCurrentToken
        email: { email: "user1@example.com" },
        token: "token123"
    };


    beforeEach(function() {
        mainContent = document.createElement("div");
        document.body.appendChild(mainContent);

        originalFetch = window.fetch;
        window.fetch = async (url, options) => {
            if (url.includes("users")) {
                return new Response(JSON.stringify([mockUser]), { status: 200 });
            }
            if (url.includes("rentals/r1")) {
                return new Response(JSON.stringify(mockRentalDetails), { status: 200 });
            }

            if (url.includes("rentals?limit=5&skip=0")) {
                return new Response(JSON.stringify(mockRentals), {status: 200});
            }
            if (url.includes("rentals?limit=5&skip=5")) {
                return new Response(JSON.stringify([]), {status: 200});
            }
            if (url.includes("users")) {
                return new Response(JSON.stringify([mockUser]), {status: 200});
            }
            if (url.includes("rentalsbyuser/u1?limit=2&size=0")) {
                return new Response(JSON.stringify(mockRentals), {status: 200});
            }
            if (url.includes("rentalsbyuser/u1?limit=2&size=2")) {
                return new Response(JSON.stringify([]), {status: 200});
            }
            if (url.includes("rentals/r1") && (!options || options.method === "GET")) {
                return new Response(JSON.stringify(mockRentalDetails), {status: 200});
            }
            if (url.includes("availablehours")) {
                return new Response(JSON.stringify({hours: ["10:00", "11:00"]}), {status: 200});
            }
            if (url.includes("rentals/r1") && options && options.method === "PUT") {
                return new Response(JSON.stringify({success: true}), {status: 200});
            }
            if (url.includes("clubs/c1/courts/cr1/rentals")) {
                if (url.includes("skip=0")) {
                    return new Response(JSON.stringify(mockRentals), {status: 200});
                }
                if (url.includes("skip=2")) {
                    return new Response(JSON.stringify([]), {status: 200});
                }
            }
            if (url.includes("rentals") && options && options.method === "POST") {
                return new Response(JSON.stringify({rid: "r2"}), {status: 200});
            }
            return new Response(JSON.stringify([]), {status: 200});
        };

        // Usa o mock token para testes
        session.setMockToken("token123");
    });

    afterEach(function() {
        window.fetch = originalFetch;
        mainContent.remove();

        // Limpa o mock token após cada teste
        session.clearMockToken();
    });

    it("getRentals should fetch and render rentals list", async function() {
        rentalsHandlers.getRentals(mainContent);
        await new Promise(r => setTimeout(r, 200));
        const links = mainContent.querySelectorAll("a.rental-link");
        expect(links.length).to.equal(1);
        expect(links[0].textContent).to.include("Court 1");
    });

    it("getRentalDetails should fetch and render rental details", async function() {
        await rentalsHandlers.getRentalDetails(mainContent, "r1");
        await new Promise(r => setTimeout(r, 200));
        expect(mainContent.textContent).to.include("Rental: r1");
        expect(mainContent.textContent).to.include("Court: Court 1");
        expect(mainContent.querySelector("a.delete-reservation-link")).to.exist;
        expect(mainContent.querySelector("a.edit-reservation-link")).to.exist;
    });

    it("EditReservationForm should render form and handle submit", async function() {
        this.timeout(5000);
        await rentalsHandlers.EditReservationForm(mainContent, "r1");
        await new Promise(r => setTimeout(r, 200));
        const form = mainContent.querySelector("form");
        expect(form).to.exist;

        const dateInput = form.querySelector('input[type="date"]');
        const selects = form.querySelectorAll('select');
        const hourSelect = selects[0];
        const durationSelect = selects[1];
        const submitButton = form.querySelector('button[type="submit"]');

        dateInput.value = "2024-06-01";
        hourSelect.innerHTML = '<option value="10:00" selected>10:00</option>';
        durationSelect.innerHTML = '<option value="1" selected>1 hour</option>';
        submitButton.style.display = "inline-block";

        form.dispatchEvent(new Event("submit"));
        await new Promise(r => setTimeout(r, 500));
        expect(window.location.hash).to.equal("#rentals/r1");
    });

    it("getRentalsByClubCourtDate should fetch and render rentals", async function() {
        rentalsHandlers.getRentalsByClubCourtDate(mainContent, "c1", "cr1", "2024-06-01T00:00:00");
        await new Promise(r => setTimeout(r, 200));
        expect(mainContent.textContent).to.include("Rentals on 2024-06-01");
        expect(mainContent.querySelectorAll("a.rental-link").length).to.equal(1);
    });

    it("getAvailableHours should fetch and render available hours and allow booking", async function() {
        rentalsHandlers.getAvailableHours(mainContent, "c1", "cr1", "2024-06-01T00:00:00");
        await new Promise(r => setTimeout(r, 200));
        const lis = mainContent.querySelectorAll("li");
        expect(lis.length).to.equal(2);

        lis[0].click();

        const yesBtn = lis[0].querySelector("button.btn-success");
        const noBtn = lis[0].querySelector("button.btn-danger");
        expect(yesBtn).to.exist;
        expect(noBtn).to.exist;

        yesBtn.click();

        await new Promise(r => setTimeout(r, 200));
        expect(window.location.hash).to.equal("#rentals/r1");
    });
});
