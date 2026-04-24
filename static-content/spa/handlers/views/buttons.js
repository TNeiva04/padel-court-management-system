import {createA, createButton, createDiv, onClick} from "../../DSL.js";

export function createPaginationControls({
                                             currentPage,
                                             isLastPage,
                                             onPageChange
                                         }) {
    const prevBtn = createButton({
        textContent: "◀ Previous",
        attributes: {
            disabled: currentPage === 0 ? "true" : undefined
        },
        ...onClick(() => {
            if (currentPage > 0) {
                onPageChange(currentPage - 1);
            }
        })
    });

    const nextBtn = createButton({
        textContent: "Next ▶",
        attributes: {
            disabled: isLastPage ? "true" : undefined
        },
        ...onClick(() => {
            onPageChange(currentPage + 1);
        })
    });

    return createDiv({
        className: "pagination-controls",
        children: [prevBtn, nextBtn]
    });
}

export function createBackLink(type, id) {
    const linkData = {
        crid: {href: `#courts/${id}`, textContent: "← Back to court details"},
        uid: {href: `#users/${id}`, textContent: "← Back to user details"},
        cid: {href: `#clubs/${id}`, textContent: "← Back to club details"},
        getcourts: {href: `#clubs/${id}courts`, textContent: "← Back to courts"},
    };

    if (linkData[type]) {
        return createA({
            textContent: linkData[type].textContent,
            className: "rentals-back-link",
            attributes: {
                href: linkData[type].href
            }
        });
    }

    return null;
}


