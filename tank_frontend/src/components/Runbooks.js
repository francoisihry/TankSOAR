import React, {Component, useContext, useEffect, useState} from 'react';
import Layout from "./Layout";
import {ReactComponent as CreateRunbook} from '../img/create_runbook.svg';


import {useDispatch, useSelector} from "react-redux";
import {loadAllRunbooks} from '../actions/runbookActions'
import Modal from "./Modals/Modal";
import OkOrCancel from "./Modals/OkOrCancel";
import BackendInterface from "../services/BackendInterface";
import moment from "moment-timezone";
import {SelectStyles} from "../styles/select";
import Select from "react-select";
import {ReactComponent as EditIcon} from "../img/edit.svg";
import {ReactComponent as DeleteUserIcon} from "../img/delete.svg";
import {useTable} from "react-table";
import {tableStyle} from "../styles/table";
import {css, cx} from "emotion";
import {iconStyle} from "../styles/icon";
import {errorMsgStyle} from "../styles/error";



const CreateRunBookModal = () => {
    const [createRunbookRef] = useState(React.createRef());
    const [error, setError] = useState(null);
    const dispatch = useDispatch();

    const options = [
        {value: 'python', label: 'Python'},
        {value: 'js', label: 'Javascript'},
        {value: 'sh', label: 'Shell'},
    ];
    const [language, setLanguage] = useState(options[0]);
    const [name, setName] = useState("");

    const resetFields = () => {
        setName('');
    }
    const createRunBook = () => {
        BackendInterface.postRunbook(name, language.value).then(res => {
                dispatch(loadAllRunbooks());
                resetFields();
            }
        ).catch(
            err => {
                setError(err.response.statusText);
            }
        )
    };

    const CreateRunbookForm = () => {
        return <OkOrCancel
            onOk={() => {
                console.log('language : ' + language)
                createRunBook()
            }}
            okTxt='CREATE'
            modalRef={createRunbookRef}>
            <div className="">
                Create a new runbook
                <br/>
                <br/>
                <table className="table table-borderless tank_beige_color">
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
                        /></td>
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


            </div>
        </OkOrCancel>
    };

    return (
        <div>
            <Modal ref={createRunbookRef}>
                <CreateRunbookForm/>
            </Modal>

            <CreateRunbook
                onClick={() => createRunbookRef.current.show()}

                className={cx('ml-4',iconStyle, css({
                                          width: '40px'
                                      }))}

                alt="create_user"/>
            {error ? <div className={errorMsgStyle} id="login_error">{error}</div> : null}
        </div>
    )


}

function Table({ columns, data }) {
  // Use the state and functions returned from useTable to build your UI
  const {
    getTableProps,
    getTableBodyProps,
    headerGroups,
    rows,
    prepareRow,
  } = useTable({
    columns,
    data,
  })

  // Render the UI for your table
  return (
    <table
        className={cx("w-100", tableStyle)}
        {...getTableProps()}>
      <thead>
        {headerGroups.map(headerGroup => (
          <tr {...headerGroup.getHeaderGroupProps()}>
            {headerGroup.headers.map(column => (
              <th {...column.getHeaderProps()}>{column.render('Header')}</th>
            ))}
          </tr>
        ))}
      </thead>
      <tbody {...getTableBodyProps()}>
        {rows.map((row, i) => {
          prepareRow(row)
          return (
            <tr {...row.getRowProps()}>
              {row.cells.map(cell => {
                return <td {...cell.getCellProps()}>{cell.render('Cell')}</td>
              })}
            </tr>
          )
        })}
      </tbody>
    </table>
  )
}


const Scripts = () => {
    const runbooks = useSelector(state => state.runbooks.runbooks)
    const dispatch = useDispatch();

    useEffect(() => {
        dispatch(loadAllRunbooks())
    }, [])


    const columns = React.useMemo(
        () => [
        {
            Header: 'Name',
            accessor: 'name'
        },
        {
            Header: 'Language',
            accessor: 'language'
        },
        {
            Header: 'Creation date',
            accessor: 'creation_date',
        }
    ]);


    return (
        <Layout>
            <h1>Scripts</h1>
            <CreateRunBookModal/>

            {runbooks&&<Table columns={columns} data={runbooks} />}

        </Layout>
    );
}

export default Scripts;