import "./document.js";
import courtHandlers from "../spa/handlers/courtHandlers.js";
import { getCourtsByClub, generateRentalsLink} from "../spa/handlers/courtHandlers.js";
import {fetchCourtsByClub} from "../spa/handlers/fetchers/courtfetchers.js";
import {fetchClubByID} from "../spa/handlers/fetchers/clubfetchers.js";


let expect;
try {
    ({ expect } = await import("chai"));
} catch {
    expect = window.chai.expect;
}

// Define o contexto global para fetch
const g = typeof global !== "undefined" ? global : window;

let originalFetch;

beforeEach(() => {
    originalFetch = g.fetch;
    window.fetch = (url, options) => {
        if (url.includes("/clubs/1/courts")) {
            return Promise.resolve(new Response(JSON.stringify({
                courts: [
                    { crid: "cr1", name: "Court 1" },
                    { crid: "cr2", name: "Court 2" }
                ]
            }), { status: 200 }));
        }
        // fallback to original fetch or return empty
        return Promise.resolve(new Response(JSON.stringify({ courts: [] }), { status: 200 }));
    };

});

afterEach(() => {
    g.fetch = originalFetch;
});


describe("Court Handlers", () => {

    it("should fetch and render courts list by club", async () => {
        const mockCourtsPage1 = [
            { crid: 1, name: "Court A" }
        ];
        const mockCourtsPage2 = [
            { crid: 2, name: "Court B" }
        ];

        // Usar um elemento DOM real para mainContent
        const mainContent = document.createElement("div");

        // Implementar replaceChildren para limpar e adicionar nós
        mainContent.replaceChildren = function (...nodes) {
            this.innerHTML = '';
            nodes.forEach(n => this.appendChild(n));
        };

        // Mock global fetch para as URLs esperadas
        g.fetch = async (url) => {
            if (url.includes("/courts")) {
                return {
                    ok: true,
                    json: async () => ({ courts: mockCourtsPage1 })
                };
            }
            if (url.includes("/courts")) {
                return {
                    ok: true,
                    json: async () => ({ courts: mockCourtsPage2 })
                };
            }
            if (url.includes("/clubs/1")) {
                return {
                    ok: true,
                    json: async () => ({ cid: 1, name: "Mock Club" })
                };
            }
            return { ok: false };
        };

        // Chamar a função que queremos testar
        await courtHandlers.getCourtsByClub(mainContent, 1);
        console.log("cccc",mainContent.innerHTML);


        // Esperar que os links dos courts estejam renderizados
        const renderedLinks = mainContent.querySelectorAll(".court-link");
        const texts = Array.from(renderedLinks).map(a => a.textContent);

        expect(texts).to.include("Court A");
       // expect(texts).to.include("Court B");
    });





    it("should fetch and render court details", async () => {
        const mockCourt = {
            crid: 1,
            name: "Court A",
            club: {
                name: "Club A",
                cid: 1,
                owner: { name: "Owner A", uid: 123 }
            }
        };

        const mainContent = {
            replaced: false,
            replaceChildren(node) {
                this.replaced = true;
                expect(node.tagName).to.equal("DIV");
                expect(node.textContent).to.include("Court A");
                expect(node.textContent).to.include("Owner A");
            }
        };

        g.fetch = async (url) => {
            if (url.includes("/courts/1")) {
                return {
                    ok: true,
                    json: async () => mockCourt
                };
            }
            if (url.includes("/clubs")) {
                return {
                    ok: true,
                    json: async () => ({ clubs: [{ cid: 1, name: "Club A" }] })
                };
            }
            return { ok: false };
        };

        await courtHandlers.getCourtDetails(mainContent, 1);
        expect(mainContent.replaced).to.be.true;
    });

    it("should generate correct link for rentals after selecting a date", () => {
        const link = generateRentalsLink(1, 1, "2025-04-30");
        if (link !== "#clubs/1/courts/1/rentals?date=2025-04-30") {
            throw new Error("Link gerado incorretamente");
        }
    });





    it("should render courts and creation form inside mainContent", async () => {
        const mainContent = {
            replaced: false,
            appended: false,
            replaceChildren() {
                this.replaced = true;
            },
            appendChild() {
                this.appended = true;
            }
        };

        const fakeFetchCourtsByClub = async () => ({
            courts: [{ name: "Court 1", crid: 1 }]
        });

        const fakeFetchClubByID = async () => ({
            name: "My Club"
        });

        await getCourtsByClub(mainContent, 1, fakeFetchCourtsByClub, fakeFetchClubByID);

        expect(mainContent.replaced).to.be.true;
        expect(mainContent.appended).to.be.true;
    });
});