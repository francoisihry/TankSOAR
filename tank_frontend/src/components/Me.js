import React from 'react';
import {ReactComponent as MeIcon} from '../img/me.svg';
import User from "./User";
import Modal from "./Modals/Modal";
import {css, cx} from "emotion";
import {iconStyle} from "../styles/icon";


const Me = () => {

    // const userModalRef = useRef();


        return (
            <div>
                <Modal >
                    <show>
                        <MeIcon
                            // onClick={() => userModalRef.current.show()}
                        className={cx(iconStyle, css({
                                          width: '25px'
                                      }))}
                        alt="me"/>
                    </show>
                    <User/>
                </Modal>

            </div>
        );
}

export default Me;