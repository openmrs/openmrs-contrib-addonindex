import {Component} from "react";
import fetch from "isomorphic-fetch";
import AddOn from "./AddOn";

export default class AllAddOns extends Component {

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
        if (this.state.addons) {
            return (
                    <ul>
                        {this.state.addons.map(addon =>
                                                       <AddOn key={addon.uid} addon={addon}/>
                        )}
                    </ul>
            );
        } else {
            return <div>Loading...</div>
        }
        return <h3>All the stuff here</h3>
    }
}