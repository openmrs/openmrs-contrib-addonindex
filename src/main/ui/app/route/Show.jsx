import {Component} from "react";
import fetch from "isomorphic-fetch";

export default class Show extends Component {

    componentDidMount() {
        fetch('/api/v1/addon/' + this.props.params.uid)
                .then(response => {
                    if (response.status >= 400) {
                        throw new Error(response.statusText);
                    }
                    else {
                        return response.json();
                    }
                })
                .then(addon => {
                    this.setState({addon: addon});
                })
                .catch(err => {
                    this.setState({error: err});
                })
    }

    render() {
        if (this.state && this.state.error) {
            return <div>{this.state.error}</div>
        }
        else if (this.state && this.state.addon) {
            const addon = this.state.addon;
            const icon = addon.type === 'OWA' ? 'fa fa-globe' : 'fa fa-puzzle-piece';
            return (
                    <div>
                        <h1>
                            <i className={icon} aria-hidden="true"></i>
                            {addon.name}
                        </h1>
                        <h3>{addon.description}</h3>
                        Versions:
                        <ul>
                            {addon.versions.map(v => {
                                return (
                                        <li>
                                            Version {v.version}
                                            <a href={v.downloadUri}>Download</a>
                                            { v.renameTo ? `Rename this file to ${v.renameTo}` : "" }
                                        </li>
                                )
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