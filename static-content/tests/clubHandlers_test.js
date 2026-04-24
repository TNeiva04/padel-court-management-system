import "./document.js";
//C:\Users\Utilizador\IdeaProjects\2425-2-LEIC43D-G07\static-content\spa\handlers\fetchers\userfetcher.js

import clubHandlers from "../spa/handlers/clubHandlers.js";

let expect;
try {
    ({ expect } = await import("chai"));
} catch {
    expect = window.chai.expect;
}

const g = typeof global !== "undefined" ? global : window;

let originalGetItem;

beforeEach(() => {
    originalGetItem = window.localStorage.getItem;
    window.localStorage.getItem = () => "mock-token";
});

afterEach(() => {
    delete g.fetch;
    window.localStorage.getItem = originalGetItem;
});

describe("Club Handlers", () => {
    it("should fetch and render club details ", async () => {
        const mainContent = {
            replaced: false,
            node: null,
            replaceChildren(node) {
                this.replaced = true;
                this.node = node;
            },
            textContent: ""
        };

        g.fetch = async (url) => {
            if (url.endsWith("/clubs/1")) {
                return {
                    ok: true,
                    json: async () => ({
                        cid: 1,
                        name: "Mock Club",
                        owner: { name: "Mock Owner", uid: 42 }
                    })
                };
            }
            return { ok: false };
        };

        await clubHandlers.getClubDetails(mainContent, 1);

        expect(mainContent.replaced).to.be.true;

        const div = mainContent.node;
        expect(div.className).to.equal("clubsdetails-container");

        const text = div.textContent;
        expect(text).to.include("Owner: Mock Owner");
        expect(text).to.include("(See owner)");
        expect(text).to.include("Mock Club");
    });



    it("should handle no clubs in getClubs", (done) => {
        g.fetch = async (url) => {
            // const urlObj = new URL(url, "http://localhost"); // base fake
            // const limit = parseInt(urlObj.searchParams.get("limit"));
            // const skip = parseInt(urlObj.searchParams.get("skip"));

            return {
                ok: true,
                json: async () => ({ clubs: [] })
            };
        };

        const mainContent = {
            replaceChildren(node) {
                try {
                    const text = node.textContent?.toLowerCase() || "";
                    expect(text).to.include("no clubs yet");
                    done();
                } catch (err) {
                    done(err);
                }
            },
            textContent: ""
        };

        clubHandlers.getClubs(mainContent);
    });


    it("should fetch and render club by name with no results", (done) => {
        g.fetch = async () => ({ json: async () => ({ clubs: [] }) });

        const mainContent = {
            replaceChildren(node) {
                try {
                    expect(node.className).to.match(/clubs-container|error-container/);
                    expect(node.textContent.toLowerCase()).to.include("no results");
                    done();
                } catch (err) {
                    done(err);
                }
            }
        };

        clubHandlers.getClubByName(mainContent, "NonExistentClub");
    });

    it("should render create club form", () => {
        const mainContent = {
            replaced: false,
            replaceChildren(node) {
                this.replaced = true;
                expect(node).to.exist;
            }
        };

        clubHandlers.createClub(mainContent);
        expect(mainContent.replaced).to.be.true;
    });

    it("should fetch and render club list with results", (done) => {
        const allClubs = [
            { cid: 1, name: "A" },
            { cid: 2, name: "B" },
            { cid: 3, name: "C" }
        ];

        g.fetch = async (url) => {
            const urlObj = new URL(url, "http://localhost");
            const limit = Number(urlObj.searchParams.get("limit")) || allClubs.length;
            const skip = Number(urlObj.searchParams.get("skip")) || 0;

            // Pega fatia dos clubes para simular paginação
            const pagedClubs = allClubs.slice(skip, skip + limit);

            return {
                ok: true,
                json: async () => ({ clubs: pagedClubs })
            };
        };

        const mainContent = {
            replaceChildren(node) {
                try {
                    const text = node.textContent.toLowerCase();
                    expect(text).to.include("discover local clubs");
                    expect(text).to.include("a");
                    expect(text).to.include("b");
                    // C deve aparecer na proxiam pagina
                    done();
                } catch (err) {
                    done(err);
                }
            },
            textContent: ""
        };

        clubHandlers.getClubs(mainContent);
    });


    it("should fetch and render club by name with results", (done) => {
        const mainContent = {
            replaceChildren(node) {
                try {
                    expect(node.className).to.equal("clubs-container");
                    expect(node.textContent.toLowerCase()).to.include("results for");
                    expect(node.textContent.toLowerCase()).to.include("myclub");
                    done();
                } catch (err) {
                    done(err);
                }
            }
        };

        g.fetch = async () => ({ json: async () => ({ clubs: [{ cid: 1, name: "MyClub" }] }) });

        clubHandlers.getClubByName(mainContent, "MyClub");
    });

    it("should create a club successfully", async () => {
        const mainContent = {
            replaced: false,
            replaceChildren(node) {
                this.replaced = true;
                expect(node).to.exist;
            }
        };

        g.fetch = async () => ({
            ok: true,
            json: async () => ({ cid: 1, name: "New Club" })
        });

        await clubHandlers.createClub(mainContent);
        expect(mainContent.replaced).to.be.true;
    });


});

