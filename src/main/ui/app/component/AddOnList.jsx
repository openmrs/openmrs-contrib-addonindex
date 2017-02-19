import {Component} from "react";
import AddOn from "./AddOn";

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
                        <p class="lead textdec">No Results</p>
                    </section>
            )
        }
        else {
            return (
                    <div>
                        {this.props.heading ?
                         <h4>{this.props.heading}</h4>
                                :
                         null
                        }
                        <div className="row">
                            {this.props.addons.map(addon =>
                                                           <div className="col-md-12 col-sm-12 col-xs-12">
                                                               <AddOn key={addon.uid} addon={addon}/>
                                                           </div>
                            )}
                        </div>
                    </div>
            )
        }
    }
}

