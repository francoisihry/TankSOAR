import {injectGlobal} from "emotion";

// Fonts
export const mainFontFamily = 'Candara'
export const mainFormButtonFontFamily = 'Impact';

// Colors
export const mainColorBeige = '#E8DFD1';
export const mainColorRed = '#852E30';
export const mainColorBlue = '#6CA2C3';
export const mainColorBlueOnHover = '#8BB3C6';
export const mainBackgroundColorBlue = '#36566B';

export const mainMsgErrorColor = '#FFB5B2';
export const mainMsgErrorBackgroundColor = '#452C2F';
export const mainMsgErrorBorderColor = '#573238';

export const mainFormButtonColor = '#F5D7A8';
export const mainFormButtonBackgroundColor = '#6CA2C3';
export const mainFormButtonBorderColor = '#2F5184';
export const mainFormButtonColorOnHover = mainColorBeige;
export const mainFormButtonBackgroundColorOnHover = '#8BB3C6';

export const mainFormInputBorderColor = '#6d7a7a';
export const mainColor = mainColorBeige;
export const mainFontweight = 'normal'

injectGlobal(
    {
        'html, body, #root': {
            fontFamily: mainFontFamily,
            height: '100%'
        },
        body: {
            margin: 0,
            fontFamily: mainFontFamily,
            '-webkit-font-smoothing': 'antialiased',
            '-moz-osx-font-smoothing': 'grayscale',
            backgroundColor: mainBackgroundColorBlue,
            color: mainColor,
            fontWeight: mainFontweight,
        }
    }
);