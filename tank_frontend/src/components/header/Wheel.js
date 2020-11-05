import React, {Component} from 'react';
import {withRouter} from "react-router-dom";
import {mainColorBlue, mainColorBlueOnHover, mainColorRed} from "../../styles";

class Wheel extends Component {
    constructor(props) {
        super(props);
        this.state = {
            radius: "47",
            bgColor: mainColorBlue,
            onHoverBgColor: mainColorBlueOnHover,
            borderWidth: "2",
            borderColor: "#F4D798"
        };
        this.wheelCircleRef= React.createRef();
        this.onClick = this.onClick.bind(this)
        this.updateBackgroundColor = this.updateBackgroundColor.bind(this)
        this.onHover = this.onHover.bind(this)
        this.onLeave = this.onLeave.bind(this)
        this.isCurrentlySelected = this.isCurrentlySelected.bind(this)
    }

    componentDidMount() {
        this.updateBackgroundColor()
    }

    isCurrentlySelected(){
        const isEqual = this.props.location.pathname === this.props.href
        if (isEqual){
            return isEqual
        }else{
            return this.props.location.pathname.startsWith(this.props.href) && this.props.href !=='/'
        }
    }

    updateBackgroundColor() {
        if (this.isCurrentlySelected()) {
            this.setState(
                {bgColor: mainColorRed,}
            )
        } else{
            this.setState(
                {bgColor: mainColorBlue,}
            )
        }
    }

    onClick = () => {
        this.props.history.push(this.props.href);
    }

    onHover(e) {
        if (!this.isCurrentlySelected()) {
            this.wheelCircleRef.current.style.fill = this.state.onHoverBgColor;
            e.target.style.cursor = "pointer";
        }
    }
    onLeave(e){
        e.target.style.fill = this.state.bgColor;
    }


    render() {
        return (

            <svg viewBox="0 0 100 100" width="100">
                <circle ref={this.wheelCircleRef} fill={this.state.bgColor} stroke={this.state.borderColor} strokeWidth={this.state.borderWidth}
                        cx="50" cy="50" r={this.state.radius} onClick={this.onClick} onMouseOver={this.onHover} onMouseLeave={this.onLeave}/>
                <text className="tank_wheel_text_color tank_wheel_text_size" x="50%" y="50%" textAnchor="middle"
                      dy=".3em" onClick={this.onClick} onMouseOver={this.onHover}>{this.props.text}</text>
            </svg>
        );
    }

}

export default withRouter(Wheel);