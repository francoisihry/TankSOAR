import React from "react";
import {css, cx} from "emotion";
import {ReactComponent as _Run} from "../../../img/run.svg";
import {ReactComponent as _Stop} from "../../../img/stop.svg";
import {ReactComponent as _Undo} from "../../../img/undo.svg";
import {ReactComponent as _Save} from "../../../img/save.svg";
import {ReactComponent as _Debug} from "../../../img/debug.svg";
import {ReactComponent as _RightPanel} from '../../../img/border_right.svg';
import {ReactComponent as _BottomPanel} from '../../../img/border_bottom.svg';
import {ReactComponent as _NoOutput} from '../../../img/no_output.svg';
import {iconStyle, WithTextHover} from "../../../styles/icon";
import {
    mainColorBlue,
    mainColorBlueOnHover,
    mainColorRed
} from "../../../styles";

// Programming interface
export const Run = (props) => {
    return (
        <WithTextHover text='Run'>
            <_Run
                onClick={props.onClick}
                className={cx(props.className, iconStyle, css({
                    width: '20px'
                }))}
                alt="run"/>
        </WithTextHover>
    )
}

export const Debug = (props) => {
    return (
        <WithTextHover text='Debug'>
            <_Debug
                onClick={props.onClick}
                className={cx(props.className, iconStyle, css({
                    width: '25px'
                }))}
                alt="debug"/>
        </WithTextHover>
    )
}

export const Stop = (props) => {
    return (
        <WithTextHover text='Stop'>
            <_Stop
                onClick={props.onClick}
                className={cx(props.className, iconStyle, css({
                    width: '20px'
                }))}
                alt="stop"/>
        </WithTextHover>

    )
}

export const Undo = (props) => {
    return (
        <WithTextHover text='Undo'>
            <_Undo
                onClick={props.onClick}
                className={cx(props.className, iconStyle, css({
                    width: '20px'
                }))}
                alt="undo"/>
        </WithTextHover>

    )
}

// Settings
export const SaveIcon = (props) => {
    return (
        <WithTextHover text='Save'>
            <_Save
                onClick={props.onClick}
                className={cx(props.className, iconStyle, css({
                    width: '40px'
                }))}
                alt="save"/>

        </WithTextHover>

    )
}

export const ResetIcon = (props) => {
    return (
        <WithTextHover text='Reset'>
            <_Undo
                onClick={props.onClick}
                className={cx(props.className, iconStyle, css({
                    width: '40px'
                }))}
                alt="reset"/>

        </WithTextHover>

    )
}

// Layout control

const layoutStyle = (selected) => {
    const fillColor = selected ? mainColorRed : mainColorBlue
    const fillHover = selected ? mainColorRed : mainColorBlueOnHover

    return css({
        fill: fillColor,
        '&:hover': {
            cursor: 'pointer',
            fill: fillHover
        }
    })
}


export const RightPanel = (props) => {
    return (
        <WithTextHover text='To Right Panel'>
            <_RightPanel
                onClick={props.onClick}
                className={cx(props.className, layoutStyle(props.selected), css({
                    width: '35px',
                    // fill: '#C99A67'
                }))}
                alt="To Right Panel">
            </_RightPanel>
        </WithTextHover>
    )
}

export const BottomPanel = (props) => {
    return (
        <WithTextHover text='To Bottom Panel'>
            <_BottomPanel
                onClick={props.onClick}
                className={cx(props.className, layoutStyle(props.selected), css({
                    width: '35px',
                    // fill: '#C99A67'
                }))}
                alt="To Bottom Panel">
            </_BottomPanel>
        </WithTextHover>
    )
}

export const NoOutput = (props) => {
    return (
        <WithTextHover text='Disable Output'>
            <_NoOutput
                onClick={props.onClick}
                className={cx(props.className, layoutStyle(props.selected), css({
                    width: '45px',
                    // fill: '#C99A67'
                }))}
                alt="Disable Output">
            </_NoOutput>
        </WithTextHover>
    )
}