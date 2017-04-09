import {Component} from "react";
import {Link} from "react-router";
import {Label, Media} from "react-bootstrap";

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
                    <Media className="addon">
                        <Media.Left>
                            <i className={`fa fa-3x fa-fw fa-${addon.icon}`}></i>
                        </Media.Left>
                        <Media.Body>
                            <Media.Heading>{title}</Media.Heading>
                            <p>{addon.description}</p>
                        </Media.Body>
                    </Media>
                </Link>
        )
    }

}