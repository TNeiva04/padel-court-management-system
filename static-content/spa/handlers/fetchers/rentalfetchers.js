import {API_BASE_URL} from "../../router.js";
import {getCurrentToken} from "../session.js";

export async function fetchRentals(limit, skip) {
    try {
        const res = await fetch(`${API_BASE_URL}rentals?limit=${limit}&skip=${skip}`);
        if (!res.ok) throw new Error();
        return await res.json();
    } catch {
        return [];
    }
}

export async function fetchRentalDetails(rid) {
    try {
        const res = await fetch(`${API_BASE_URL}rentals/${rid}`);
        if (!res.ok) throw new Error();
        return await res.json();
    } catch {
        return [];
    }
}

export async function fetchRentalsById(rid) {
    try {
        const res = await fetch(`${API_BASE_URL}rentals/${rid}`, {
            headers: {
                Authorization: `Bearer ${getCurrentToken()}`,
            },
        });
        if (!res.ok) throw new Error();
        return await res.json();
    } catch {
        return [];
    }
}

export async function updateRental(rid, data) {
    const res = await fetch(`${API_BASE_URL}rentals/${rid}`, {
        method: "PUT",
        headers: {
            Authorization: `Bearer ${getCurrentToken()}`,
            "Content-Type": "application/json",
        },
        body: JSON.stringify(data),
    });

    if (!res.ok) {
        const errorBody = await res.text();
        throw new Error(`HTTP ${res.status}: ${errorBody}`);
    }

    return await res.json();
}

export async function fetchRentalsByUser(uid, limit, skip) {
    try {
        const res = await fetch(`${API_BASE_URL}rentalsbyuser/${uid}?limit=${limit}&skip=${skip}`, {
            headers: {
                Authorization: `Bearer ${getCurrentToken()}`,
            },
        });
        if (!res.ok) throw new Error();
        return await res.json();
    } catch {
        return [];
    }
}

export async function fetchRentalsByClubCourtDate(cid, crid, date, limit, skip) {
    try {
        const res = await fetch(`${API_BASE_URL}clubs/${cid}/courts/${crid}/rentals?date=${date}T00:00:00&limit=${limit}&skip=${skip}`);
        if (!res.ok) throw new Error();
        return await res.json();
    } catch {
        return [];
    }
}

export async function fetchAvailableHours(cid, crid, date, limit, skip) {
    try {
        const res = await fetch(`${API_BASE_URL}clubs/${cid}/courts/${crid}/availablehours?date=${date}&limit=${limit}&skip=${skip}`);
        if (!res.ok) throw new Error();
        return await res.json();
    } catch {
        return [];
    }
}

export async function createReservation(clubId, courtId, data) {
    try {
        const token = getCurrentToken();
        const res = await fetch(`${API_BASE_URL}clubs/${clubId}/courts/${courtId}/rentals`, {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json",
            },
            body: JSON.stringify(data)
        });

        if (!res.ok) throw new Error("Error creating reservation.");
        return await res.json();
    } catch {
        return {success: false, message: "Failed to create reservation."};
    }
}
