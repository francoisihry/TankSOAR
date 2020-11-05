
class SessionStorage {
    static LOCAL_STORAGE_USER = 'user';

    static get user() {
        return localStorage.getItem(this.LOCAL_STORAGE_USER);
    }

    static set user(user) {
        return localStorage.setItem(this.LOCAL_STORAGE_USER, user);
    }

    static isAuthenticated() {
        return this.user !== null;
    }

    static clear() {
        localStorage.removeItem(this.LOCAL_STORAGE_USER);
    }
}

export default SessionStorage;