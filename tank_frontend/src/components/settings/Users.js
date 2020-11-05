import React, {createRef, useEffect, useState} from "react";
import BackendInterface from "../../services/BackendInterface";
import {useTable, useFilters, useSortBy, useFlexLayout} from "react-table";
import {ReactComponent as CreateUserIcon} from '../../img/create_user.svg';
import Modal from "../Modals/Modal";
import OkOrCancel from "../Modals/OkOrCancel";

import moment from "moment-timezone"
import {useDispatch, useSelector} from "react-redux";
import {loadMe} from "../../actions/meActions";
import {cx, css} from 'emotion'
import {tableStyle} from "../../styles/table";
import {DeleteIcon, EditIcon, iconStyle} from "../../styles/icon";
import {errorMsgStyle} from "../../styles/error";
import {buttonStyle, inputStyle} from "../../styles/form";
import User from "../User";
import {loadAllUsers} from "../../actions/usersActions";
import {ErrorResponseFromJson} from "../../utils";


const CreateUserForm = ({hide}) => {
    const [inputs, setInputs] = useState({
        username: '',
        password: ''
    });
    const {username, password} = inputs;
    const [submitted, setSubmitted] = useState(false);
    const [msg, setMsg] = useState(null);
    const dispatch = useDispatch();

    function handleChange(e) {
        const {name, value} = e.target;
        setInputs(inputs => ({...inputs, [name]: value}));
    }

    function onSubmit(e) {
        e.preventDefault();

        setSubmitted(true);
        if (username && password) {
            BackendInterface.createUser(username, password)
                .then(() => {
                    hide();
                    dispatch(loadAllUsers())
                })
                .catch(err => {
                    setMsg(ErrorResponseFromJson(err.response.data));
                });
        }
    }

    return (
        <form onSubmit={onSubmit}>
            <div className="form-group">
                <label>Username</label>
                <input type="text" name="username" value={username} onChange={handleChange}
                       className={'form-control' + (submitted && !username ? ' is-invalid' : '')}/>
                {submitted && !username &&
                <div className="invalid-feedback">Username is required</div>
                }
            </div>
            <div className="form-group">
                <label>Password</label>
                <input type="password" name="password" value={password} onChange={handleChange}
                       className={'form-control' + (submitted && !password ? ' is-invalid' : '')}/>
                {submitted && !password &&
                <div className="invalid-feedback">Password is required</div>
                }
            </div>

            {msg ?
                <div className={errorMsgStyle}>{msg}</div> : null}

            <div className="text-center">
                <button onClick={onSubmit}
                    //btn btn-lg btn-block tank_form_button tank_modal_ok_button
                        className={cx("float-right row", buttonStyle)}>
                    OK
                </button>
            </div>
        </form>
    );

}


function Table({data, columns, error}) {
    const [filterInput, setFilterInput] = useState("");

    const {
        getTableProps,
        getTableBodyProps,
        headerGroups,
        rows,
        prepareRow,
        setFilter
    } = useTable(
        {columns, data},
        useFilters,
        useSortBy,
        useFlexLayout
    )

    const handleFilterChange = e => {
        const value = e.target.value || undefined;
        setFilter("username", value);
        setFilterInput(value);
    };

    const createUserModalRef = createRef()
    return (
        <div>
            <div className="text-center">
                <input
                    className={inputStyle}
                    value={filterInput}
                    onChange={handleFilterChange}
                    placeholder={"Search me name"}
                />

                <div className={'d-inline-flex'}>
                    <Modal ref={createUserModalRef}>
                        <show>
                            <CreateUserIcon
                                className={cx('ml-4', iconStyle, css({
                                    width: '45px'
                                }))}
                                alt="create_user"/>
                        </show>
                        <CreateUserForm hide={() => createUserModalRef.current.hide()}/>
                    </Modal>
                </div>
            </div>


            {error ? <div className={errorMsgStyle}>{error}</div> : null}

            <table
                className={cx("w-100", tableStyle)}
                {...getTableProps()} >
                <thead>
                {headerGroups.map(headerGroup => (
                    <tr {...headerGroup.getHeaderGroupProps()}>
                        {headerGroup.headers.map(column => (
                            <th
                                {...column.getHeaderProps()}
                            >
                                {column.render('Header')}
                            </th>
                        ))}
                    </tr>
                ))}
                </thead>
                <tbody {...getTableBodyProps()}>
                {rows.map(row => {
                    prepareRow(row)
                    return (
                        <tr {...row.getRowProps()}>
                            {row.cells.map(cell => {
                                return (
                                    <td
                                        {...cell.getCellProps()}
                                    >
                                        {cell.render('Cell')}
                                    </td>
                                )
                            })}

                        </tr>
                    )
                })}
                </tbody>
            </table>
        </div>

    )
}


function Users() {
    const [error, setError] = useState(null);
    const timezone = useSelector(state => state.me.user.settings.timezone)
    const users = useSelector(state => state.users.users)
    const dispatch = useDispatch();

    useEffect(() => {
        dispatch(loadAllUsers())
        dispatch(loadMe())
    }, [dispatch])

    const deleteUser = (username) => {
        BackendInterface.deleteUser(username).then(
            () => {
                dispatch(loadAllUsers())
                setError(null)
            }
        ).catch(
            err => {
                setError(err.response.statusText)
            }
        )
    }

    const columns = React.useMemo(
        () => [
            {
                Header: 'User name',
                accessor: 'username'
            },
            {
                Header: 'Email',
                accessor: 'email'
            },
            {
                Header: 'Last login',
                accessor: 'last_login',
                Cell: (props) => {

                    const dateTime = moment(props.value).tz(timezone).format("YYYY-MM-DD HH:mm:ss");

                    return <span>{dateTime}</span>
                },
            },
            {
                Header: 'Edit',
                width: 50,
                accessor: 'edit',
                Cell: ({row}) => {
                    const deleteUserModalRef = createRef()
                    return <div>
                        <div className={cx("justify-content-around ",
                            css({
                                display: 'none',
                                'tr:hover &': {
                                    display: 'flex'
                                }
                            }))}>
                            <div>
                                <EditIcon/>
                            </div>
                            <div>
                                <Modal ref={deleteUserModalRef}>
                                    <show>
                                        <DeleteIcon/>
                                    </show>
                                    <OkOrCancel
                                        hide={() => deleteUserModalRef.current.hide()}
                                        onOk={() => deleteUser(row.original.username)}
                                        okTxt='DELETE'
                                    >
                                        Delete {row.original.username} ?
                                    </OkOrCancel>
                                </Modal>
                            </div>


                        </div>
                    </div>
                }
            },
        ],
        [deleteUser, timezone]);
    const userModalRef = createRef();
    return (
        <div>
            <Modal ref={userModalRef}>
                <User/>
            </Modal>

            <Table data={users} columns={columns}
                   error={error}/>
            <hr/>
        </div>
    );
}

export default Users;