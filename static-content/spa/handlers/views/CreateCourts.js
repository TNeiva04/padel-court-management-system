import {createButton, createForm, createInput, createLabel} from "../../DSL.js";
import {currentUser} from "../session.js";
import {API_BASE_URL} from "../../router.js";

export function createCourtForm(cid, onCreated) {
    const form = createForm({
        attributes: {id: "create-court-form"},
        children: [
            createLabel({
                textContent: "Name:",
                attributes: {for: "court-name"}
            }),
            createInput({
                attributes: {
                    type: "text",
                    name: "name",
                    id: "court-name",
                    required: "true"
                }
            }),
            createButton({
                textContent: "Create Court",
                attributes: {
                    type: "submit",
                    style: "margin-top: 10px;"
                }
            })
        ]
    });

    form.addEventListener("submit", async (e) => {
        e.preventDefault();
        if (form.dataset.submitting === "true") return;
        form.dataset.submitting = "true";

        try {
            const formData = new FormData(form);
            const data = Object.fromEntries(formData.entries());

            const res = await fetch(`${API_BASE_URL}clubs/${cid}/courts`, {
                method: "POST",
                headers: {
                    "Authorization": `Bearer ${currentUser}`,
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(data)
            });

            const result = await res.json();
            if (result.error) {
                alert(`Error: ${result.error}`);
            } else {
                alert("Court created successfully!");
                if (onCreated) onCreated();
            }
        } catch (err) {
            alert(`Error: ${err.message}`);
        } finally {
            form.dataset.submitting = "false";
        }
    });

    return form;
}

