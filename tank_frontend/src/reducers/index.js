import {combineReducers} from "redux";

import errorReducer from './errorReducer'
// import authReducer from './authReducer'
import meReducer from "./meReducer";
import runbookReducer from "./runbooksReducer";
import usersReducer from "./usersReducer";

export default combineReducers({
    error: errorReducer,
    // auth: authReducer,
    me: meReducer,
    runbook: runbookReducer,
    users: usersReducer
});