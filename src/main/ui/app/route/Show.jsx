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

    formatRequiredModules(version) {
        let requirements = [];
        if (version.requireModules) {
            for (let key in version.requireModules) {
                requirements.push(key.replace("org.openmrs.module.", ""));
            }
        }
        return requirements.join(", ");
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
                                            <span>
                                                Version {v.version}
                                            </span>
                                            <span>
                                                {v.requireOpenmrsVersion}
                                            </span>
                                            <span>
                                                {this.formatRequiredModules(v)}
                                            </span>
                                            <a href={v.renameTo ? `/api/v1/addon/${addon.uid}/${v.version}/download` : v.downloadUri}>
                                                Download
                                            </a>
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