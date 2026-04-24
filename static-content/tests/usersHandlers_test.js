import "./document.js";
import userHandlers from "../spa/handlers/userHandlers.js";
const expect = chai.expect;

describe("userHandlers.getUserDetails", function() {
    let mainContent;
    let originalFetch;

    const mockUser = {
        uid: "u1",
        name: "User One",
        email: { email: "user1@example.com" },
        password: "password123",
        token: "token123"
    };

    const mockOtherUser = {
        uid: "u2",
        name: "User Two",
        password: "password456",
        email: { email: "user2@example.com" }
    };

    beforeEach(() => {
        mainContent = document.createElement("div");
        document.body.appendChild(mainContent);

        originalFetch = window.fetch;

        window.fetch = (url) => {
            if (url.includes("/users/u1")) {
                return Promise.resolve(new Response(JSON.stringify(mockUser), { status: 200 }));
            }
            if (url.includes("/users/u2")) {
                return Promise.resolve(new Response(JSON.stringify(mockOtherUser), { status: 200 }));
            }
            if (url.includes("/users")) {
                // Return list of users for getUserByToken
                return Promise.resolve(new Response(JSON.stringify([mockUser]), { status: 200 }));
            }
            return Promise.reject(new Error("Unknown URL: " + url));
        };
    });

    afterEach(() => {
        window.fetch = originalFetch;
        mainContent.remove();
    });

    it("should render full details with links when user is current user", async () => {
        // Simulate currentUser token matching mockUser.token
        window.sessionStorage.setItem("currentUser", JSON.stringify(mockUser.token));

        await userHandlers.getUserDetails(mainContent, "u1");

        const container = mainContent.querySelector(".userdetails-container");
        expect(container).to.exist;

        const h1 = container.querySelector("h1");
        expect(h1.textContent).to.equal("User One's Details");

        const lis = container.querySelectorAll("li");
        expect(lis.length).to.equal(4); // Name, Email, See my rentals, See my clubs

        expect(lis[0].textContent).to.equal("Name: User One");
        expect(lis[1].textContent).to.equal("Email: user1@example.com");

        const rentalsLink = lis[2].querySelector("a");
        expect(rentalsLink).to.exist;
        expect(rentalsLink.textContent).to.equal("See my rentals");
        expect(rentalsLink.getAttribute("href")).to.equal("#rentalsbyuser/u1");

        const clubsLink = lis[3].querySelector("a");
        expect(clubsLink).to.exist;
        expect(clubsLink.textContent).to.equal("See my clubs");
        expect(clubsLink.getAttribute("href")).to.equal("#clubsbyuser");
    });

    it("should render limited details without links when user is different", async () => {
        // Simulate currentUser token different from mockOtherUser
        window.sessionStorage.setItem("currentUser", JSON.stringify("token123"));

        await userHandlers.getUserDetails(mainContent, "u2");

        const container = mainContent.querySelector(".userdetails-container");
        expect(container).to.exist;

        const h1 = container.querySelector("h1");
        expect(h1.textContent).to.equal("User Two's Details");

        const lis = container.querySelectorAll("li");
        expect(lis.length).to.equal(2); // Name, Email only

        expect(lis[0].textContent).to.equal("Name: User Two");
        expect(lis[1].textContent).to.equal("Email: user2@example.com");

        // No links should be present
        const links = container.querySelectorAll("a");
        expect(links.length).to.equal(0);
    });

    it("should show error message on fetch failure", async () => {
        window.fetch = () => Promise.reject(new Error("Fetch failed"));

        await userHandlers.getUserDetails(mainContent, "u1");

        expect(mainContent.textContent).to.equal("Falha ao carregar os detalhes do utilizador.");
    });
});




/*
console.log("Test script carregado.");



let expect;

try {
    // Node
    ({ expect } = await import("chai"));
} catch {
    // Browser
    //console.log("Test script carregado.");
    expect = window.chai.expect;
}
console.log("Expect carregado:", expect);

const g = typeof global !== "undefined" ? global : window;
if (typeof g.document === "undefined") {
    g.document = {};
}


const users = [
    { id: 123, name: "John Doe" },
    { id: 456, name: "Jane Smith" },
    { id: 789, name: "Alice Johnson" }
];

// Simula a busca de um usuário no banco de dados
const findUserById = (id) => {
    const user = users.find(u => u.id === id);
    if (!user) {
        throw new Error("InternalError.UserNotFound");
    }
    return user;
};

// Simula o comportamento do `fetch` que retorna um erro quando o usuário não é encontrado
g.fetch = (url, options) => {
    const userId = parseInt(url.split("/")[2]); // Supondo que o ID seja parte da URL
    try {
        const user = findUserById(userId);
        return Promise.resolve({
            json() {
                return user;
            }
        });
    } catch (error) {
        if (error.message === "InternalError.UserNotFound") {
            return Promise.reject({
                status: 404,
                json() {
                    return {
                        code: 5,
                        error: "User not found"
                    };
                }
            });
        }
    }
};

describe("Users Handlers", () => {

    it("deve obter os detalhes do utilizador com sucesso", (done) => {
        console.log("Test script carregadovvvvv.");

        const fakeResponse = {
            json() {
                return {
                    uid: 123,
                    name: "John Doe",
                    email: { email: "john@example.com" },
                    token: "token123"
                };
            }
        };

        g.fetch = async () => fakeResponse;

        const mainContent = {
            replaceChildrenCalledWith: null,
            replaceChildren(node) {
                this.replaceChildrenCalledWith = node;
                done();
            }
        };
        console.log("Test script carregadovv.");

        // Chama o handler para o usuário com ID 123
        usersHandlers.getUserDetails(mainContent, 123);
    });

    it("deve lançar erro quando o ID do usuário não for encontrado", (done) => {
        const invalidUserId = 99999;

        // Simular erro do fetch
        g.fetch = () =>
            Promise.reject(new Error("User not found"));

        const mainContent = {
            textContent: "",
            replaceChildren() {
                // não faz nada, porque no erro não usamos isto
            }
        };

        // Executa a função
        usersHandlers.getUserDetails(mainContent, invalidUserId);

        // Espera um pouco para a Promise ser rejeitada
        setTimeout(() => {
            try {
                expect(mainContent.textContent).to.equal("Falha ao carregar os detalhes do utilizador.");
                done();
            } catch (err) {
                done(err);
            }
        }, 10);
    });

});
*/