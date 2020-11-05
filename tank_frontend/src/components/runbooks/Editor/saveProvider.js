import React, {useState} from "react";
import {useDispatch} from "react-redux";
import {updateRunbook} from "../../../actions/runbookActions";


const SaveContext = React.createContext();

const SaveProvider = ({runbook, children}) => {
    const dispatch = useDispatch();
    const [newLanguage, setNewLanguage] = useState(null)
    const [newName, setNewName] = useState(null)
    const [newContent, setNewContent] = useState(null)

    const clear = () => {
        setNewLanguage(null);
        setNewName(null);
        setNewContent(null);
    }


    const save = () => {
        dispatch(updateRunbook(runbook.name,newName, newLanguage, newContent))
        clear()
    }

    const shouldBeSave = () => {
        return (newLanguage !== null) || (newName !== null) || (newContent !== null)
    }


    const value = {
        newLanguage: newLanguage,
        newName: newName,
        newContent: newContent,
        setNewLanguage: setNewLanguage,
        setNewName: setNewName,
        setNewContent: setNewContent,
        shouldBeSave: shouldBeSave,
        save: save,
        reset:clear
    }

    return (
        <SaveContext.Provider value={value}>
            {children}
        </SaveContext.Provider>
    )


}
export {SaveProvider, SaveContext}