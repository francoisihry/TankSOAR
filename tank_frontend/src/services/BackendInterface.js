import axios from "axios";

import store from "../store";
import {logout} from "../actions/authActions";


export const BACKEND_URL = process.env.BACKEND_URL
const GET_ME_URL = '/api/me/'
const GET_USERS_URL = '/api/users/'
const CREATE_USER_URL = '/api/users/'
const DELETE_USER_URL = '/api/users/'
const UPDATE_USER_SETTINGS = '/api/me/settings/'
const RUNBOOKS_URL = '/api/runbooks/'

const SET_CSRF_TOKEN_URL = '/api/set-csrf/'
const LOGIN_URL = '/api/login/'
const LOGOUT_URL = '/api/logout/'


axios.defaults.baseURL = BACKEND_URL
axios.defaults.xsrfHeaderName = "X-CSRFToken"
axios.defaults.xsrfCookieName = 'csrftoken'
axios.defaults.withCredentials = true


axios.interceptors.response.use((response) => {
    return response;
}, (error) => {
    // if session is over, client should be logged out
    if (error.response.status === 401) {
        return new Promise((resolve, reject) => {
            console.log('Session time exceeded')
            store.dispatch(logout());
        });
    }else{ // else the error musts be raised to be properly catch
        return new Promise((resolve, reject) => {
            reject(error);
        });
    }
 })


class BackendInterface {
    static performRequest(method, url, params) {
        const body = method === 'get' ? 'params' : 'data'

        const config = {
            method,
            url,
            [body]: params || {}
        }
        return axios.request(config)
    }

    static login(username, password) {
        // Headers
        const config = {
            headers: {
                'Content-Type': 'application/json'
            }
        }
        // request body
        const body = JSON.stringify({username, password})

        return axios.get(SET_CSRF_TOKEN_URL).then(() =>
            axios.post(LOGIN_URL,body, config)
        )
        // return axios.post(GET_TOKEN_URL, body, config)
    }

    static logout() {
        return this.performRequest('get', LOGOUT_URL)
    }

    static getMe() {
        return this.performRequest('get', GET_ME_URL)
    }

    static getUsers() {
        return this.performRequest('get', GET_USERS_URL)
    }

    static createUser(username, password) {
        // Headers
        const config = {
            headers: {
                'Content-Type': 'application/json'
            }
        }
        // request body
        const body = JSON.stringify({username, password})
        return axios.post(CREATE_USER_URL, body, config)
    }

    static deleteUser(username) {
        return axios.delete(DELETE_USER_URL + username + '/')
    }

    static updateSettingsTimeZone(timeZone) {
        const config = {
            headers: {
                'Content-Type': 'application/json'
            }
        }
        const body = {
            timezone: timeZone
        }
        return axios.patch(UPDATE_USER_SETTINGS, body, config)
    }

    static getAllRunBooks() {
        return this.performRequest('get', RUNBOOKS_URL)
    }

    static getSingleRunBook(name){
        return this.performRequest('get', RUNBOOKS_URL+name+'/')
    }

    static postRunbook(name, language) {
        const runbook_data = {
            name: name,
            language: language,
            content: ''
        }
        return this.performRequest('post', RUNBOOKS_URL, runbook_data)
    }

    static updateRunbook(name, newName=null, newLanguage=null, newContent=null) {
        const config = {
            headers: {
                'Content-Type': 'application/json'
            }
        }
        const body = {}
        if (newName){
            body['name'] = newName
        }
        if (newLanguage){
            body['language'] = newLanguage
        }
        if (newContent){
            body['content'] = newContent
        }
        return axios.patch(RUNBOOKS_URL+name+'/', body, config)
    }

    static deleteRunbook(runbook) {
        return axios.delete(RUNBOOKS_URL + runbook + '/')
    }
}


export default BackendInterface


