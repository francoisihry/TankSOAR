import React from 'react';
import {buttonStyle} from "../../styles/form";


const OkOrCancel =  ({hide, onOk, onCancel, okTxt,cancelTxt, children}) =>{

    const ok = () => {
        if (onOk) {
            onOk()
        }
        hide()

    }

    const cancel = () => {
        hide()
        if (onCancel) {
            onCancel()
        }
    }

    return (
        <div>

            {children}

            <div className="d-flex justify-content-between">
                <button onClick={cancel}
                        className={buttonStyle}>
                    {cancelTxt ? cancelTxt : 'CANCEL'}
                </button>

                <button onClick={ok}
                        type="submit"
                        className={buttonStyle}>
                    {okTxt ? okTxt : 'OK'}
                </button>
            </div>
        </div>
    )
}

export default OkOrCancel;
