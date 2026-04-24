import {createA, createDiv, createH1, createP} from "../../../DSL.js";
import {createBackLink, createPaginationControls} from "../buttons.js";

export function renderRentals(mainContent, H1, rentals, currentPage, isLastPage, onPrev, onNext, fetchAndRender, backtype, id) {
    let court = null;

    const container = createDiv({
        className: "rentals-container",
        children: [
            createH1({textContent: H1}),

            rentals.length === 0
                ? createP({textContent: "No rentals yet!"})
                : createDiv({
                    children: rentals.map(rental => {
                        court = rental.crid;
                        return createA({
                            textContent: `${rental.datein} - ${court.name} (Club: ${court.club.name})`,
                            attributes: {href: `#rentals/${rental.rid}`},
                            className: "rental-link"
                        });
                    })
                }),

            createDiv({
                className: "rentals-nav",
                children: [
                    createPaginationControls({
                        currentPage,
                        isLastPage,
                        onPageChange: (newPage) => {
                            if (newPage < currentPage && onPrev) {
                                onPrev();
                            } else if (newPage > currentPage && onNext) {
                                onNext();
                            }
                        }
                    })
                ]
            }),
            createBackLink(backtype, id)
        ]
    });

    mainContent.replaceChildren(container);
}




