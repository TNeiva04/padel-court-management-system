import router from "../spa/router.js";

import homeHandlers from "../spa/handlers/homeHandlers.js";
import clubHandlers from "../spa/handlers/clubHandlers.js";
import courtHandlers from "../spa/handlers/courtHandlers.js";
import rentalsHandlers from "../spa/handlers/rentalsHandlers.js";
import usersHandlers from "../spa/handlers/userHandlers.js";

let expect;

try {
    // Node
    ({ expect } = await import("chai"));
} catch {
    // Browser
    //console.log("Test script carregado.");
    expect = window.chai.expect;
}
console.log("Expect carregado jjjj:", expect);

describe('Router Tests', function () {

    before(function() {
        // Configuração inicial do router
        router.addRouteHandler("home", homeHandlers.getHome);
        router.addRouteHandler("clubs", clubHandlers.getClubs);
        router.addRouteHandler("clubs/10", clubHandlers.getClubDetails);
        router.addRouteHandler("clubs/{10}/courts", courtHandlers.getCourtsByClub);
        router.addRouteHandler("courts/{11}", courtHandlers.getCourtDetails);
        router.addRouteHandler("rentals/{12}", rentalsHandlers.getRentalDetails);
        router.addRouteHandler("rentals", rentalsHandlers.getRentalsByUser );
        router.addRouteHandler("clubs/{4}/courts/{12}/rentals", rentalsHandlers.getRentalsByClubCourtDate);
        router.addRouteHandler("users/{1}", usersHandlers.getUserDetails);
        router.addRouteHandler("clubsbyuser", clubHandlers.getUserClubs);
    });


    it('should find getClubDetails handler', function () {
        let calledWith = null;

        // Cria um fake handler para capturar a chamada
        const fakeHandler = (mainContent, cid) => {
            calledWith = { mainContent, cid };
        };

        router.addRouteHandler("clubs/{cid}", fakeHandler);

        const handler = router.getRouteHandler("clubs/10");
        expect(handler).to.be.a("function");

        handler("div#main");

        expect(calledWith).to.deep.equal({ mainContent: "div#main", cid: "10" });
    });

    it('should find getCourtDetails handler', function () {
        let calledWith = null;

        const fakeHandler = (mainContent, crid) => {
            calledWith = { mainContent, crid };
        };

        router.addRouteHandler("courts/{crid}", fakeHandler);

        const handler = router.getRouteHandler("courts/11");
        expect(handler).to.be.a("function");

        handler("div#main");

        expect(calledWith).to.deep.equal({ mainContent: "div#main", crid: "11" });
    });

    it('should find getUserDetails handler', function () {
        let calledWith = null;

        const fakeHandler = (mainContent, uid) => {
            calledWith = { mainContent, uid };
        };

        router.addRouteHandler("users/{uid}", fakeHandler);

        const handler = router.getRouteHandler("users/1");
        expect(handler).to.be.a("function");

        handler("div#main");

        expect(calledWith).to.deep.equal({ mainContent: "div#main", uid: "1" });
    });

    it('should find getRentalDetails handler', function () {
        let calledWith = null;

        const fakeHandler = (mainContent, rid) => {
            calledWith = { mainContent, rid };
        };

        router.addRouteHandler("rentals/{rid}", fakeHandler);

        const handler = router.getRouteHandler("rentals/12");
        expect(handler).to.be.a("function");

        handler("div#main");

        expect(calledWith).to.deep.equal({ mainContent: "div#main", rid: "12" });
    });

    it('should find getCourtsByClub handler', function () {
        let calledWith = null;

        const fakeHandler = (mainContent, cid) => {
            calledWith = { mainContent, cid };
        };

        router.addRouteHandler("clubs/{cid}/courts", fakeHandler);

        const handler = router.getRouteHandler("clubs/10/courts");
        expect(handler).to.be.a("function");

        handler("div#main");

        expect(calledWith).to.deep.equal({ mainContent: "div#main", cid: "10" });
    });

    it('should find getRentalsByClubCourtDate handler', function () {
        let calledWith = null;

        const fakeHandler = (mainContent, cid, crid, date) => {
            calledWith = { mainContent, cid, crid, date };
        };

        router.addRouteHandler("clubs/{cid}/courts/{crid}/rentals", fakeHandler);

        const handler = router.getRouteHandler("clubs/4/courts/12/rentals?date=2025-05-01T10:00");
        expect(handler).to.be.a("function");

        handler("div#main");

        expect(calledWith).to.deep.equal({ mainContent: "div#main", cid: "4", crid: "12", date: "2025-05-01T10:00" });
    });


    it('should find getHome handler', function () {
        const handler = router.getRouteHandler("home");
        expect(handler).to.equal(homeHandlers.getHome);

    });

    it('should find a handler for "clubs/10"', function () {
        const handler = router.getRouteHandler("clubs/10");
        expect(handler).to.be.a("function");
    });


    it('should find getCourtDetails handler', function () {
        const handler = router.getRouteHandler("courts/5");
        expect(handler).to.be.a("function");
    });

    it('should find getRentalDetails handler', function () {
        const handler = router.getRouteHandler("rentals/3");
        expect(handler).to.be.a("function");
    });

    it('should return notFound handler for non-existent route', function () {
        const handler = router.getRouteHandler("nonexistent");
        expect(handler).to.be.a("function"); // Porque o router devolve o handler default
    });



    it('should find getClubs handler', function () {
        const handler = router.getRouteHandler("clubs");
        expect(handler).to.equal(clubHandlers.getClubs);
    });

    it('should find getRentalsByUser handler', function () {
        const handler = router.getRouteHandler("rentals");
        expect(handler).to.equal(rentalsHandlers.getRentalsByUser);
    });

    it('should find getUserClubs handler', function () {
        const handler = router.getRouteHandler("clubsbyuser");
        expect(handler).to.equal(clubHandlers.getUserClubs);
    });



});