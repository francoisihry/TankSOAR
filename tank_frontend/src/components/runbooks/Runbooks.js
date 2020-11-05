import React, {createRef, useEffect, useState} from 'react';
import Layout from "../Layout";

import {useDispatch, useSelector} from "react-redux";
import {loadAllRunbooks} from '../../actions/runbookActions'
import Modal from "../Modals/Modal";
import OkOrCancel from "../Modals/OkOrCancel";
import BackendInterface from "../../services/BackendInterface";
import moment from "moment-timezone";
import {SelectStyles} from "../../styles/select";
import Select from "react-select";
import {useFlexLayout, useSortBy, useTable} from "react-table";
import {fieldTableStyle, tableStyle} from "../../styles/table";
import {css, cx} from "emotion";
import {CreateRunbookIcon, DeleteIcon, EditIcon, WatchIcon} from "../../styles/icon";
import {errorMsgStyle} from "../../styles/error";
import {loadMe} from "../../actions/meActions";
import {getIconByLanguage} from "../../styles/icon/languages";
import {useHistory} from "react-router-dom";
import {buttonStyle} from "../../styles/form";
import {ErrorResponseFromJson} from "../../utils";
import WatchRunbook from "./WatchRunbook";


const CreateRunBookForm = ({modalRef}) => {
    const [error, setError] = useState('');
    const dispatch = useDispatch();

    const options = [
        {value: 'python', label: 'Python'},
        {value: 'js', label: 'Javascript'},
        {value: 'sh', label: 'Shell'},
    ];
    const [language, setLanguage] = useState(options[0]);
    const [name, setName] = useState("");
    const [submitted, setSubmitted] = useState(false);

    function onSubmit(e) {
        e.preventDefault();
        setSubmitted(true);
        if (language && name) {
            BackendInterface.postRunbook(name, language.value)
                .then(() => {
                        dispatch(loadAllRunbooks());
                        modalRef.current.hide();
                    }
                ).catch(
                err => {
                    setError(ErrorResponseFromJson(err.response.data));
                }
            )

        }

    }

    return (
        <div>
            <form onSubmit={onSubmit}>
                Create a new runbook
                <br/>
                <br/>
                <table className={fieldTableStyle}>
                    <tbody>
                    <tr>
                        <th className="align-right" scope="row">Name:</th>
                        <td><input type="text"
                                   id="runbook-name-input"
                                   name="name"
                                   required
                                   value={name}
                                   onChange={(e) => setName(e.target.value)}
                                   autoFocus
                                   className={'form-control' + (submitted && !name ? ' is-invalid' : '')}
                        />
                            {submitted && !name &&
                            <div className="invalid-feedback">Playbook name is required</div>
                            }

                        </td>
                    </tr>
                    <tr>
                        <th className="align-right" scope="row">Language:</th>
                        <td>
                            <Select
                                value={language}
                                onChange={(selectedOption) => setLanguage(selectedOption)}
                                options={options}
                                styles={SelectStyles}
                            />
                        </td>
                    </tr>
                    </tbody>
                </table>
                <div className="d-flex justify-content-between">
                    <button type='button' onClick={modalRef.current.hide}
                            className={buttonStyle}>
                        CANCEL
                    </button>

                    <button onClick={onSubmit}
                            type="submit"
                            className={buttonStyle}>
                        CREATE
                    </button>
                </div>
                {error ? <div className={errorMsgStyle} id="login_error">{error}</div> : null}
            </form>
        </div>
    )


}

function Table({columns, data}) {
    // Use the state and functions returned from useTable to build your UI
    const {
        getTableProps,
        getTableBodyProps,
        headerGroups,
        rows,
        prepareRow,
    } = useTable(
        {columns, data},
        useSortBy,
        useFlexLayout
    )

    // Render the UI for your table
    return (
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
    )
}


const Runbooks = () => {
    const [error, setError] = useState(null);
    const runbooks = useSelector(state => state.runbook.all.runbooks)
    const timezone = useSelector(state => state.me.user.settings.timezone)
    const dispatch = useDispatch();

    useEffect(() => {
        dispatch(loadAllRunbooks())
        dispatch(loadMe())
    }, [dispatch])

    const deleteRunbook = (runbook) => {
        BackendInterface.deleteRunbook(runbook).then(
            () => {
                dispatch(loadAllRunbooks())
                setError(null)
            }
        ).catch(
            err => {
                setError(err.response.statusText)
            }
        )
    }

    const history = useHistory();
    const columns = React.useMemo(
        () => [
            {
                Header: 'Name',
                accessor: 'name'
            },
            {
                Header: 'Language',
                accessor: 'language',
                Cell: (props) => {
                    return getIconByLanguage(props.value)
                },
            },
            {
                Header: 'Last update',
                accessor: 'updated_at',
                Cell: (props) => {
                    let dateTime = ''
                    if (props.value) {
                        dateTime = moment(props.value).tz(timezone).format("YYYY-MM-DD HH:mm:ss");
                    }
                    return <span>{dateTime}</span>
                },
            },
            {
                Header: 'Edit',
                width: 60,
                accessor: 'edit',
                Cell: ({row}) => {
                    const deleteModalRef = createRef()
                    return <div>
                        <div className={cx("justify-content-around",
                            css({
                                display: 'none',
                                'tr:hover &': {
                                    display: 'flex'
                                }
                            }))}>
                            <div>
                                <EditIcon onClick={() => history.push('/editor/' + row.original.name)}/>
                            </div>
                            <div>
                                <Modal>
                                    <show>
                                        <WatchIcon/>
                                    </show>
                                    <WatchRunbook name={row.original.name}/>

                                </Modal>
                            </div>
                            <div>
                                <Modal ref={deleteModalRef}>
                                    <show>
                                        <DeleteIcon/>
                                    </show>
                                    <OkOrCancel
                                        hide={() => deleteModalRef.current.hide()}
                                        onOk={() => deleteRunbook(row.original.name)}
                                        okTxt='DELETE'
                                    >
                                        Delete {row.original.name} ?
                                    </OkOrCancel>
                                </Modal>
                            </div>


                        </div>
                    </div>
                }
            },
        ], [deleteRunbook, history, timezone]);

    const createRunbookModalRef = createRef();
    return (
        <Layout>
            <h1>Runbooks</h1>

            <Modal ref={createRunbookModalRef}>
                <show>
                    <CreateRunbookIcon className='ml-4'/>
                </show>
                <CreateRunBookForm modalRef={createRunbookModalRef}/>
            </Modal>

            <br/>

            {error ? <div className={errorMsgStyle}>{error}</div> : null}

            {runbooks && <Table columns={columns} data={runbooks}/>}

        </Layout>
    );
}

export default Runbooks;