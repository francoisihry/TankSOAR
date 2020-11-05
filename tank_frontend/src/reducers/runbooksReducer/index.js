import {combineReducers} from "redux";

import AllRunbooks from "./all";
import SingleRunbook from "./single";

export default combineReducers({
    all: AllRunbooks,
    single: SingleRunbook
});