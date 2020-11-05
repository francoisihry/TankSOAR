import React, {Component} from 'react';
import {cx, css} from "emotion";
import Logout from "../Logout";
import Me from "../Me";
import menu_tank_gun from '../../img/menu_tank_gun.png'
import menu_tank_platform from '../../img/menu_tank_platform.png'


import Wheel from "./Wheel";

class Header extends Component {
    render() {
        return (
            <div className="container">
                <div className="row">
                    <div className="col-2">
                    </div>
                    <div className="col-8">
                        <div className="text-center">
                            <img src={menu_tank_gun} alt="tank gun logo"
                                 className={css({
                                     marginBottom: '1px'
                                 })}
                            />
                        </div>
                        <div className={cx("text-center", css({
                            marginBottom: '2px'
                        }))}>
                            <img src={menu_tank_platform} alt="tank platform logo"/>
                        </div>
                        <ul className={cx("nav justify-content-around container", css({
                            border: '2px solid #F4D798',
                            borderRadius: '100px',
                            minHeight: '105px',
                            maxWidth: '350px',
                            paddingLeft: '20px'
                        }))}>

                            <Wheel text="EVENTS" href="/"/>
                            <Wheel text="RUN BOOKS" href="/runbooks"/>

                            <Wheel text="SETTINGS" href="/settings"/>
                            {/*<li className="nav-item">*/}
                            {/*    <a className="nav-link disabled" href="#">Disabled</a>*/}
                            {/*</li>*/}
                        </ul>
                    </div>
                    <div className={cx("col-2 ", css({
                        marginTop: '20px',
                        display: 'flex',
                        alignItems: 'center'
                    }))}>
                        <Me/>
                        <Logout/>
                    </div>
                </div>
            </div>
        );
    }
}

export default Header;