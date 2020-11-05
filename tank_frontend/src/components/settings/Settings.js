import React, {Component} from 'react';

import {
    BrowserRouter as Router,
    Link,
    Route // for later
} from 'react-router-dom'
import Layout from "../Layout";

import Users from "./Users";

function Home() {
    return (
        <h1>
            Settings home
        </h1>
    )
}

class Settings extends Component {
    render() {
        return (
            <Layout>

                <Router>
                    <nav
                        className=" tank_navbar_border navbar navbar-expand-lg  justify-content-center rounded-pill tank_navbar_background_color ">
                        <Link className="navbar-brand tank_navbar_text_color" to="/settings">Settings</Link>

                        <ul className="navbar-nav ">
                            <li className="nav-item active">
                                <Link className="nav-link tank_navbar_text_color" to="/settings">Home <span
                                    className="sr-only">(current)</span></Link>
                            </li>
                            <li className="nav-item">
                                <Link className="nav-link tank_navbar_text_color" to="/settings/users">Users</Link>
                            </li>
                            <li className="nav-item">
                                <Link className="nav-link disabled" to="/settings">Disabled</Link>
                            </li>
                        </ul>
                    </nav>
                    <hr/>
                    <Route exact path='/settings' component={Home}/>
                    <Route exact path='/settings/users' component={Users}/>
                </Router>
            </Layout>
        )
    }
}

export default Settings