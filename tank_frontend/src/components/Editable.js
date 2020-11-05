import React, {useRef, useState} from "react";
import {EditIcon} from "../styles/icon";
import {cx, css} from "emotion";
import {mainBackgroundColorBlue, mainColorBeige} from "../styles";


const Editable = ({text, onNewVal}) => {
    const [isEditing, setEditing] = useState(false);
    const [content, setContent] = useState(text)
    const inputRef = useRef(null);

    const width = () => {
        return content.length + "ch"
    }

    const style = () => {
        const commonStyles = cx("form-control text-center", css({
            width: width(),
            maxWidth: '150px'
        }))
        return isEditing ? (commonStyles) : cx(commonStyles, css({
            backgroundColor: mainBackgroundColorBlue,
            border: 'none',
            color: mainColorBeige,
        }))
    }

    const keyPress = (e) => {
        // If type enter:
        if (e.keyCode === 13) {
            console.log('value', e.target.value);
            inputRef.current.blur()
            onNewVal(content)
        }
    }

    return (

        <div className="input-group">
            <input
                ref={inputRef} type="text"
                onBlur={() => {
                    setEditing(false)
                    onNewVal(content);
                }}
                value={content}
                onChange={event => setContent(event.target.value)}
                className={style()}
                onKeyDown={keyPress}
            />
            <span className="input-group-addon">
              {isEditing ? null : <EditIcon
                  onClick={() => {
                      setEditing(true);
                      inputRef.current.focus();
                  }}
                  className='ml-2 input-group-addon'/>}
            </span>
        </div>

    );
};

export default Editable;