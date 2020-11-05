import React, {Component} from 'react';
import {ReactComponent as LogoutIcon} from '../img/logout.svg';

import {useDispatch} from 'react-redux'
import {logout} from "../actions/authActions";
import {css, cx} from "emotion";
import {iconStyle} from "../styles/icon";

const Logout = ( state) =>{
    const dispatch = useDispatch();
    return (
        <div>
            <LogoutIcon onClick={()=> {
                console.log('clicked')
                dispatch(logout())
            }} to="/login"
                        className={cx(iconStyle, css({
                            width: '30px',
                            marginLeft: '25px'
                        }))}
                        alt="logout"/>
        </div>
    );
}

export default Logout;