import {Component} from "react";
import {Link} from "react-router";
import fetch from "isomorphic-fetch";
import NamedList from "../component/NamedList";

export default class ShowList extends Component {

    updateList() {
        this.setState({list: null});
        return fetch('/api/v1/list/' + this.props.params.uid)
                .then(response => {
                    if (response.status >= 400) {
                        throw new Error(response.statusText);
                    }
                    else {
                        return response.json();
                    }
                })
                .then(list => {
                    this.setState({list: list});
                })
                .catch(err => {
                    this.setState({error: err});
                })
    }

    componentDidMount() {
        this.updateList();
    }

    componentDidUpdate(prevProps, prevState) {
        if (this.props.params.uid !== prevProps.params.uid) {
            this.updateList();
        }
    }

    render() {
        if (this.state && this.state.error) {
            return <div>{this.state.error}</div>
        }
        else if (this.state && this.state.list) {
            const list = this.state.list;
            return <NamedList list={list}/>
        }
        else {
            return <div>Loading...</div>
        }
    }

}