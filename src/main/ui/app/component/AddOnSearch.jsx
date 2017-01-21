import {Component} from "react";
import fetch from "isomorphic-fetch";
import AddOnList from "./AddOnList";

export default class AddOnSearch extends Component {

    constructor(props) {
        super(props);
        this.state = {};
    }

    componentDidMount() {
        fetch('/api/v1/addon')
                .then(response => {
                    return response.json();
                })
                .then(json => {
                    this.setState({addons: json});
                })
    }

    render() {
        return (
                <AddOnList addons={this.state.addons}/>
        )
    }
}