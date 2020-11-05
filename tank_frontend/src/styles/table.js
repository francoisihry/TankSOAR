import {css, cx} from "emotion";
import {mainBackgroundColorBlue, mainColorBeige, mainColorBlue} from "./index";

// react table style for displaying incidents, users etc...
export const tableStyle = css({
    borderSpacing: '0',
    border: `1px solid ${mainColorBeige}`,

    "& td, & th": {
        textAlign: 'center',
        margin: '0',
        padding: '0.5rem',
        borderBottom: `1px solid ${mainColorBeige}`,
        borderRight: `1px solid ${mainColorBeige}`,
        position: 'relative',
    },

    "& tr:nth-child(even)": {
        backgroundColor: '#597C99',
    },

    "& tbody tr:hover": {
        backgroundColor: '#C6BCA3',
        color: mainBackgroundColorBlue,
        fontWeight: 'bold',
    },

    '& th::before': {
        position: 'absolute',
        right: '15px',
        top: '16px',
        content: "",
        width: '0',
        height: '0',
        borderLeft: '5px solid transparent',
        borderRight: '5px solid transparent',
    },

    '& th.sort-asc::before':{
        borderBottom: '5px solid #22543d'
    },

    '& th.sort-desc::before': {
        borderTop: '5px solid #22543d'
    },
})

// Simple table style for displaying fields
export const fieldTableStyle = cx("table table-borderless ", css({
                            color: mainColorBeige
                        }))




