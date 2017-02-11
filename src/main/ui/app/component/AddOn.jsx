import {Component} from "react";
import {Link} from "react-router";
import { ListGroupItem , Badge, Accordion , Panel , Media , Button } from 'react-bootstrap';

export default class AddOn extends Component {

    render() {
        const addon = this.props.addon;
        const icon = addon.type === 'OWA' ? 'fa fa-2x fa-globe' : 'fa fa-2x fa-puzzle-piece';
        const link = this.props.version ? `/show/${addon.uid}?highlightVersion=${this.props.version}` : `/show/${addon.uid}`;
	
	const title = (
 <div>{addon.name} <div className="pull-right">{addon.type}</div></div>
);

        return (
                <Link to={link}>
                    <ListGroupItem className="mainlist">
                   <Media>
                        <Media.Left>
                            <i className={icon} aria-hidden="true"></i>
                     	</Media.Left>
                   <Media.Body> 
                            
    			
		            
    <Panel header={title} className="list-grp" eventKey="1">
      {addon.description}
    </Panel>
       			
</Media.Body>	
      			</Media>    
                    </ListGroupItem>
      
                </Link>
        )
    }

}
