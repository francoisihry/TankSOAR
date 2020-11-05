import React, {Component} from 'react';
import {Provider} from "react-redux";

import {BrowserRouter, Switch, Route} from 'react-router-dom';
import PrivateRoute from "./services/PrivateRoute";

import LoginForm from "./components/Login";
import Home from "./components/Home";
import Settings from "./components/settings/Settings";
import Runbooks from "./components/runbooks/Runbooks";


import store from './store'
import Editor from "./components/runbooks/Editor";
import WindowDimensionsProvider from "./services/WindowDimensionsProvider";


class App extends Component {
    render() {
        return (
            <Provider store={store}>
                <WindowDimensionsProvider>
                    <div className="h-100">
                        <BrowserRouter>
                            <Switch>
                                <Route exact path="/login" component={LoginForm}/>
                                <PrivateRoute path="/settings" component={Settings}/>
                                <PrivateRoute exact path="/runbooks" component={Runbooks}/>
                                <PrivateRoute exact path="/" component={Home}/>
                                <PrivateRoute exact path="/editor/:name" component={Editor}/>
                                <Route component={LoginForm}/>
                            </Switch>
                        </BrowserRouter>
                    </div>

                </WindowDimensionsProvider>


            </Provider>
        );
    }
}

export default App;
