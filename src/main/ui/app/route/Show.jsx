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
            let hosted = "";
            if (addon.hostedUrl) {
                if (addon.hostedUrl.includes("bintray.com")) {
                    hosted = <p>Hosted on <a target="_blank" href={addon.hostedUrl}>Bintray</a></p>
                }
                else {
                    hosted = <p>Hosted at <a target="_blank" href={addon.hostedUrl}>{addon.hostedUrl}</a></p>
                }
            }
            return (
                    <div>
                        <h1>
                            <i className={icon} aria-hidden="true"></i>
                            {addon.name}
                        </h1>
                        <h3>{addon.description}</h3>
                        {hosted}
                        <p>
                            Maintained by:
                            {addon.maintainers.map(m => {
                                if (m.url) {
                                    return (
                                            <a target="_blank" href={m.url}>{m.name}</a>
                                    )
                                } else {
                                    return <span>{m.name}</span>
                                }
                            })}
                        </p>
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