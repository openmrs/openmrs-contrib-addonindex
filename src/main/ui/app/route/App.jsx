import {Component} from "react";
import {Link} from "react-router";

export default class App extends Component {

    render() {
        return (
                <div>
                    <h1>OpenMRS Add-On Index</h1>
                    <Link to="/">Home</Link>
                    <Link to="/about">About</Link>
                    {this.props.children}
                </div>
        )
    }

}