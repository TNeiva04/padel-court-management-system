import {API_BASE_URL} from "../../router.js";

export async function fetchCourtsByClub(cid, limit, skip) {
    try {
        const res = await fetch(`${API_BASE_URL}clubs/${cid}/courts?limit=${limit}&skip=${skip}`);
        return await res.json();
    } catch (err) {
        console.error(`Error fetching courts for club ${cid}:`, err);

        return {courts: []};
    }
}

export async function fetchCourtDetails(crid) {
    try {
        const res = await fetch(`${API_BASE_URL}courts/${crid}`);
        if (!res.ok) throw new Error(`Error fetching court: ${res.status}`);
        return await res.json();
    } catch (err) {
        console.error(`Error fetching details for court ${crid}:`, err);
        return null;
    }
}
