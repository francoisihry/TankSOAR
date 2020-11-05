import React, {useEffect, useState} from 'react';
import {css, cx} from "emotion";

import {ReactComponent as UserIcon} from '../img/id_card.svg';
import {ReactComponent as SaveIcon} from '../img/save.svg';
import {ReactComponent as UndoIcon} from '../img/undo.svg';

import {useDispatch, useSelector} from "react-redux";
import Select from 'react-select';
import moment from "moment-timezone"

import {loadMe, updateTimezone} from '../actions/meActions'
import {SelectStyles} from '../styles/select'
import {mainColorBeige} from "../styles";
import {iconStyle} from "../styles/icon";
import {fieldTableStyle} from "../styles/table";

const User = () => {
    const user = useSelector(state => state.me.user)
    const dispatch = useDispatch();
    const [timeZone, setTimeZone] = useState(null)

    useEffect(() => {
        dispatch(loadMe())
    }, [dispatch]);

    useEffect(() => {
        if (user) {
            setTimeZone(user.settings.timezone);
        }
    }, [user]);

    const SelectableElement = (val) => {
        const element = {
            value: val,
            label: val
        }
        return element;
    }

    const saveNewSettings = () => {
        dispatch(updateTimezone(timeZone))
    }

    const newSettingsToBeSaved = () => {
        return timeZone !== user.settings.timezone
    }

    const undo = () => {
        setTimeZone(user.settings.timezone)
    }

    return (

        <div className="row justify-content-center align-items-center">
            {user ?
                <div
                    className={cx("d-flex",
                        css({
                            '& .table td, & .table th ': {verticalAlign: 'middle'}
                        }))}>
                    <UserIcon
                        className={css({
                                fill: mainColorBeige,
                                width: '300px',
                                marginRight: '20px'
                            }
                        )}
                        alt="User"/>
                    <div className="center-block">
                        <table className={fieldTableStyle}>
                            <tbody>
                            <tr>
                                <th className="align-right" scope="row">User name:</th>
                                <td>{user.username}</td>
                            </tr>

                            <tr>
                                <th className="align-right" scope="row">Email:</th>
                                <td>{user.email}</td>
                            </tr>
                            <tr>
                                <th className="align-right" scope="row">First name:</th>
                                <td>{user.first_name}</td>
                            </tr>
                            <tr>
                                <th className="align-right" scope="row">Last name:</th>
                                <td>{user.last_name}</td>
                            </tr>
                            <tr>
                                <th className="align-right" scope="row">Last login:</th>
                                <td>
                                    {/*{me.last_login}*/}
                                    {moment(user.last_login).tz(user.settings.timezone)
                                        .format("YYYY-MM-DD HH:mm:ss")}
                                </td>
                            </tr>
                            <tr>
                                <th className="align-right" scope="row">Timezone:</th>
                                <td>
                                    <div style={{display: 'inline-block', minWidth: '260px'}}>
                                        <Select
                                            menuColor='red'
                                            value={SelectableElement(timeZone)}
                                            onChange={(selectedOption) => setTimeZone(selectedOption.value)}
                                            options={moment.tz.names().map(tz => SelectableElement(tz))}
                                            styles={SelectStyles}
                                        />
                                    </div>
                                </td>
                            </tr>
                            </tbody>
                        </table>

                        {newSettingsToBeSaved() ?
                            <div className="d-flex justify-content-around">
                                <UndoIcon
                                    onClick={undo}
                                    className={cx(iconStyle, css({
                                          width: '25px'
                                      }))}
                                    alt="undo"/>
                                <SaveIcon
                                    onClick={saveNewSettings}
                                    className={cx(iconStyle, css({
                                          width: '25px'
                                      }))}
                                    alt="save"/>
                            </div> : null}
                    </div>

                </div> : <div>Loading...</div>

            }
        </div>
    );
}

export default User;