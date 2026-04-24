import {
    createDiv,
    createH1,
    createUl,
    createLi,
    createA, onSubmit, createButton, createInput, createLabel, createForm
} from '../DSL.js';
import {fetchUserDetails, getUserByToken} from "./fetchers/userfetcher.js";
import {currentUser, setCurrentUser} from "./session.js";
import {API_BASE_URL} from "../router.js";

async function getUserDetails(mainContent, uid) {
    console.log("getUserDetails called with uid:", uid);
    try {
        const user = await fetchUserDetails(uid);

        // Obter token atual do sessionStorage
        const storedToken = JSON.parse(window.sessionStorage.getItem("currentUser"));
        let container;

        if (storedToken != null) {
            const userlog = await getUserByToken(storedToken);

            if (user.uid === userlog.uid) {
                container = createDiv({
                    className: "userdetails-container",
                    children: [
                        createH1({
                            textContent: `${user.name}'s Details`
                        }),
                        createUl({
                            children: [
                                createLi({ textContent: `Name: ${user.name}` }),
                                createLi({ textContent: `Email: ${user.email.email}` }),
                                createLi({
                                    children: [
                                        createA({
                                            textContent: "See my rentals",
                                            attributes: { href: `#rentalsbyuser/${user.uid}` }
                                        })
                                    ]
                                }),
                                createLi({
                                    children: [
                                        createA({
                                            textContent: "See my clubs",
                                            attributes: { href: `#clubsbyuser` }
                                        })
                                    ]
                                })
                            ]
                        })
                    ]
                });
            }
        }

        // Se não é o utilizador autenticado ou token não existe
        if (!container) {
            container = createDiv({
                className: "userdetails-container",
                children: [
                    createH1({
                        textContent: `${user.name}'s Details`
                    }),
                    createUl({
                        children: [
                            createLi({ textContent: `Name: ${user.name}` }),
                            createLi({ textContent: `Email: ${user.email.email}` }),
                        ]
                    })
                ]
            });
        }

        mainContent.replaceChildren(container);

    } catch (error) {
        console.error(error);
        mainContent.textContent = "Falha ao carregar os detalhes do utilizador.";
    }
}


function Login(mainContent) {
    mainContent.replaceChildren(
        createDiv({
            className: "login-container",
            children: [
                createH1({ textContent: "Login" }),
                createForm({
                    className: "login-form",
                    children: [
                        createLabel({ textContent: "Email:" }),
                        createInput({
                            attributes: {
                                type: "email",
                                name: "email",
                                required: true,
                                placeholder: "Enter your email"
                            }
                        }),
                        createLabel({ textContent: "Password:" }),
                        createInput({
                            attributes: {
                                type: "password",
                                name: "password",
                                required: true,
                                placeholder: "Enter your password"
                            }
                        }),
                        createButton({
                            textContent: "Login",
                            attributes: {
                                type: "submit"
                            }
                        })
                    ],
                    ...onSubmit(async (e) => {
                        e.preventDefault();
                        const form = e.target;
                        const email = form.email.value;
                        const password = form.password.value;
                        const submitBtn = form.querySelector("button[type=submit]");
                        submitBtn.disabled = true;

                        console.log("Login attempted with:", { email, password });

                        try {
                            const r = await fetch(`${API_BASE_URL}login`, {
                                method: "POST",
                                headers: {
                                    "Content-Type": "application/json"
                                },
                                body: JSON.stringify({ email, password })
                            });

                            if (r.ok) {
                                const data = await r.json();
                                console.log("Login successful:", data);
                                setCurrentUser(data.token);
                                alert("Welcome!");
                                window.location.hash = `#home`;
                            } else {
                                const errorData = await r.json();
                                console.error("Login failed:", errorData);
                                alert("Email or Password are wrong");
                            }
                        } catch (error) {
                            console.error("Request error:", error);
                            alert("Could not connect to the server.");
                        } finally {
                            submitBtn.disabled = false;
                        }
                    })
                })
            ]
        })
    );

    mainContent.appendChild(
        createDiv({
            className: "register-link",
            children: [
                createA({
                    textContent: "Don't have an account? Register here.",
                    attributes: {
                        href: "#register",
                        class: "center"
                    }
                })
            ]
        })
    );
}



function Register(mainContent) {
    mainContent.replaceChildren(
        createDiv({
            className: "register-container",
            children: [
                createH1({ textContent: "Register" }),
                createForm({
                    className: "register-form",
                    children: [
                        createLabel({ textContent: "Name:" }),
                        createInput({
                            attributes: {
                                type: "text",
                                name: "name",
                                placeholder: "Enter your name"
                            }
                        }),
                        createLabel({ textContent: "Email:" }),
                        createInput({
                            attributes: {
                                type: "email",
                                name: "email",
                                required: "true",
                                placeholder: "Enter your email"
                            }
                        }),
                        createLabel({ textContent: "Password:" }),
                        createInput({
                            attributes: {
                                type: "password",
                                name: "password",
                                required: "true",
                                placeholder: "Enter your password"
                            }
                        }),
                        createLabel({ textContent: "Confirm Password:" }),
                        createInput({
                            attributes: {
                                type: "password",
                                name: "confirm",
                                required: "true",
                                placeholder: "Confirm your password"
                            }
                        }),
                        createButton({
                            textContent: "Register",
                            attributes: {
                                type: "submit"
                            }
                        })
                    ],
                    ...onSubmit(async (e) => {
                        e.preventDefault();
                        const form = e.target;
                        const name = form.name.value;
                        const email = form.email.value;
                        const password = form.password.value;
                        const confirmPassword = form.confirm.value;

                        if (password !== confirmPassword) {
                            alert("Passwords do not match!");
                            return;
                        }

                        const data = { name, email, password };
                        console.log("Register attempted with:", data);

                        try {
                            const response = await fetch(`${API_BASE_URL}users`, {
                                method: "POST",
                                headers: {
                                    "Content-Type": "application/json"
                                },
                                body: JSON.stringify(data)
                            });

                            if (response.ok) {
                                const responseData = await response.json();
                                alert("Registration successful!");
                                window.location.hash = "#login";
                            } else {
                                const errorData = await response.json();
                                alert("Registration failed - Theres already an account with that email" + (errorData.message || response.statusText));
                            }
                        } catch (error) {
                            console.error("Erro na requisição:", error);
                            alert("Erro na ligação ao servidor.");
                        }
                    })
                })
            ]
        })
    );

    mainContent.appendChild(
        createDiv({
            className: "register-link",
            children: [
                createA({
                    textContent: "You already have an account? Login here.",
                    attributes: {
                        href: "#login",
                        class: "center"
                    }
                })
            ]
        })
    );
}


export default {
    getUserDetails,
    Login,
    Register
};