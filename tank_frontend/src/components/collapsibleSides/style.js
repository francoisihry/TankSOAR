import {cx, css, injectGlobal} from "emotion";
import {ReactComponent as _RightChevron} from '../../img/chevron-right.svg';
import {ReactComponent as _LeftChevron} from '../../img/chevron-left.svg';
import React, {useContext, useEffect, useState} from "react";
import {iconStyle} from "../../styles/icon";
import {mainBackgroundColorBlue, mainColorBlue, mainColorBlueOnHover} from "../../styles";
import {StyleContext} from "./StyleContextProvider";

export const ICON_WIDTH = 40
const LIMIT_WIDTH = 7
const LIMIT_WIDTH_ON_HOVER = 10
const LIMIT_COLOR = mainColorBlue
const LIMIT_COLOR_ON_HOVER = mainColorBlueOnHover
const LIMIT_COLOR_ON_DRAGGING = mainColorBlueOnHover

export const MainContentStyle = cx(css({
    width: '100%',
    height: '100%',
    marginRight: `${ICON_WIDTH}px`,
    marginLeft: `${ICON_WIDTH}px`
}))


const sideIconeStyle = cx(css({
    margin: '0',
    position: 'absolute',
    top: '50%',
    msTransform: 'translateY(-50%)',
    transform: 'translateY(-50%)',
    zIndex: '10',
    background: mainBackgroundColorBlue
}))

const RightChevronIcon = ({onClick, className}) => {
    return (
        <_RightChevron
            onClick={onClick}
            className={cx(className, iconStyle, sideIconeStyle, css({
                width: `${ICON_WIDTH}px`,
                left:'0px'
            }))}
            alt=""/>
    )
}

const LeftChevronIcon = ({onClick, className}) => {
    return (
        <_LeftChevron
            onClick={onClick}
            className={cx(className, iconStyle, sideIconeStyle, css({
                width: `${ICON_WIDTH}px`,
                right:'0px'
            }))}
            alt=""/>
    )
}

const limitStyle = cx(css({
    // border: '1px solid red',
    height: '100%',
    borderRadius: '20px',
    background: LIMIT_COLOR,
    width: `${LIMIT_WIDTH}px`,
    margin: '0',
    position: 'absolute',
    top: '50%',
    msTransform: 'translateY(-50%)',
    transform: 'translateY(-50%)',
    '&:hover': {
        cursor: 'pointer',
        background: LIMIT_COLOR_ON_HOVER,
        width: `${LIMIT_WIDTH_ON_HOVER}px`,
    }
}))


const DraggableLimitLeft = ({children}) => {
    const [dragging, setDragging] = useState(false)
    const styleCtx = useContext(StyleContext)

    const onMouseMove = (e) => {
        if (dragging) {
            styleCtx.updateLeftPercentWidthFromX(e.pageX)
        }
        e.stopPropagation()
        e.preventDefault()
    }

    const onMouseUp = (e) => {
        setDragging(false)
        styleCtx.setDraggingLeft(false)
        e.stopPropagation()
        e.preventDefault()
    }

    useEffect(() => {
        if (dragging) {
            document.addEventListener('mousemove', onMouseMove)
            document.addEventListener('mouseup', onMouseUp)
            document.body.style.cursor = 'grabbing'
            return () => {
                document.removeEventListener('mousemove', onMouseMove)
                document.removeEventListener('mouseup', onMouseUp)
                document.body.style.cursor = 'default'
            }
        }
    }, [dragging])

    const onMouseDown = (e) => {
        console.log('e.pageX : ' + e.pageX)
        setDragging(true);
        styleCtx.setDraggingLeft(true)
        e.stopPropagation()
        e.preventDefault()
    }

    const draggingLimitStyle = () => {
        return dragging ? cx(css({
            background: LIMIT_COLOR_ON_DRAGGING,
            width: `${LIMIT_WIDTH_ON_HOVER}px`,
        })) : ''
    }

    return (
        <div onMouseDown={onMouseDown}
             className={cx('d-flex justify-content-center',
                 limitStyle,
                 draggingLimitStyle(),
                 css({right: '-10px'})
             )}>
            {children}
        </div>
    )
}

export const LeftIcon = ({isOpen, onClick}) => {
    return (
        isOpen ?
            <DraggableLimitLeft>
                <LeftChevronIcon onClick={onClick}/>
            </DraggableLimitLeft>
            :
            <RightChevronIcon onClick={onClick}/>
    )
}


const DraggableLimitRight = ({children}) => {
    const [dragging, setDragging] = useState(false)
    const styleCtx = useContext(StyleContext)

    const onMouseMove = (e) => {
        if (dragging) {
            styleCtx.updateRightPercentWidthFromX(e.pageX)
        }
        e.stopPropagation()
        e.preventDefault()
    }

    const onMouseUp = (e) => {
        setDragging(false)
        styleCtx.setDraggingRight(false)
        e.stopPropagation()
        e.preventDefault()
    }

    useEffect(() => {
        if (dragging) {
            document.addEventListener('mousemove', onMouseMove)
            document.addEventListener('mouseup', onMouseUp)
            document.body.style.cursor = 'grabbing'
            return () => {
                document.removeEventListener('mousemove', onMouseMove)
                document.removeEventListener('mouseup', onMouseUp)
                document.body.style.cursor = 'default'
            }
        }
    }, [dragging])

    const onMouseDown = (e) => {
        setDragging(true);
        styleCtx.setDraggingRight(true)
        e.stopPropagation()
        e.preventDefault()
    }

    const draggingLimitStyle = () => {
        return dragging ? cx(css({
            background: LIMIT_COLOR_ON_DRAGGING,
            width: `${LIMIT_WIDTH_ON_HOVER}px`,
            '&:hover':{
                background: LIMIT_COLOR_ON_DRAGGING,
            }
        })) : ''
    }

    return (
        <div onMouseDown={onMouseDown}
             className={cx('d-flex justify-content-center',
                 limitStyle,
                 css({left: '-10px'}),
                 draggingLimitStyle()
             )}>
            {children}
        </div>
    )
}


export const RightIcon = ({isOpen, onClick, className}) => {
    const rightStyle = cx(css({left: '-' + `${ICON_WIDTH}px`}))

    return (
        isOpen ?
            <DraggableLimitRight>
                <RightChevronIcon onClick={onClick}/>
            </DraggableLimitRight>
            :
            <LeftChevronIcon onClick={onClick}
                             className={cx(rightStyle, className)}/>
    )
}



