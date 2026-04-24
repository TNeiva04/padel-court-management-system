import {
    createButton,
    createDiv,
    createForm,
    createH1,
    createInput,
    createLabel,
    createP
} from "../../DSL.js";
import {currentUser} from "../session.js";
import {createClubPost} from "../fetchers/clubfetchers.js";


export function createClubFormView(fetchAndRender, presetName = null) {
    const formChildren = [];

    if (presetName === null) {
        formChildren.push(
            createLabel({
                textContent: "Name:",
                attributes: {for: "club-name"}
            }),
            createInput({
                attributes: {
                    type: "text",
                    name: "name",
                    id: "club-name",
                    required: "true"
                }
            })
        );
    } else {
        formChildren.push(
            createP({
                textContent: `Create Club with the name: "${presetName}"`,
                className: "mb-2"
            })
        );
    }

    formChildren.push(
        createButton({
            textContent: "Create Club",
            attributes: {type: "submit", style: "margin-top:10px"}
        })
    );

    const form = createForm({
        id: "create-club-form",
        children: formChildren,
        events: {
            submit: async (e) => {
                e.preventDefault();
                if (form.dataset.submitting === "true") return;
                form.dataset.submitting = "true";

                try {
                    const name = presetName ?? form.querySelector("#club-name").value.trim();

                    if (!name) {
                        alert("Name of club is required.");
                        form.dataset.submitting = "false";
                        return;
                    }

                    const club = await createClubPost(name);

                    if (club && club.cid) {
                        alert(`Club with name "${name}" created.`);
                        location.hash = `#clubs/${club.cid}`;
                    } else {
                        alert("Error creating club.");
                    }

                } catch (err) {
                    console.error("Error creating club:", err);
                    alert("Error creating club.");
                } finally {
                    form.dataset.submitting = "false";
                }
            }
        }
    });

    return createDiv({
        className: "create-court-container",
        children: [
            createH1({textContent: "Do you want to create a Club? Do it Now!"}),
            form
        ]
    });
}
