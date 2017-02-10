import {Component} from "react";
import {Link} from "react-router";
import { ListGroupItem , Badge, Accordion , Panel , Media , Button } from 'react-bootstrap';

export default class AddOn extends Component {

    render() {
        const addon = this.props.addon;
        const icon = addon.type === 'OWA' ? 'fa fa-2x fa-globe' : 'fa fa-2x fa-puzzle-piece';
        const link = this.props.version ? `#/show/${addon.uid}?highlightVersion=${this.props.version}` : `#/show/${addon.uid}`;
	
	const title = (
 <div>{addon.name} <div className="takeright">{addon.type}</div></div>
);

        return (
                
                    <ListGroupItem className="mainlist">
                   <Media>
                        <Media.Left>
                            <i className={icon} aria-hidden="true"></i>
                     	</Media.Left>
                   <Media.Body> 
                            
    			
		            <Accordion>
    <Panel header={title} className="listgrp" eventKey="1">
      {addon.description} 
<p><div className="takeright"><Button className="spacing" href={link} bsSize="small">More Info</Button>
<Button  href={link} bsStyle="primary" bsSize="small">Download</Button></div>
      </p>
    </Panel>
       			</Accordion>
</Media.Body>	
      			</Media>    
                    </ListGroupItem>
      
                
        )
    }

}
