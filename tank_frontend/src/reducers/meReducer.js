import {
    LOGIN_SUCCESS, LOGIN_FAIL,
    LOGOUT_SUCCESS, ME_LOADED, ME_LOAD_FAIL, ME_LOADING,
    SETTINGS_UPDATED, SETTINGS_UPDATING, SETTINGS_UPDATE_FAIL, AUTH_ERROR
} from "../actions/types";
import SessionStorage from "../services/SessionStorage";


const initialState = {
    isAuthenticated: SessionStorage.isAuthenticated(),
    isRefreshing: false,
    user: JSON.parse(SessionStorage.user),
    userLoading: false,
    userUpdating: false
};

export default function (state = initialState, action) {
    switch (action.type) {
        case LOGIN_SUCCESS:
            const user = action.payload
            SessionStorage.user = JSON.stringify(action.payload)
            return {

                ...state,
                ...action.payload,
                isAuthenticated: true,
                isRefreshing: false,
                user:user,
            };
        case AUTH_ERROR:
        case LOGIN_FAIL:
        case LOGOUT_SUCCESS:
            SessionStorage.clear()
            return {
                ...state,
                isAuthenticated: false,
                isRefreshing: false
            };

        case ME_LOADING:
            return {
                ...state,
                userLoading: true
            };
        case SETTINGS_UPDATE_FAIL:
        case ME_LOAD_FAIL:
            return {
                ...state,
                userLoading: false,
                userUpdating: false
            };
        case ME_LOADED:
            return {
                ...state,
                user: action.payload,
                userLoading: false,
                userUpdating: false
            };
        case SETTINGS_UPDATING:
            return {
                ...state,
                userUpdating: true,
            };
        case SETTINGS_UPDATED:
            console.log('payload:'+JSON.stringify(action.payload))

            return {
                ...state,
                userLoading: false,
                userUpdating: false,
                user: {
                    ...state.user,
                    settings: action.payload
                }
            };
        default:
            return state;

    }

}