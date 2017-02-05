import {Component} from "react";
import AddOn from "./AddOn";

export default class AddOnList extends Component {

    render() {
        if (!this.props.addons) {
            return (
             <section>
      <p class="lead">Loading</p>
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
                   <table className="table-responsive table-hover table-striped col-md-12 col-sm-12 col-xs-12 p">
                    <thead class="thead-inverse lead">
                    <tr>
                    <th className="col-md-3">Name</th>
                    <th className="col-md-7">Description</th>
                    <th className="col-md-2">Type</th> 
                    
                    </tr>
                    </thead>
                    <tbody>
                        {this.props.addons.map(addon =>
                                                       <AddOn key={addon.uid} addon={addon}/>
                        )}
                    </tbody>
                    </table>
            )
        }
    }
}

