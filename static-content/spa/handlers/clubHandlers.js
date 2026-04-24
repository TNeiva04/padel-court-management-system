import {currentUser, getCurrentToken} from "./session.js";
import {
    createDiv, createH1, createA, createElement, createP, createButton, createLi,
} from '../DSL.js';
import { createClubFormView } from "./views/CreateClubs.js";
import {
    createClubPost,
    fetchClubByID,
    fetchClubs,
    fetchClubsByName,
    fetchClubsByUser
} from "./fetchers/clubfetchers.js";
import { getUserByToken } from "./fetchers/userfetcher.js";
import { createBackLink, createPaginationControls } from "./views/buttons.js";

export async function getUserClubs(mainContent) {
    const token =  getCurrentToken();
    const getUser = getUserByToken;
    const getClubsByUser =  fetchClubsByUser;

    try {
        const user = await getUser(token);
        if (!user?.uid) throw new Error("User not found");

        const { clubs } = await getClubsByUser(user.uid);
        if (!clubs) throw new Error("Clubs not found");

        const ul = createElement("ul", {
            children: clubs.map(club =>
                createElement("li", {
                    children: [
                        createA({
                            textContent: club.name,
                            attributes: { href: `#clubs/${club.cid}` },
                            className: "club-link"
                        })
                    ]
                })
            )
        });

        const container = createDiv({
            className: "userclubs-container",
            children: [
                createH1({ textContent: `${user.name}'s Clubs` }),
                ul,
                createBackLink("uid", user.uid)
            ]
        });

        mainContent.replaceChildren(container);
    } catch (error) {
        console.error(error);
        mainContent.textContent = "Failed to load user clubs.";
    }
}

