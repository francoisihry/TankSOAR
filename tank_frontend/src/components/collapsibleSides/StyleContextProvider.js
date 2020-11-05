import React, {useState} from "react";

const StyleContext = React.createContext();


const StyleContextProvider = ({children}) => {
    const [leftWidth, setLeftWidth] = useState('')
    const [rightWidth, setRightWidth] = useState('')
    const [draggingLeft, setDraggingLeft] = useState(false)
    const [draggingRight, setDraggingRight] = useState(false)
    const [leftPercentWidth, setLeftPercentWidth] = useState(10)
    const [rightPercentWidth, setRightPercentWidth] = useState(10)
    const [ref] = useState(React.createRef())


    const updateLeftPercentWidthFromX = (x) => {
        setLeftPercentWidth((x-ref.current.offsetLeft)*100/(window.innerWidth))
    }

    const updateRightPercentWidthFromX = (x) => {
        setRightPercentWidth((ref.current.offsetWidth-(x-ref.current.offsetLeft))*100/window.innerWidth)
    }

    const value = {
        leftWidth: leftWidth,
        rightWidth: rightWidth,
        setLeftWidth: setLeftWidth,
        setRightWidth: setRightWidth,
        draggingLeft: draggingLeft,
        draggingRight: draggingRight,
        setDraggingLeft: setDraggingLeft,
        setDraggingRight: setDraggingRight,

        leftPercentWidth: leftPercentWidth,
        setLeftPercentWidth:setLeftPercentWidth,
        updateLeftPercentWidthFromX:updateLeftPercentWidthFromX,

        rightPercentWidth:rightPercentWidth,
        setRightPercentWidth:setRightPercentWidth,
        updateRightPercentWidthFromX:updateRightPercentWidthFromX,


    }

    return (
        <StyleContext.Provider value={value}>
            <div ref={ref} className='h-100 w-100'>
                {children}
            </div>
        </StyleContext.Provider>
    )
}
export {StyleContextProvider, StyleContext}