import {Component} from "react";

export default class AddOn extends Component {

    render() {
        const addon = this.props.addon;
        return <li><b>{addon.name}</b> - {addon.versions.length} version(s), latest: {addon.versions[0].version}</li>
    }

}