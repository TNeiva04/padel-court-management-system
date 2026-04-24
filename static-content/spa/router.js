
const routes = []
export const API_BASE_URL = `http://localhost:8080/`;
//export const API_BASE_URL = "https://img-ls-2425-2-43-g07.onrender.com/";


let notFoundRouteHandler = () => {
    throw "Route handler for unknown routes not defined"
}

function addRouteHandler(pathTemplate, handler) {
    routes.push({pathTemplate, handler})
}

function addDefaultNotFoundRouteHandler(notFoundRH) {
    notFoundRouteHandler = notFoundRH
}

function getRouteHandler(path) {
    console.log("Received path:", path);
    const patterns = [
        {
            regex: /^courts\/(\d+)$/,
            template: "courts/{crid}",
            extractParams: (m) => [m[1]]
        },
        {
            regex: /^users\/(\d+)$/,
            template: "users/{uid}",
            extractParams: (m) => [m[1]]
        },
        {
            regex: /^clubs\/(\d+)\/courts$/,
            template: "clubs/{cid}/courts",
            extractParams: (m) => [m[1]]
        },
        {
            regex: /^clubs\/(\d+)$/,
            template: "clubs/{cid}",
            extractParams: (m) => [m[1]]
        },
        {
            regex: /^clubs\?name=([^&]+)$/,
            template: "clubs?name={name}",
            extractParams: (m) => [decodeURIComponent(m[1])]
        },
        {
            regex: /^rentals\/(\d+)$/,
            template: "rentals/{rid}",
            extractParams: (m) => [m[1]]
        },
        {
            regex: /^clubs\/(\d+)\/courts\/(\d+)$/,
            template: "clubs/{cid}/courts/{crid}",
            extractParams: (m) => [m[1], m[2]]
        },
        {
            regex: /^rentalsbyuser\/(\d+)$/,
            template: "rentalsbyuser/{uid}",
            extractParams: (m) => [m[1], m[2]]
        },
        {
            regex: /^clubs\/(\d+)\/courts\/(\d+)\/rentals\?date=([\d\-T:]+)$/,
            template: "clubs/{cid}/courts/{crid}/rentals",
            extractParams: (m) => [m[1], m[2], m[3]]
        },
        {
            regex: /^clubs\/(\d+)\/courts\/(\d+)\/availablehours\?date=([\d\-T:]+)$/,
            template: "clubs/{cid}/courts/{crid}/availablehours",
            extractParams: (m) => [m[1], m[2], m[3]]
        },
        {
            regex: /^rentalsbyuser\/(\d+)\$/,
            template: "rentalsbyuser/{uid}",
            extractParams: (m) => [m[1]]
        },
        {
            regex: /^editrental\/(\d+)$/,
            template: "editrental/{rid}",
            extractParams: (m) => [m[1]]
        },
        {
            regex: /^clubs\/(\d+)\/courts\/(\d+)\/rentals$/,
            template: "clubs/{cid}/courts/{crid}/rentals",
            extractParams: (m) => [m[1], m[2]]
        }
    ];

    for (const pattern of patterns) {
        const match = path.match(pattern.regex);
        if (match) {
            const route = routes.find(r => r.pathTemplate === pattern.template);
            if (route) {
                const params = pattern.extractParams(match);
                return (mainContent) => route.handler(mainContent, ...params);
            }
            return notFoundRouteHandler;
        }
    }


    const route = routes.find(r => r.pathTemplate === path);
    return route ? route.handler : notFoundRouteHandler;
}


const router = {
    addRouteHandler,
    getRouteHandler,
    addDefaultNotFoundRouteHandler
}

export default router
