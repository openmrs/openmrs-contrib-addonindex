import {Component} from "react";
import {Link} from "react-router";

export default class AddOn extends Component {

    render() {
        const addon = this.props.addon;
        const icon = addon.type === 'OWA' ? 'fa fa-2x fa-globe' : 'fa fa-2x fa-puzzle-piece';
        const link = this.props.version ? `/show/${addon.uid}?highlightVersion=${this.props.version}` : `/show/${addon.uid}`;
        return (
                <Link to={link}>
                    <div className="media panel">
                        <div className="media-left">
                            <i className={icon} aria-hidden="true"></i>
                        </div>
                        <div className="media-body">
                            <h4 className="media-heading">
                                {addon.name}
                                <span className="badge">
                                { this.props.version || "" }
                            </span>
                            </h4>
                            {addon.description}
                        </div>
                    </div>
                </Link>
        )
    }

}