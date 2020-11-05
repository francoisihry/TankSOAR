import {ReactComponent as _BashLogo} from '../../img/languages/bash.svg';
import {ReactComponent as _PythonLogo} from '../../img/languages/python.svg';
import {ReactComponent as _JSLogo} from '../../img/languages/js.svg';
import {css, cx} from "emotion";
import React from "react";
import {mainBackgroundColorBlue, mainColorBeige, mainColorBlue, mainColorBlueOnHover} from "../index";
import {WithTextHover} from "./index";

const LanguageStyle = css({
    // fill: '#A7B748',
    fill: '#83A060',
    '&:hover':{
        cursor: 'pointer',
        // fill: mainColorBlueOnHover
    }

})



export const BashLogo = (props) => {
    return (
        <WithTextHover text='Shell'>
            <_BashLogo
                className={cx(props.className, LanguageStyle, css({
                    width: '35px',
                    // fill: '#C99A67'
                }))}
                alt="shell">
            </_BashLogo>
        </WithTextHover>
    )
}

export const PythonLogo = (props) => {
    return (
        <WithTextHover text='Python'>
            <_PythonLogo
            className={cx(props.className,'mr-1', LanguageStyle, css({
                width: '30px',
                // fill: '#80D674',
            }))}
            alt="shell"/>
        </WithTextHover>
    )
}

export const JSLogo = (props) => {
    return (
        <WithTextHover text='JavaScript'>
            <_JSLogo
            className={cx(props.className,'mr-1', LanguageStyle, css({
                width: '28px',
            }))}
            alt="JS"/>
        </WithTextHover>
    )
}

export const  getIconByLanguage = (language) => {
    switch (language) {
        case 'python':
            return <PythonLogo/>
        case 'sh':
            return <BashLogo/>
        case 'js':
            return <JSLogo/>
    }
}