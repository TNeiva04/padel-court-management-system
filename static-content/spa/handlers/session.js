let _mockToken = JSON.parse(sessionStorage.getItem("currentUserMocker")) || null;

let _currentUser = _mockToken === null
    ? JSON.parse(sessionStorage.getItem("currentUser")) || null
    : JSON.parse(sessionStorage.getItem("currentUserMocker"));

export let currentUser = _currentUser;

export function setMockToken(token) {
    _mockToken = token;
    if (token) {
        sessionStorage.setItem("currentUserMocker", JSON.stringify(token));
    } else {
        sessionStorage.removeItem("currentUserMocker");
    }
    currentUser = _mockToken || JSON.parse(sessionStorage.getItem("currentUser")) || null;
}

export function clearMockToken() {
    _mockToken = null;
    sessionStorage.removeItem("currentUserMocker");
    currentUser = JSON.parse(sessionStorage.getItem("currentUser")) || null;
}

export function setCurrentUser(user) {
    sessionStorage.setItem("currentUser", JSON.stringify(user));
    if (!_mockToken) {
        currentUser = user;
    }
}

export function getCurrentUser() {
    return currentUser;
}

export function getCurrentToken() {
    return currentUser;
}


// export let currentUser = JSON.parse(sessionStorage.getItem("currentUser")) ||null
//
// let _mockToken = null;
//
// export function setMockToken(token) {
//     console.log("www",token)
//     _mockToken = token;
//     sessionStorage.setItem("currentUserMocker", JSON.stringify(token));
//
// }
//
// export function clearMockToken() {
//     _mockToken = null;
// }
//
// export function setCurrentUser(user) {
//     currentUser = user;
//     sessionStorage.setItem("currentUser", JSON.stringify(user));
// }
//
// export function getCurrentUser() {
//     return currentUser;
// }
//
// export function getCurrentToken() {
//     return currentUser;
// }
//
