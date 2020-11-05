import React, {useState, forwardRef, useImperativeHandle, useEffect} from "react";
import {
    modalBackgroundStyle,
    modalBoxStyle,
    modalCloseStyle,
    modalContentStyle,
    modalPositionStyle
} from "../../styles/modal";
import {css, cx} from "emotion";
import {mainColor, mainFontFamily, mainFontweight} from "../../styles";


const Content = ({children, hide}) => {
    const keyPress = (e) => {
        // If type echap
        if (e.keyCode === 27) {
            hide()
        }
    }
    useEffect(() => {
        document.addEventListener("keydown", keyPress, false);

        return () => {
            document.removeEventListener("keydown", keyPress, false);
        };
    }, []);

    return (
        <div>
            {React.Children.toArray(children).filter(child => child.type !== 'show')}
        </div>
    )
}

const Modal = forwardRef((props, ref) => {
    const [showState, setShowState] = useState(null);

    const hide = () => {
        if (props.onclose) {
            props.onclose()
        }
        ;
        setShowState(false);
    };

    const modalContent = <div className={modalBackgroundStyle}>
        <div className={modalPositionStyle}>
            <div className={modalBoxStyle}>
                                    <span className={modalCloseStyle}
                                          onClick={hide}>&times;</span>
                <div className={modalContentStyle}>
                    <Content hide={hide}>
                        {props.children}
                    </Content>
                </div>
            </div>
        </div>
    </div>

    const clickable = (child) => {
        child = React.cloneElement(
            child,
            {onClick: () => setShowState(true)}
        )
        return child
    }

    useImperativeHandle(ref, () => ({
        show: () => {
            setShowState(true)
        },

        hide: () => {
            if (props.onclose) {
                props.onclose()
            }
            setShowState(false)
        }
    }));

    return (
        <div className={cx(css({
            fontFamily: mainFontFamily,
            color: mainColor,
            fontWeight: mainFontweight,
            margin: 'auto',
            padding: 'auto'

        }))}>
            {showState && modalContent}
            {React.Children.toArray(props.children).filter(child => child.type === 'show').map(
                child => clickable(child)
            )
            }
        </div>
    )
})

export default Modal