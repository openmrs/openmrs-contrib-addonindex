import {Component} from "react";
import {Link} from "react-router";
import {Label, Panel, Media} from "react-bootstrap";

export default class AddOn extends Component {

    render() {
        const addon = this.props.addon;
        const link = this.props.version ? `/show/${addon.uid}?highlightVersion=${this.props.version}` : `/show/${addon.uid}`;

        const title = (
                <div>
                    {addon.name}
                    &nbsp;
                    {addon.status === "DEPRECATED" ?
                     <Label bsStyle="danger">{addon.status}</Label>
                            :
                     addon.status === "INACTIVE" ?
                     <Label bsStyle="warning">{addon.status}</Label>
                             :
                     null
                    }
                    <div className="pull-right">{addon.type}</div>
                </div>
        );

        return (
                <Link to={link}>
                    <Media>
                        <Media.Body>

                            <Panel header={title} className="list-grp">
                                {addon.description}
                            </Panel>

                        </Media.Body>
                    </Media>
                </Link>
        )
    }

}