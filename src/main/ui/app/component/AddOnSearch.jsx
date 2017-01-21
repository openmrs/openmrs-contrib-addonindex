import {Component} from "react";
import fetch from "isomorphic-fetch";
import AddOnList from "./AddOnList";
import DebounceInput from "react-debounce-input";


export default class AddOnSearch extends Component {

    constructor(props) {
        super(props);
        this.state = {};
    }

    componentDidMount() {
        fetch('/api/v1/addon')
                .then(response => {
                    return response.json();
                })
                .then(json => {
                    this.setState({allAddOns: json});
                })
    }

    setType(type) {
        this.setState({
                          type: type,
                          searchResults: null
                      }, this.doSearch);

    }

    setQuery(query) {
        this.setState({
                          query: query,
                          searchResults: null
                      }, this.doSearch);
    }

    doSearch() {
        if (this.state.type || this.state.query) {
            var url = "/api/v1/addon?";
            if (this.state.type) {
                url += "type=" + this.state.type;
            }
            if (this.state.query) {
                url += "&q=" + this.state.query;
            }
            fetch(url)
                    .then(response => {
                        return response.json();
                    })
                    .then(json => {
                        this.setState({searchResults: json});
                    });
        }
    }

    render() {
        return (
                <div>
                    Type:
                    <select onChange={event => this.setType(event.target.value)}>
                        <option value="">All Types</option>
                        <option value="OMOD">Module (OMOD)</option>
                        <option value="OWA">Open Web App (OWA)</option>
                    </select>

                    <br/>
                    <DebounceInput
                            placeholder="Search..."
                            className="search"
                            minLength={1}
                            debounceTimeout={500}
                            onChange={event => this.setQuery(event.target.value)}/>
                    <i className="fa fa-search fa-2x"></i>

                    <br/>

                    { (this.state.query || this.state.type) ?
                      <AddOnList addons={this.state.searchResults}/>
                            :
                      <AddOnList addons={this.state.allAddOns}/>
                    }
                </div>
        )
    }
}