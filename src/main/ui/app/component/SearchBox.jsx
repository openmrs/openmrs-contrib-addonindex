import {Component} from "react";
import fetch from "isomorphic-fetch";
import DebounceInput from "react-debounce-input";
import {DropdownButton, MenuItem} from "react-bootstrap";

export default class SearchBox extends Component {

    constructor(props) {
        super(props);
        this.state = {};
    }

    setType(type) {
        this.props.onSearchResults(null);
        this.setState({
                          type: type
                      }, this.doSearch);

    }

    setQuery(query) {
        this.props.onSearchResults(null);
        this.setState({
                          query: query
                      }, this.doSearch);
    }

    doSearch() {
        if (this.state.type || this.state.query) {
            let url = "/api/v1/addon?";
            if (this.state.type) {
                url += "type=" + this.state.type;
            }
            if (this.state.query) {
                url += "&q=" + this.state.query;
            }
            let searchKey = `type:${this.state.type ? this.state.type : "all"} query:${this.state.query}`;
            this.props.onStartSearch(searchKey);
            fetch(url)
                    .then(response => {
                        return response.json();
                    })
                    .then(json => {
                        this.props.onSearchResults(searchKey, json);
                    });
        }
    }

    formatType(type) {
        if (type === "OMOD") {
            return "Module (OMOD)";
        }
        else if (type === "OWA") {
            return "Open Web App (OWA)";
        }
        else {
            return "All Types";
        }
    }

    render() {
        const title = (
                <span>{this.formatType(this.state.type)}</span>
        );
        return (
                <div className="row pushdown">
                    <div className="col-md-12 col-sm-12 col-xs-12">
                        <div className="input-group">

                            <div className="input-group-btn">
                                <DropdownButton title={title} id="dropdown-size-small"
                                                bsSize="large"
                                                onSelect={event => this.setType(event)}>
                                    <MenuItem eventKey="">{this.formatType("")}</MenuItem>
                                    <MenuItem eventKey="OMOD">{this.formatType("OMOD")}</MenuItem>
                                    <MenuItem eventKey="OWA">{this.formatType("OWA")}</MenuItem>
                                </DropdownButton>
                            </div>

                            <DebounceInput
                                    placeholder="Search..."
                                    className="form-control input-lg col-md-8 col-sm-8 col-xs-8"
                                    minLength={1}
                                    debounceTimeout={500}
                                    autoFocus
                                    onChange={event => this.setQuery(event.target.value)}/>

                        </div>
                    </div>
                </div>

        )
    }
}
