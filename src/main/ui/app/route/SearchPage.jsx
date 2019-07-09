/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

import {Component} from "react";
import fetch from "isomorphic-fetch";
import SearchBox from "../component/SearchBox";
import AddOnList from "../component/AddOnList";

export default class SearchPage extends Component {

    constructor(props) {
        super(props);
        this.state = {};
    }

    componentDidMount() {
        this.doSearch();
    }

    componentDidUpdate(prevProps) {
        if (this.props.location.query !== prevProps.location.query) {
            this.doSearch();
        }
    }

    doSearch() {
        const query = this.props.location.query;

        const searchKey = `${query.type ? "type:" + query.type : ""} ${query.q ? "query:" + query.q : ""} ${query.tag ? "tag:" + query.tag : ""} ${query.exclude ? "-" + query.exclude : ""} ${query.name ? "name:" + query.name : ""} ${query.status ? "status:" + query.status : ""} ${query.moduleid ? "moduleId:" + query.moduleid : ""}`;

        if (this.state.latestSearch === searchKey) {
            return;
        }

        this.handleStartSearch(searchKey);

        let url = "/api/v1/addon?";

        Object.keys(query).forEach(function (key) {
            if (query[key]){
                url += ("&" + key + "=" + query[key]);
            }
        });

        fetch(url)
                .then(response => {
                    return response.json();
                })
                .then(json => {
                    this.handleSearchResults(searchKey, json);
                });
    }

    handleStartSearch(searchKey) {
        this.setState({
                          latestSearch: searchKey
                      });
    }

    handleSearchResults(searchKey, searchResults) {
        if (this.state.latestSearch === searchKey) {
            this.setState({
                              latestSearch: null,
                              searchResults: searchResults
                          });
        }
    }

    render() {
        let query = this.props.location.query;

        if (this.state && this.state.error) {
            return <div>{this.state.error}</div>
        }
        else {
            return (
                    <div>
                        <SearchBox initialQuery={`${query.type ? "type:" + query.type : ""} ${query.q ? "query:" + query.q : ""} ${query.tag ? "tag:" + query.tag : ""} ${query.exclude ? "-" + query.exclude : ""} ${query.name ? "name:" + query.name : ""} ${query.status ? "status:" + query.status : ""} ${query.moduleid ? "moduleId:" + query.moduleid : ""}`}/>

                        {this.state.latestSearch ?
                         <div>Searching for {this.state.latestSearch}</div>
                                :
                         this.state.searchResults ?
                         <AddOnList addons={this.state.searchResults}
                                    heading={`${this.state.searchResults.length} result(s)`}/>
                                 :
                         <div>No results</div>
                        }
                    </div>
            )
        }
    }

}
