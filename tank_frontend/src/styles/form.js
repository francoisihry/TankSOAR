import {cx, css} from "emotion";
import {
    mainFormButtonBackgroundColor, mainFormButtonBackgroundColorOnHover,
    mainFormButtonBorderColor,
    mainFormButtonColor, mainFormButtonColorOnHover,
    mainFormButtonFontFamily, mainFormInputBorderColor
} from "./index";

export const buttonStyle = cx('btn', css({
    color: mainFormButtonColor,
    backgroundColor: mainFormButtonBackgroundColor,
    borderColor: mainFormButtonBorderColor,
    textDecoration: 'none',
    borderStyle: 'solid',
    fontFamily: mainFormButtonFontFamily,
    borderWidth: '2px',
    textAlign: 'center',
    verticalAlign: 'middle',
    cursor: 'pointer',
    fontWeight: '400',
    padding: '.5rem 1rem',
    fontSize: '1.25rem',
    lineHeight: '1.5',
    borderRadius: '.3rem',
    width: 'auto',
    '&:hover, &:active': {
        backgroundColor: mainFormButtonBackgroundColorOnHover,
        color: mainFormButtonColorOnHover,

    }
}))

export const inputStyle = css({
    padding: '10px',
    fontSize: '16px',
    textAlign: 'center',
    marginBottom: '20px',
    borderRadius: '5px',
    border: `1px solid ${mainFormInputBorderColor}`,
    boxShadow: 'none',
    width: 'auto',
})