import {Component} from "react";
import AddOn from "./AddOn";
import { ListGroup , Table} from 'react-bootstrap';


export default class AddOnList extends Component {

    render() {
        if (!this.props.addons) {
            return (
             <section>
      <p class="lead textdec">Loading</p>
      </section>
      )
        }
        else if (this.props.addons.length === 0) {
            return (
            <section>
      <h1 class="lead textdec">No Results</h1>
      </section>

            )
        }
        else {
            return (
                   <div>
                    <ListGroup>
                        {this.props.addons.map(addon =>
                                                       <AddOn key={addon.uid} addon={addon}/>
                        )}
                    </ListGroup>
                    </div>
            )
        }
    }
}

