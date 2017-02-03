import {Component} from "react";
import {Link} from "react-router";
import fetch from "isomorphic-fetch";
import AddOn from "../component/AddOn";

export default class Show extends Component {

    componentDidMount() {
        fetch('/api/v1/list/' + this.props.params.uid)
                .then(response => {
                    if (response.status >= 400) {
                        throw new Error(response.statusText);
                    }
                    else {
                        return response.json();
                    }
                })
                .then(list => {
                    this.setState({list: list});
                })
                .catch(err => {
                    this.setState({error: err});
                })
    }

    render() {
        if (this.state && this.state.error) {
            return <div>{this.state.error}</div>
        }
        else if (this.state && this.state.list) {
            const list = this.state.list;
            return (
                    <div>
                        <h1>{list.name}</h1>
                        <h3>{list.description}</h3>
                        <ul>
                            {list.addOns.map(addon => {
                                if (addon.version) {
                                    return (
                                            <AddOn key={addon.uid} addon={addon.details} version={addon.version}/>
                                    )
                                } else {
                                    return (
                                            <AddOn key={addon.uid} addon={addon.details}/>
                                    )
                                }
                            })}
                        </ul>
                    </div>
            )
        }
        else {
            return <div>Loading...</div>
        }
    }

}