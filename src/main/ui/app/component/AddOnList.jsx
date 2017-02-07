import {Component} from "react";
import AddOn from "./AddOn";

export default class AddOnList extends Component {

    render() {
        if (!this.props.addons) {
            return <div>Loading...</div>
        }
        else if (this.props.addons.length === 0) {
            return <div>No results</div>
        }
        else {
            return (
                    <div>
                        {this.props.addons.map(addon =>
                                                       <AddOn key={addon.uid} addon={addon}/>
                        )}
                    </div>
            )
        }
    }
}

