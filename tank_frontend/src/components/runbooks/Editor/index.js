import React, {useContext, useEffect} from "react";
import {useDispatch, useSelector} from "react-redux";
import {loadSingleRunbook} from "../../../actions/runbookActions";
import {getIconByLanguage} from "../../../styles/icon/languages";
import AceEditor from "react-ace";
import {ErrorResponseFromJson, TimestampToDate} from "../../../utils";
import logo from '../../../img/logo.png'
import "ace-builds/src-noconflict/mode-python";

import "ace-builds/src-noconflict/theme-dracula";

import "ace-builds/src-noconflict/ext-language_tools"
import {Debug, ResetIcon, Run, SaveIcon, Stop, Undo} from "./style";
import Select from "react-select";
import {SelectStyles} from "../../../styles/select";
import {fieldTableStyle} from "../../../styles/table";
import {SaveProvider, SaveContext} from "./saveProvider";
import {errorMsgStyle} from "../../../styles/error";
import Editable from "../../Editable";
import {css, cx} from "emotion";
import CollapsibleSides from "../../collapsibleSides";

const Header = () => {
    return (
        <div className='d-flex'>
            <img src={logo} alt="logo" width='100px' className='mt-2 ml-4 mb-2'/>
            <h1 className='text-center w-100'>Runbook Editor</h1>
        </div>
    )
}

const Save = () => {
    const saveCtx = useContext(SaveContext)
    const error = useSelector(state => state.runbook.errorUpdating);

    return (
        <div className='text-center'>
            {saveCtx.shouldBeSave() ?
                <div className='mb-2'>
                    <SaveIcon
                        onClick={saveCtx.save}
                    />

                    <ResetIcon
                        onClick={saveCtx.reset}
                        className='ml-5'
                    />
                </div>
                : null}
            {error && (<div className={errorMsgStyle} id="save_error">
                {ErrorResponseFromJson(error.response.data)}

            </div>)}
        </div>
    )

}

const Description = () => {
    const saveCtx = useContext(SaveContext)
    const runbook = useSelector(state => state.runbook.single.runbook)
    // const [newLanguage, setNewLanguage] = useState(null)

    const toLang = {
        python: 'Python',
        js: 'JavaScript',
        sh: 'Shell'
    }

    const SelectableElement = (lang) => {
        return {
            value: lang,
            label: <div>{getIconByLanguage(lang)} {toLang[lang]}</div>
        }
    }

    const language = () => {
        return saveCtx.newLanguage ? saveCtx.newLanguage : runbook.language
    }

    const Options = () => {
        const languages = ['python', 'js', 'sh']
        languages.splice(languages.indexOf(language()), 1)
        return languages.map(l => SelectableElement(l))
    }

    const onSelect = (val) => {
        // saveCtx.setNewLanguage(val)
        saveCtx.setNewLanguage(val)
    }

    return (
        <div className="d-flex justify-content-center align-items-center">
            <div className="center-block">
                <Save/>

                <table className={fieldTableStyle}>
                    <tbody>
                    <tr>
                        <th className="align-right" scope="row">Name:</th>

                        <td><Editable text={runbook.name}
                                      onNewVal={val => val !== runbook.name ? saveCtx.setNewName(val) : null}/></td>

                    </tr>

                    <tr>
                        <th className="align-right" scope="row">Language:</th>
                        <td>
                            <div style={{display: 'inline-block', minWidth: '150px'}}>
                                <Select
                                    menuColor='red'
                                    styles={SelectStyles}
                                    value={SelectableElement(language())}
                                    onChange={(selectedOption) => onSelect(selectedOption.value)}
                                    options={Options()}
                                />
                            </div>
                        </td>
                    </tr>

                    <tr>
                        <th className="align-right" scope="row">Creation:</th>
                        <td>
                            <TimestampToDate timestamp={runbook.created_at}/>
                        </td>
                    </tr>

                    <tr>
                        <th className="align-right" scope="row">Last update:</th>
                        <td>
                            <TimestampToDate timestamp={runbook.updated_at}/>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
    )
}


const Output = () => {
    return (
        <div>
            <h2 className='text-center'>Output</h2>
            output... output... output... output... output... output... output... output... output...
            <br/>
            output... output... output... output... output... output... output... output... output... output...
            output... output... output... output... output...
            output... output... output... output... output... output... output... output... output... output...
            output...
            output... output... output... output... output... output... output... output... output... output...
            output...
            output... output... output... output... output... output... output... output... output... output...
            output...
            output... output... output... output... output... output... output... output... output... output...
            output...
        </div>
    )
}

const Index = (props) => {
    const dispatch = useDispatch();
    const runbook = useSelector(state => state.runbook.single.runbook)
    const error = useSelector(state => state.runbook.single.errorLoading)

    useEffect(() => {
        dispatch(loadSingleRunbook(props.match.params.name))
    }, [dispatch, props.match.params.name])

    const ProgrammingInterface = () => {
        return (
            <div className='ml-4 mb-2'>
                <Run className='ml-3'/>
                <Debug className='ml-3'/>
                <Stop className='ml-3'/>
                <Undo className='ml-3'/>
            </div>
        )

    }


    const Settings = () => {
        return (
            <div>
                <div className="d-flex justify-content-around">
                    <Description runbook={runbook}/>
                </div>
            </div>
        )
    }

    const Editor = () => {
        const saveCtx = useContext(SaveContext)
        const runbook = useSelector(state => state.runbook.single.runbook)

        return (
            <div className='h-100 w-100'>
                <ProgrammingInterface/>
                <AceEditor
                    mode={runbook.language}
                    theme="dracula"
                    onChange={val => saveCtx.setNewContent(val)}
                    value={saveCtx.newContent ? saveCtx.newContent : runbook.content}
                    name={runbook.name}
                    editorProps={{$blockScrolling: true}}
                    width='100%'
                    setOptions={{
                        enableBasicAutocompletion: true,
                        enableLiveAutocompletion: true,
                        enableSnippets: true
                    }}
                    height='85%'
                />
            </div>
        )
    }

    return (
        <SaveProvider runbook={runbook}>
            <div className='h-100'>
                {error ? (
                        <div className="d-flex h-100 align-items-center justify-content-center w-100">
                            <div className={cx(errorMsgStyle, 'd-flex align-items-center justify-content-center h-25 w-25')}
                                 id="save_error">
                                {ErrorResponseFromJson(error.response.data)}
                            </div>
                        </div>
                    )
                    : runbook && (
                    <div
                        className="h-100"
                    >
                        <div>
                            <Header/>
                        </div>

                        <div className='pt-5 h-100'>
                            <CollapsibleSides
                                leftContent={<Settings/>}
                                leftPercentWidth='20'
                                mainContent=<Editor/>
                            rightContent=<Output/>
                            rightPercentWidth='35'
                            leftMinWidth='310px'
                            rightMinWidth='300px'
                            />
                        </div>
                    </div>

                )
                }
            </div>
        </SaveProvider>

    )

}

export default Index