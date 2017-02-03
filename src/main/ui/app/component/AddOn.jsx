import {Component} from "react";
import {Link} from "react-router";

export default class AddOn extends Component {

    render() {
        const addon = this.props.addon;
        const icon = addon.type === 'OWA' ? 'fa fa-2x fa-globe' : 'fa fa-2x fa-puzzle-piece';
        const link = this.props.version ? `/show/${addon.uid}?highlightVersion=${this.props.version}` : `/show/${addon.uid}`;
        return (
                <li>
                    <Link to={link}>
                        <i className={icon} aria-hidden="true"></i>
                        <b>{addon.name}</b>
                        -
                        { this.props.version ?
                          `version ${this.props.version}`
                                :
                          `${addon.versionCount} version(s), latest: ${addon.latestVersion}`
                        }

                    </Link>
                </li>
        )
    }

}