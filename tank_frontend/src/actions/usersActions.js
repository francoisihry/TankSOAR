import {
    ALL_USERS_LOADING,
    ALL_USERS_LOAD_FAIL,
    ALL_USERS_LOADED
} from "../actions/types";

import BackendInterface from "../services/BackendInterface";

export const loadAllUsers = () => dispatch => {
    dispatch({type:ALL_USERS_LOADING});
    BackendInterface.getUsers()
        .then(res => dispatch({
            type: ALL_USERS_LOADED,
            payload: res.data
        }))
        .catch( err => dispatch({
            type: ALL_USERS_LOAD_FAIL
        }))
}
