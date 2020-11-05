import React, {Component, forwardRef, useImperativeHandle, useState} from 'react';
import Modal from "./Modal";
import {buttonStyle} from "../../styles/form";


const Ok = forwardRef((props, ref) => {

    const onOk = () => {
        if (props.onOk) {
            props.onOk()
        }
        props.modalRef.current.hide()

    }

    return (
        <div>
            {props.children}
            <br/>
            <br/>
                <button onClick={onOk}
                        className={buttonStyle}>
                    {props.okTxt ? props.okTxt : <div>OK</div>}
                </button>

        </div>
    )
})

export default Ok;
