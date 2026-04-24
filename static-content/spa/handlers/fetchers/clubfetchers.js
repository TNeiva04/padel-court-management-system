import {API_BASE_URL} from "../../router.js";
import {currentUser, getCurrentToken} from "../session.js";

export async function fetchClubs(pageSize, skip) {
    const url = `${API_BASE_URL}clubs?limit=${pageSize}&skip=${skip}`;
    const response = await fetch(url);

    if (!response.ok) {
        throw new Error(`Error fetching clubs: ${response.status} ${response.statusText}`);
    }

    return response.json();
}

export async function fetchClubByID(cid) {
    try {
        const res = await fetch(`${API_BASE_URL}clubs/${cid}`);
        if (!res.ok) throw new Error();
        return await res.json();
    } catch {
        return [];
    }
}

export async function fetchClubsByUser(uid) {
    try {
        const token = getCurrentToken();
        const res = await fetch(`${API_BASE_URL}clubsbyuser/${uid}`, {
            headers: {
                "Authorization": `Bearer ${token}`,
                "Accept": "application/json"
            }
        });

        if (!res.ok) throw new Error("Error fetching user's clubs.");
        return await res.json();
    } catch {
        return {clubs: []};
    }
}

export async function fetchClubsByName(name, limit, skip) {
    try {
        const res = await fetch(`${API_BASE_URL}clubs?name=${encodeURIComponent(name)}&limit=${limit}&skip=${skip}`);
        if (!res.ok) throw new Error();
        return await res.json();
    } catch {
        return [];
    }
}

export async function createClubPost(newName) {
    try {
        const res = await fetch(`${API_BASE_URL}clubs`, {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${currentUser}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify({name: newName.trim()})
        });

        if (!res.ok) throw new Error(`Error creating club: ${res.status} ${res.statusText}`);
        return await res.json();
    } catch (error) {
        console.error("Error in createClub:", error);
        return null;
    }
}
