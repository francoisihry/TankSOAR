import React, {useContext, useEffect, useState} from 'react';
import {css, cx} from "emotion";
import {
    ICON_WIDTH,
    LeftIcon, MainContentStyle, RightIcon
} from "./style";
import {StyleContext, StyleContextProvider} from "./StyleContextProvider";
import {useWindowDimensions} from "../../services/WindowDimensionsProvider";

const SECS = '0.4s'

const transitionStyle = cx(css({
    transitionDuration: SECS,
    transitionProperty: `transform opacity width`,
}));

const getWidthStyle = (isSideOpen, percentWidth, minWidth = '0px', maxWidth) => {
    return isSideOpen ? cx(css({
        width: `calc(1vw * ${percentWidth})`,
        minWidth: minWidth,
        maxWidth:maxWidth,
    })) : cx(css({
        width: '0',
    }))
}

const getSidebarStyle = (isSideOpen, percentWidth, minWidth = '0px', maxWidth) => {
    return isSideOpen ? cx(css({
        width: `calc(1vw * ${percentWidth})`,
        minWidth: minWidth,
        maxWidth:maxWidth,
        zIndex: '5',
        opacity: '1'
    })) : cx(css({
        // width: `calc(1vw * ${percentWidth})`,
        width: `0px`,
        zIndex: '-5',
        opacity: '0'
    }))

}

const Left = ({leftContent, leftPercentWidth, minWidth}) => {
    const [leftOpen, setLeftOpen] = useState(true)
    const styleCtx = useContext(StyleContext)
    const {width} = useWindowDimensions()
    const maxWidth = `${0.7*width}px`

    useEffect(() => {
        styleCtx.setLeftPercentWidth(leftPercentWidth)
    }, [])

    const transigiton = () => {
        return styleCtx.draggingLeft ? '' : transitionStyle
    }

    const WidthStyle = () => {
        return getWidthStyle(leftOpen, styleCtx.leftPercentWidth, minWidth, maxWidth)
    }

    const sidebarStyle = () => {
        const space = leftOpen ? css({paddingRight: `${ICON_WIDTH}px`}) : ''
        return getSidebarStyle(leftOpen, styleCtx.leftPercentWidth, space, minWidth, maxWidth)
    }


    const leftContentStyle = () => {
        return leftOpen ? '' : cx(css({transform: 'translateX(-100%)',}))
    }

    return (
        <div>
            {leftContent && <div id='left' className={cx(
                'h-100',
                transigiton(),
                WidthStyle(),
            )}>
                <div className={cx('position-relative', css({height: '50%'})
                )}>
                    <div className={cx(
                        transigiton(),
                        sidebarStyle(),
                        leftContentStyle(),
                        css({minWidth:minWidth})
                    )}>
                        {leftContent}
                    </div>
                    <div className="float-right">
                        <LeftIcon
                            isOpen={leftOpen}
                            onClick={() => setLeftOpen(!leftOpen)}
                        />
                    </div>
                </div>
            </div>}
        </div>
    )
}

const Right = ({rightContent, rightPercentWidth, minWidth}) => {
    const [rightOpen, setRightOpen] = useState(true)
    const styleCtx = useContext(StyleContext)
    const {width} = useWindowDimensions()
    const maxWidth = `${0.8*width}px`

    useEffect(() => {
        styleCtx.setRightPercentWidth(rightPercentWidth)
    }, [])

    const transigiton = () => {
        return styleCtx.draggingRight ? '' : transitionStyle
    }

    const WidthStyle = () => {
        return getWidthStyle(rightOpen, styleCtx.rightPercentWidth, minWidth, maxWidth)
    }

    const sidebarStyle = () => {
        const space = rightOpen ? css({paddingLeft: `${ICON_WIDTH}px`}) : ''
        return cx(getSidebarStyle(rightOpen, styleCtx.rightPercentWidth, minWidth, maxWidth), space, )
    }

    return (
        <div>
            {rightContent && <div id='right' className={cx(
                'h-100',
                transigiton(),
                WidthStyle(),
            )}>

                <div className={cx('position-relative', css({height: '50%'}))}>
                    <div className={cx(
                        transigiton(),
                        sidebarStyle(),
                    )}>
                        <div className={cx('d-flex', css({overflow: 'hidden'}))}>
                            {rightContent}
                        </div>

                    </div>
                    <div className="float-left">
                        <RightIcon
                            isOpen={rightOpen}
                            onClick={() => setRightOpen(!rightOpen)}
                        />
                    </div>
                </div>
            </div>}
        </div>
    )
}


const CollapsibleSides = ({
                              mainContent, leftContent, rightContent, rightMinWidth, leftMinWidth,
                              leftPercentWidth = 25, rightPercentWidth = 25
                          }) => {
    return (
        <StyleContextProvider>
            <div className='d-flex h-100'>
                <Left leftContent={leftContent} leftPercentWidth={leftPercentWidth} minWidth={leftMinWidth}/>
                <div className={cx('h-100', MainContentStyle)}>
                    {mainContent}
                </div>
                <Right rightContent={rightContent} rightPercentWidth={rightPercentWidth} minWidth={rightMinWidth}/>
            </div>
        </StyleContextProvider>
    );
}
export default CollapsibleSides;

