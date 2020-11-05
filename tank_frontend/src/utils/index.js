import {useSelector} from "react-redux";
import React from "react";
import moment from "moment-timezone";


export const TimestampToDate = (props) => {
    const timezone = useSelector(state => state.me.user.settings.timezone)

    return (
        <span>
            {timezone &&
            moment(props.timestamp).tz(timezone).format("YYYY-MM-DD HH:mm:ss")
            }
        </span>
    )
}

export const ErrorResponseFromJson = (jsonVal) => {
    if (jsonVal.constructor === ({}).constructor) {
        let arr = []
    Object.keys(jsonVal).forEach(function(key) {
        const item = <div>{jsonVal[key]}</div>
      arr.push(item);
    });
    return <div>{arr.map(item => item)}</div>
    } else {
        return jsonVal
    }

}