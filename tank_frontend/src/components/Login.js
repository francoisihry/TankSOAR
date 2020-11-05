import React, {useState} from 'react';
import {cx} from "emotion";
import './Login.css';
import {useDispatch, useSelector} from "react-redux";
import {login} from '../actions/authActions'
import {Redirect} from "react-router-dom"
import logo from '../img/logo.png'
import {clearErrors} from "../actions/errorActions";
import {errorMsgStyle} from "../styles/error";
import {buttonStyle} from "../styles/form";


const Login = ( state) => {
    const [inputs, setInputs] = useState({
        username: '',
        password: ''
    });
    const {username, password} = inputs;

    const error = useSelector(state => state.error);
    const isAuthenticated = useSelector(state => state.me.isAuthenticated);
    const dispatch = useDispatch();

    const onChange = (e) => {
        const {name, value} = e.target;
        setInputs(inputs => ({...inputs, [name]: value}));
    }

    const onSubmit = (e) => {
        e.preventDefault();
        if (username && password) {
            // setMsg(null);
            dispatch(clearErrors())
            dispatch(login(username, password));
        }
    }

        const {from} = state.location.state || {from: {pathname: '/'}}

        if (isAuthenticated) {
            return <Redirect to={from}/>
        } else {
            return (
                <div className="d-flex h-100 align-items-center">
                    <form className="form-signin " id="login_form" onSubmit={onSubmit}>
                    <div className="text-center">
                        <img src={logo} alt="logo"/>
                    </div>

                    <input type="text"
                           name="username"
                           id = "login_input"
                           className="form-control"
                           placeholder="User Name"
                           required
                           value={username}
                           onChange={onChange}
                           autoFocus/>

                    <input type="password"
                           name="password"
                           id = "login_input"
                           className="form-control"
                           placeholder="Password"
                           required
                           value={password}
                           onChange={onChange}/>

                    {error.msg ? <div className={errorMsgStyle} id="login_error">{error.msg}</div> : null}

                    <button className={cx('w-100',buttonStyle)}

                            id="login_button"
                            type="submit"
                    >
                        LOGIN
                    </button>
                    <p className="mt-5 mb-3 text-muted ">&copy; Tank SOAR</p>
                    <br/>
                </form>
                </div>
            );
        }
}
export default Login