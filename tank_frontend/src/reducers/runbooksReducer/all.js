import {ALL_RUNBOOKS_LOAD_FAIL, ALL_RUNBOOKS_LOADED, ALL_RUNBOOKS_LOADING} from "../../actions/types";

const initialState = {
    runbooks: [],
    loaded: false,
    loading: false,
    updating: false,
    updated: false,
    error: null
};


const AllRunbooks = (state = initialState, action) => {
    switch (action.type) {
        case ALL_RUNBOOKS_LOADING:
            return {
                ...state,
                loaded: false,
                loading: true,
                error: null
            };
        case ALL_RUNBOOKS_LOAD_FAIL:
            return {
                ...state,
                loaded: false,
                loading: false,
                error: action.payload
            };
        case ALL_RUNBOOKS_LOADED:
            return {
                ...state,
                runbooks: action.payload,
                loaded: true,
                loading: false,
                error: null
            };
        default:
            return state;
    }
}

export default AllRunbooks