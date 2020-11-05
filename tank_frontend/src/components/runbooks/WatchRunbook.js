import React, {useEffect, useState} from "react";
import {useSelector} from "react-redux";
import AceEditor from "react-ace";
import {getIconByLanguage} from "../../styles/icon/languages";

const WatchRunbook = ({name}) => {
    const runbookList = useSelector(state => state.runbook.all.runbooks)
    const [runbook, setRunbook] = useState(null)


    useEffect(() => {
        const rb = runbookList.filter(function (el) {
            return el.name === name;
        })[0]
        setRunbook(rb)
    }, [runbookList, name])


    return (
        <div>
            {runbook ? (
                <div>

                    <h2>{getIconByLanguage(runbook.language)} {runbook.name}</h2>

                    <AceEditor
                        mode={runbook.language}
                        theme="dracula"
                        value={runbook.content}
                        name={runbook.name}
                        editorProps={{$blockScrolling: true}}
                        // width='100%'
                        // height='95%'
                    />
                </div>
            ):
                <div>Loading...</div>
            }
        </div>
    )
}
export default WatchRunbook