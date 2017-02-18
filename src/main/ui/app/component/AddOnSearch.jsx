import {Component} from "react";
import fetch from "isomorphic-fetch";
import AddOnList from "./AddOnList";
import DebounceInput from "react-debounce-input";
import {DropdownButton, MenuItem} from "react-bootstrap";

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

        const title = (
                <span>Type: {this.state.type}</span>
        );
        return (
                <div className="row col-md-12 col-sm-12 col-xs-12 pushdown">
                    <div>
                        <div className="input-group">

                            <div className="input-group-btn">

                                <DropdownButton title={title} id="dropdown-size-small"
                                                onSelect={event => this.setType(event)}>
                                    <MenuItem eventKey="">All Types</MenuItem>
                                    <MenuItem eventKey="OMOD">Module (OMOD)</MenuItem>
                                    <MenuItem eventKey="OWA">Open Web App (OWA)</MenuItem>
                                </DropdownButton>
                            </div>


                            <DebounceInput
                                    placeholder="Search..."
                                    className="form-control col-md-8 col-sm-8 col-xs-8"
                                    minLength={1}
                                    debounceTimeout={500}
                                    onChange={event => this.setQuery(event.target.value)}/>


                        </div>
                    </div>

                    { (this.state.query || this.state.type) ?
                      <AddOnList addons={this.state.searchResults}/>
                            :
                      <AddOnList addons={this.state.allAddOns}/>
                    }
                </div>

        )
    }
}
