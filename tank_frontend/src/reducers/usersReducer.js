import {
    ALL_USERS_LOAD_FAIL,
    ALL_USERS_LOADED,
    ALL_USERS_LOADING,
} from "../actions/types";


const initialState = {
    users: [],
    loaded: false,
    loading: false,
};

export default function (state = initialState, action) {
    switch (action.type) {
        case ALL_USERS_LOADING:
            return {
                ...state,
                loaded: false,
                loading: true
            };
        case ALL_USERS_LOAD_FAIL:
            return {
                ...state,
                loaded: false,
                loading: false,
            };
        case ALL_USERS_LOADED:
            return {
                ...state,
                users: action.payload,
                loaded: true,
                loading: false
            };
        default:
            return state;
    }
}
