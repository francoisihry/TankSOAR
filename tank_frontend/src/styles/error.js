import {cx, css} from "emotion";
import {mainMsgErrorBackgroundColor, mainMsgErrorBorderColor, mainMsgErrorColor} from "./index";

export const errorMsgStyle = cx('alert alert-warning',css({
    color: mainMsgErrorColor,
    backgroundColor: mainMsgErrorBackgroundColor,
    borderColor: mainMsgErrorBorderColor
}))