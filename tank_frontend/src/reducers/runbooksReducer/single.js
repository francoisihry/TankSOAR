import {
    SINGLE_RUNBOOK_LOAD_FAIL,
    SINGLE_RUNBOOK_LOADED,
    SINGLE_RUNBOOK_LOADING,
    SINGLE_RUNBOOK_UPDATE_FAILED,
    SINGLE_RUNBOOK_UPDATED,
    SINGLE_RUNBOOK_UPDATING
} from "../../actions/types";

const initialState = {
    loaded: false,
    loading: false,
    updating: false,
    updated: false,
    runbook: null,
    errorLoading: null,
    errorUpdating:null,
};

const SingleRunbook = (state = initialState, action) => {
    switch (action.type) {
        case SINGLE_RUNBOOK_UPDATING:
            return {
                ...state,
                updating: true,
                updated: false,
                loaded: false,
                loading: false,
                errorUpdating: null
            };
        case SINGLE_RUNBOOK_UPDATED:
            return {
                ...state,
                runbook: action.payload,
                updating: false,
                updated: true,
                loaded: false,
                loading: false,
                errorUpdating: null
            };
        case SINGLE_RUNBOOK_UPDATE_FAILED:
            return {
                ...state,
                updating: false,
                updated: false,
                loaded: false,
                loading: false,
                errorUpdating: action.payload
            };
        case SINGLE_RUNBOOK_LOADING:
            return {
                ...state,
                loaded: false,
                loading: true,
                errorLoading: null
            };
        case SINGLE_RUNBOOK_LOAD_FAIL:
            return {
                ...state,
                loaded: false,
                loading: false,
                errorLoading: action.payload
            };
        case SINGLE_RUNBOOK_LOADED:
            return {
                ...state,
                runbook: action.payload,
                loaded: true,
                loading: false,
                errorLoading: null
            };
        default:
            return state;
    }
}

export default SingleRunbook