import {
    LOGIN_SUCCESS, LOGIN_FAIL,
    LOGOUT_SUCCESS
} from "../actions/types";

import {returnErrors} from "./errorActions";
import BackendInterface from "../services/BackendInterface";


// login me
export const login = (username, password) => dispatch => {
    BackendInterface.login(username,password)
    .then(res => {
        console.log('res:'+res)
        dispatch({
            type: LOGIN_SUCCESS,
            payload: res.data
        });
    })
        .catch( err => {
            dispatch(returnErrors(err.response.data, err.response.status, 'LOGIN_FAIL'));
            dispatch({
                type: LOGIN_FAIL
            });
        });
}

// logout me
export const logout = () => dispatch => {
    console.log('logging out...')
    BackendInterface.logout()
    .then(res => {
        dispatch({
            type: LOGOUT_SUCCESS,
            payload: res.data
        });
    })
};

