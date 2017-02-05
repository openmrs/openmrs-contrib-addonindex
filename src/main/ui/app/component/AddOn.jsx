import {Component} from "react";
import {Link} from "react-router";

export default class AddOn extends Component {

    render() {
        const addon = this.props.addon;
        const icon = addon.type === 'OWA' ? 'fa fa-2x fa-globe' : 'fa fa-2x fa-puzzle-piece';
        const link = this.props.version ? `/show/${addon.uid}?highlightVersion=${this.props.version}` : `/show/${addon.uid}`;
        return (
               <tr>
                    <td className="col-md-3">
                    <Link to={`/show/${addon.uid}`}>
                        
                        <b>{addon.name}</b>
                    </Link>
                    </td>
                    <Link to={`/show/${addon.uid}`}>
                    <td className="col-md-7 descmod">latest: {addon.latestVersion}</td>
                    </Link>
                    <td>{addon.type}</td>
                    
                </tr>
                
        )
    }

}