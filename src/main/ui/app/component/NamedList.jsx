import {Component} from "react";
import AddOn from "./AddOn";


export default class NamedList extends Component {
    
    display(addon) {
        if (addon.version) {
            return (
                    <AddOn key={addon.uid} addon={addon.details} version={addon.version}/>
            )
        } else {
            return (
                    <AddOn key={addon.uid} addon={addon.details}/>
            )
        }
    }

    render() {
        const COLUMNS_IF_SIZE = 5;

        const list = this.props.list;

        return (
                <div>
                    <h1>{list.name}</h1>
                    <h3>{list.description}</h3>
                    <div className="row">
                        <div className="col-md-12 col-sm-12 col-xs-12">
                            { list.addOns.map(addon => this.display(addon)) }
                        </div>
                    </div>
                </div>
        )
    }

}

