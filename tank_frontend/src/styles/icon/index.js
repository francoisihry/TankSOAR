import React from "react";
import {css, cx} from "emotion";
import {mainBackgroundColorBlue, mainColorBeige, mainColorBlue, mainColorBlueOnHover} from "../index";
import {ReactComponent as _DeleteUserIcon} from '../../img/delete.svg';
import {ReactComponent as _EditIcon} from '../../img/edit.svg';
import {ReactComponent as _CreateRunbook} from '../../img/create_runbook.svg';
import {ReactComponent as _Watch} from '../../img/eye.svg';

export const WithTextHover = (props) => {
    return (
        <span className={cx('', css({
            '&': {
                position: 'relative',
                display: 'inline-block',
            },
            '& .tooltiptext': {
                visibility: 'hidden',
                backgroundColor: mainColorBeige,
                color: mainBackgroundColorBlue,
                // textAlign: 'center',
                borderRadius: '6px',
                padding: '4px',
                fontSize: '0.7rem',
                fontWeight: 'normal',
                // border: '1px solid',
                bottom: '50%',
                left: '80%',
                /* Position the tooltip */
                position: 'absolute',
                zIndex: '1',
                opacity:'0.8'
            },
            '&:hover .tooltiptext': {
                transitionDelay: '0.7s',
                visibility: 'visible',
            }
        }))}>
            {props.children}
            <span className="tooltiptext">{props.text}</span>
        </span>
    )

}

export const iconStyle = css({
    fill: mainColorBlue,
    '&:hover': {
        cursor: 'pointer',
        fill: mainColorBlueOnHover
    }
})


export const DeleteIcon = (props) => {
    return (
        <WithTextHover text='Delete'>
            <_DeleteUserIcon
                onClick={props.onClick}
                className={cx(props.className, iconStyle, css({
                    width: '20px'
                }))}
                alt="delete"/>
        </WithTextHover>
    )
}

export const EditIcon = (props) => {
    return (
        <WithTextHover text='Edit'>
            <_EditIcon
                onClick={props.onClick}
                className={cx(props.className, iconStyle, css({
                    width: '25px'
                }))}
                alt="edit"/>
        </WithTextHover>
    )
}

export const CreateRunbookIcon = (props) => {
    return (
        <WithTextHover text='Create runbook'>
            <_CreateRunbook
                onClick={props.onClick}
                className={cx(props.className, iconStyle, css({
                    width: '40px'
                }))}
                alt="new runbook"/>
        </WithTextHover>
    )
}

export const WatchIcon = (props) => {
    return (
        <WithTextHover text='Watch'>
            <_Watch
                onClick={props.onClick}
                className={cx(props.className, iconStyle, css({
                    width: '25px'
                }))}
                alt="Watch"/>
        </WithTextHover>
    )
}


