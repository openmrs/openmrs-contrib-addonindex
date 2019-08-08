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
import searchQuery from "search-query-parser";

//Configuration for the Search Query Parser
const options = {keywords: ['uid', 'type', 'tag', 'query', 'moduleid', 'status', 'name'],
    tokenize:true ,offsets:false};

export default class SearchPage extends Component {

    constructor(props) {
        super(props);
        console.log("Constructed!");
        this.state = {};
    }

    componentDidMount() {
        console.log("Mount");
        this.doSearch();
    }

    componentDidUpdate(prevProps) {
        if (this.props.location.query !== prevProps.location.query) {
            this.doSearch();
        }
    }

    normalizeQuery(query) {
        //Basic Regex Matching to fix query inconsistencies
        //Removing all extra spaces i.e. two or more
        query = query.replace(/\s+/g,' ').trim();
        //Removing all spaces to the left of search keys
        query = query.replace(new RegExp("\\s+:","g"),":");
        //Removing all spaces to the right of search keys
        query = query.replace(new RegExp(":\\s+","g"),":");
        return query;
    }

    doSearch() {
        console.log("Here!");
        const searchKey = this.props.location.query.q;
        console.log(searchKey.toString());
        console.log(searchKey);
        let query = searchKey.toLowerCase();
        console.log(query);
        if (this.state.latestSearch === searchKey) {
            return;
        }

        this.handleStartSearch(searchKey);

        let url = "/api/v1/addon?";

        query = this.normalizeQuery(query);

        // Check if query is an advanced query. We want to use the Parser only if the query is advanced
        if (query.includes(":") || query.includes("-")) {
            let searchQueryObj = searchQuery.parse(query, options);
            Object.keys(searchQueryObj).forEach(function (key) {
                if (searchQueryObj[key]) {
                    if (key === "text" || key === "query") {
                        key === "text" ? url += "&q=" + searchQueryObj[key].join(" ") : url += "&q=" + searchQueryObj[key];
                    }
                    else if (key === "type" || key === "status") {
                        url += "&" + key + "=" + searchQueryObj[key].toUpperCase();
                    }
                    else if (key === "exclude") {
                        if (searchQueryObj[key].text) {
                            url += "&exclude=" + searchQueryObj[key].text;
                        }
                    }
                    else {
                        url += "&" + key + "=" + searchQueryObj[key];
                    }
                }
            });
        }
        else {
            url += "&q=" + query;
        }

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
        console.log("jhe");
        let query = this.props.location.query.q;

        if (this.state && this.state.error) {
            return <div>{this.state.error}</div>
        }
        else {
            return (
                    <div>
                        <SearchBox initialQuery={query}/>

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
