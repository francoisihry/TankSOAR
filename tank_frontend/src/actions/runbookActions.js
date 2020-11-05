import {
    ALL_RUNBOOKS_LOADING,
    ALL_RUNBOOKS_LOAD_FAIL,
    ALL_RUNBOOKS_LOADED,
    SINGLE_RUNBOOK_LOADED,
    SINGLE_RUNBOOK_LOADING,
    SINGLE_RUNBOOK_LOAD_FAIL, SINGLE_RUNBOOK_UPDATING, SINGLE_RUNBOOK_UPDATED, SINGLE_RUNBOOK_UPDATE_FAILED
} from "../actions/types";

import BackendInterface from "../services/BackendInterface";

export const loadAllRunbooks = () => dispatch => {
    dispatch({type:ALL_RUNBOOKS_LOADING});
    BackendInterface.getAllRunBooks()
        .then(res => dispatch({
            type: ALL_RUNBOOKS_LOADED,
            payload: res.data
        }))
        .catch( err => dispatch({
            type: ALL_RUNBOOKS_LOAD_FAIL
        }))
}

export const loadSingleRunbook = (name) => dispatch => {
    dispatch({type:SINGLE_RUNBOOK_LOADING});
    BackendInterface.getSingleRunBook(name)
        .then(res => dispatch({
            type: SINGLE_RUNBOOK_LOADED,
            payload: res.data
        }))
        .catch( err => dispatch({
            type: SINGLE_RUNBOOK_LOAD_FAIL,
            payload: err
        }))
}

export const updateRunbook = (name, newName=null, newLanguage=null, newContent=null) => dispatch => {
    dispatch({type:SINGLE_RUNBOOK_UPDATING});
    BackendInterface.updateRunbook(name, newName, newLanguage, newContent)
        .then(res => dispatch({
            type: SINGLE_RUNBOOK_UPDATED,
            payload: res.data
        }))
        .catch( err => dispatch({
            type: SINGLE_RUNBOOK_UPDATE_FAILED,
            payload: err
        }))
}

