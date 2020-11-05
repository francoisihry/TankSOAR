import {
    ME_LOADED, ME_LOAD_FAIL, ME_LOADING,
    SETTINGS_UPDATED, SETTINGS_UPDATING, SETTINGS_UPDATE_FAIL
} from "../actions/types";

import BackendInterface from "../services/BackendInterface";

export const loadMe = () => dispatch => {
    dispatch({type:ME_LOADING});
    BackendInterface.getMe()
        .then(res => dispatch({
            type: ME_LOADED,
            payload: res.data
        }))
        .catch( err => dispatch({
            type: ME_LOAD_FAIL
        }))
}

export const updateTimezone = (timeZone) => dispatch => {
    dispatch({type:SETTINGS_UPDATING});
    BackendInterface.updateSettingsTimeZone(timeZone)
        .then(res => dispatch({
            type: SETTINGS_UPDATED,
            payload: res.data
        }))
        .catch(err => dispatch({
            type: SETTINGS_UPDATE_FAIL
        }))
}

