import React, {Component} from 'react';
import {css, cx} from "emotion";
import Header from "./header/Header";
// import Footer from "./Footer";

const Footer = () => {
    const paddingSides = () => {
        return ''
    }

    return (
        <div className={css({marginTop: 'auto'})}>
            <div className={css({
                textAlign: 'center',
                borderTop: '1px solid rgb(221, 221, 221) !important',
                // backgroundColor: 'rgb(247, 247, 247)',
            })}>
                <div className={cx(paddingSides(), css({
                    margin: '0px auto !important',
                }))}>

                    {/*<div className={cx('d-flex justify-content-around', css({*/}
                    {/*    paddingTop: '20px',*/}
                    {/*    paddingBottom: '20px',*/}
                    {/*}))}>*/}
                    {/*    <div>Qui sommes nous ?</div>*/}
                    {/*    <div>Contact</div>*/}
                    {/*    <div>Plan du site</div>*/}

                    {/*</div>*/}

                    <div className={css({
                        // borderTop: '1px solid rgb(221, 221, 221) !important',
                        paddingTop: '24px !important',
                        paddingBottom: '24px !important',
                    })}>
                        © 2020 TankSOAR, Inc. Tous droits réservés
                    </div>


                </div>

            </div>
        </div>

    )
}


class Layout extends Component {

    render() {
        return (
            <div className="h-100 d-flex flex-column">
                <div>
                    <Header/>
                </div>

                {/*<div className="row  h-75 mt-2">*/}
                {/*<div className="col-2">*/}
                {/*    left pannel*/}
                {/*</div>*/}

                <div className='d-flex justify-content-center mt-4'>
                    <div className="col-8 ">
                        {this.props.children}
                    </div>
                </div>


                {/*<div className="col-2">*/}
                {/*    right pannel*/}
                {/*</div>*/}

                {/*</div>*/}
                <Footer/>


            </div>
        );
    }
}

export default Layout;