function getClubs(mainContent) {
    const getClubs =  fetchClubs;

    const pageSize = 1;
    let currentPage = 0;

    async function fetchAndRender() {
        const skip = currentPage * pageSize;

        try {
            const [currentData, nextData] = await Promise.all([
                getClubs(pageSize, skip),
                getClubs(pageSize, skip + pageSize)
            ]);

            const clubs = currentData.clubs ?? currentData;
            const nextClubs = nextData.clubs ?? nextData;
            const noClubs = !clubs?.length;
            const isLastPage = !nextClubs?.length;

            const listDiv = noClubs ? null : createDiv({
                children: clubs.map(club =>
                    createP({
                        children: [
                            createA({
                                textContent: club.name,
                                attributes: { href: `#clubs/${club.cid}` }
                            })
                        ]
                    })
                )
            });

            const pagination = noClubs ? null : createPaginationControls({
                currentPage,
                isLastPage,
                onPageChange: (newPage) => {
                    currentPage = newPage;
                    fetchAndRender();
                }
            });

            const clubsSection = createDiv({
                className: "clubs-container",
                children: [
                    createH1({
                        textContent: noClubs
                            ? "No clubs yet — be the first to create one!"
                            : "Discover Local Clubs"
                    }),
                    listDiv,
                    pagination
                ].filter(Boolean)
            });

            let createSection = null
            if(currentUser != null){
                createSection = createClubFormView(fetchAndRender)
            }else{
                createSection = createDiv({
                    className: 'reservation-warning',
                    children: [
                        createP({
                            textContent: 'You need to log in to create a club.'
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


            mainContent.replaceChildren(
                createDiv({ children: [clubsSection, createSection] })
            );
        } catch (err) {
            mainContent.textContent = `Error: ${err.message}`;
        }
    }

    fetchAndRender();
}

function getClubByName(mainContent, initialName) {
    const getByName =  fetchClubsByName;
    const postClub =  createClubPost;

    const name = initialName.trim();
    const pageSize = 1;
    let currentPage = 0;
    let isFetching = false;

    async function fetchAndRender() {
        if (isFetching) return;
        isFetching = true;

        try {
            const skip = currentPage * pageSize;
            const [currentData, nextData] = await Promise.all([
                getByName(name, pageSize, skip),
                getByName(name, pageSize, skip + pageSize)
            ]);

            const clubs = currentData.clubs ?? [];
            const isLastPage = !nextData?.clubs?.length;
            const noClubs = clubs.length === 0;

            let content;
            let button, button2;

            if (noClubs) {
                let h1 = createH1({ textContent: `No results for "${name}".` });
                if(currentUser != null){
                    h1 = createH1({ textContent: `No results for "${name}". Create it now!` });
                    button = createButton({
                        textContent: `Create club named "${name}"`,
                        className: "custom-box btn-create-name",
                        events: {
                            click: async () => {
                                try {
                                    const club = await postClub(name);
                                    if (club) {
                                        alert(`Club "${name}" created successfully!`);
                                        window.location.hash = `#clubs/${club.cid}`;
                                    } else {
                                        alert("Failed to create club.");
                                    }
                                } catch (err) {
                                    alert("Error creating the club: " + err.message);
                                }
                            }
                        }
                    });
                    button2 = createButton({
                        textContent: "Create club with another name",
                        className: "custom-box btn-create-other",
                        events: {
                            click: () => location.hash = "#clubs/create"
                        }
                    });
                }
                    const a = createA({
                        textContent: "← Back to home",
                        attributes: { href: "#home" },
                        className: "home-link back-home-link"
                        });
                if(currentUser === null){
                    content = [h1, a];
                }else{
                    content = [h1, button,button2, a];
                }
            } else {
                const list = createDiv({
                    children: clubs.map(club =>
                        createP({
                            children: [
                                createA({
                                    textContent: club.name,
                                    attributes: { href: `#clubs/${club.cid}` }
                                })
                            ]
                        })
                    )
                });

                const pagination = createPaginationControls({
                    currentPage,
                    isLastPage,
                    onPageChange: (newPage) => {
                        currentPage = newPage;
                        fetchAndRender();
                    }
                });

                content = [
                    createH1({ textContent: `Results for "${name}"` }),
                    list,
                    pagination
                ];
            }

            mainContent.replaceChildren(
                createDiv({
                    className: "clubs-container",
                    children: content
                })
            );

        } catch (err) {
            console.error("Error fetching clubs:", err);
            mainContent.replaceChildren(
                createDiv({
                    className: "error-container",
                    children: [
                        createP({ textContent: "Something went wrong while loading clubs." }),
                        createA({
                            textContent: "← Back to home",
                            attributes: { href: "#home" },
                            className: "home-link"
                        })
                    ]
                })
            );
        } finally {
            isFetching = false;
        }
    }

    fetchAndRender();
}

async function getClubDetails(mainContent, cid) {
    const getByID = fetchClubByID;

    console.log("[getClubDetails] cid recebido:", cid);

    try {
        const club = await getByID(cid);
        console.log("[getClubDetails] clube recebido:", club);

        const ul = createElement("ul", {
            children: [
                createLi({ textContent: `Name: ${club.name}` }),
                createLi({ textContent: `Cid: ${club.cid}` }),
                createLi({
                    children: [
                        createElement("span", {
                            textContent: `Owner: ${club.owner?.name || "Unknown"} `
                        }),
                        ...(club.owner ? [createA({
                            textContent: "(See owner)",
                            attributes: { href: `#users/${club.owner.uid}` }
                        })] : [])
                    ]
                }),
                createLi({
                    children: [
                        createA({
                            textContent: " -> See courts of this club",
                            attributes: { href: `#clubs/${club.cid}/courts` }
                        })
                    ]
                }),
                createLi({
                    children: [
                        createA({
                            textContent: "◀ Back to clubs",
                            attributes: { href: "#clubs" }
                        })
                    ]
                })
            ]
        });

        console.log("[getClubDetails] ul gerado:", ul);

        const div = createDiv({
            className: "clubsdetails-container",
            children: [
                createH1({ textContent: `Club ${club.name} Details` }),
                ul
            ]
        });

        console.log("[getClubDetails] div final gerada:", div);

        mainContent.replaceChildren(div);
        console.log("[getClubDetails] replaceChildren chamado");

    } catch (err) {
        console.error("[getClubDetails] erro ao carregar clube:", err);
        mainContent.textContent = "Failed to load club details.";
    }
}


function createClub(mainContent, presetName = null) {
    mainContent.replaceChildren(createClubFormView(presetName));
}

const clubHandlers = {
    getClubs,
    getClubDetails,
    getUserClubs,
    getClubByName,
    createClub
};

export default clubHandlers;
