import {Component} from "react";
import {Link} from "react-router";

export default class AddOn extends Component {

    render() {
        const addon = this.props.addon;
        return (
                <li>
                    <Link to={`/show/${addon.uid}`}>
                        <b>{addon.name}</b> - {addon.versionCount} version(s), latest: {addon.latestVersion}
                    </Link>
                </li>
        )
    }

}