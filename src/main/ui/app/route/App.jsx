import {Component} from "react";
import {Link} from "react-router";
import ListOfLists from "../component/ListOfLists";

export default class App extends Component {

    render() {
        return (
                <div className="container-fluid">
                    <header className="clearfix row vertical-align-center">
                        <h1 className="col-sm-5">
                            <a href="#/">
                                <img className="logo logo1" src="/images/logo.png" alt="OpenMRS Add-Ons Logo"/>
                            </a>
                        </h1>
                        <div className="col-sm-7">
                            <ListOfLists/>
                        </div>
                    </header>
                    {this.props.children}
                </div>
        )
    }

}