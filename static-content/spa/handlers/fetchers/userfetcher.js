import {API_BASE_URL} from "../../router.js";

export async function fetchUsers() {
    try {
        const res = await fetch(`${API_BASE_URL}users`);
        return await res.json();
    } catch {
        return [];
    }
}

export async function fetchUserDetails(uid) {
    try {
        const res = await fetch(`${API_BASE_URL}users/${uid}`);
        if (!res.ok) throw new Error();
        return await res.json();
    } catch {
        return [];
    }
}

export async function getUserByToken(token) {
    try {
        const users = await fetchUsers();
        return users.find(user => user.token === token);
    } catch (error) {
        console.error("Error:", error);
    }
}
