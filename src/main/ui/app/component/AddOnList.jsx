import {Component} from "react";
import AddOn from "./AddOn";

export default class AddOnList extends Component {

    render() {
        if (!this.props.addons) {
            return <div>Loading...</div>
        }
        else {
            return (
                    <ul>
                        {this.props.addons.map(addon =>
                                                       <AddOn key={addon.uid} addon={addon}/>
                        )}
                    </ul>
            )
        }
    }
}

