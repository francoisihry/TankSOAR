// import {
//     AUTH_ERROR, LOGIN_SUCCESS, LOGIN_FAIL,
//     LOGOUT_SUCCESS
// } from "../actions/types";
//
// import TokenStorage from "../services/TokenStorage";
//
// const initialState = {
//     isAuthenticated: false,
//     isRefreshing: false,
// };
//
// export default function (state=initialState, action) {
//     console.log('type:'+action.type)
//     switch (action.type) {
//         case LOGIN_SUCCESS:
//             console.log('storing refresh : '+action.payload.refresh)
//             // TokenStorage.token = action.payload.access;
//             // TokenStorage.refreshToken = action.payload.refresh;
//             return {
//                 ...state,
//                 ...action.payload,
//                 isAuthenticated: true,
//                 isRefreshing: false
//             };
//         case AUTH_ERROR:
//         case LOGIN_FAIL:
//         case LOGOUT_SUCCESS:
//             TokenStorage.clear()
//             return {
//                 ...state,
//                 token: null,
//                 isAuthenticated: false,
//                 isRefreshing: false
//             };
//         default:
//             return state;
//
//     }
//
// }