import {Component} from "react";
import {Link} from "react-router";

export default class AddOn extends Component {

    render() {
        const addon = this.props.addon;
        const icon = addon.type === 'OWA' ? 'fa fa-2x fa-globe' : 'fa fa-2x fa-puzzle-piece';
        return (
                <li>
                    <Link to={`/show/${addon.uid}`}>
                        <i className={icon} aria-hidden="true"></i>
                        <b>{addon.name}</b> - {addon.versionCount} version(s), latest: {addon.latestVersion}
                    </Link>
                </li>
        )
    }

}