import React from 'react';
import { Route, Redirect } from 'react-router-dom';
import {useSelector} from "react-redux";


const PrivateRoute = ({component: Component, ...rest}) => {
    const auth = useSelector(state => state.me)
    return (
        <Route
            {...rest}
            render={props => {
                if (auth.isLoading) {
                    return <h2>Loading...</h2>;
                } else if (!auth.isAuthenticated) {
                    return <Redirect to={{
                        pathname: '/login',
                        state: {from: props.location}
                    }}/>;
                } else {
                    return <Component {...props} />;
                }
            }

            }
        />)
};
export default PrivateRoute


// const PrivateRoute = ({ component: Component, auth, ...rest }) => (
//   <Route
//     {...rest}
//     render={props => {
//         if (auth.isLoading){
//             return <h2>Loading...</h2>;
//         } else if (!auth.isAuthenticated){
//             return <Redirect to={{
//               pathname: '/login',
//               state: { from: props.location }
//             }} />;
//         } else{
//             return <Component {...props} />;
//         }
//     }
//
//     }
//   />
// );
//
// const mapStateToProps = state => ({
//     auth: state.auth,
// });
// export default connect(mapStateToProps)(PrivateRoute);