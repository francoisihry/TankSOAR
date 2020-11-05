import {cx, css} from "emotion";


export const modalPositionStyle = css({
    marginTop: '20vh',
    transform: 'translateY(-20%)',
    textAlign: 'center',
})

export const modalCloseStyle = cx('close', 'text-right', css({
    '&:hover, &:focus': {
        color: '#000',
        textDecoration: 'none',
        cursor: 'pointer',

    }
}))

export const modalContentStyle = css({
    backgroundColor: 'var(--main-background-color-blue)',
    padding: '20px 30px 10px 30px',
})


export const modalBackgroundStyle = css({
    display: 'block',
    position: 'fixed',
    zIndex: '1',
    paddingTop: '100px',
    left: '0',
    top: '0',
    width: '100%',
    height: '100%',
    overflow: 'auto',
    backgroundColor: 'rgba(0,0,0,0.4)',
    '&:hover, &:focus': 'none'
})


export const modalBoxStyle = css({
    display: 'inline-block',
    minWidth: '300px',
    minHeight: '100px',
    backgroundColor: 'var(--main-background-color-blue)',
    border: '1px solid #888',
    paddingRight: '10px',
})

