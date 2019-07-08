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
        const advancedQuery = this.props.location.query;
        const query = this.props.location.query.q;
        const type = this.props.location.query.type;
        const tag = this.props.location.query.tag;
        const exclude = this.props.location.query.exclude;
        const moduleid = this.props.location.query.moduleid;
        const name = this.props.location.query.name;
        const status = this.props.location.query.status;
        console.log(advancedQuery);
        const searchKey = `${type ? "type:" + type : ""} ${query ? "query:" + query : ""} ${tag ? "tag:" + tag : ""} ${exclude ? "-" + exclude : ""} ${name ? "name:" + name : ""} ${status ? "status:" + status : ""} ${moduleid ? "moduleId:" + moduleid : ""}`;

        if (this.state.latestSearch === searchKey) {
            return;
        }

        this.handleStartSearch(searchKey);

        let url = "/api/v1/addon?";

        Object.keys(advancedQuery).forEach(function (key) {
            if (advancedQuery[key]){
                url += ("&" + key + "=" + advancedQuery[key]);
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
        let simpleQuery = this.props.location.query.q;
        let moduleType = this.props.location.query.type;
        let moduleTag = this.props.location.query.tag;
        let moduleExclude = this.props.location.query.exclude;
        let moduleid = this.props.location.query.moduleid;
        let moduleName = this.props.location.query.name;
        let moduleStatus = this.props.location.query.status;

        if (this.state && this.state.error) {
            return <div>{this.state.error}</div>
        }
        else {
            return (
                    <div>
                        <SearchBox initialQuery={`${moduleType ? "type:" + moduleType : ""} ${simpleQuery ? "query:" + simpleQuery : ""} ${moduleTag ? "tag:" + moduleTag : ""} ${moduleExclude ? "-" + moduleExclude : ""} ${moduleName ? "name:" + moduleName : ""} ${moduleStatus ? "status:" + moduleStatus : ""} ${moduleid ? "moduleId:" + moduleid : ""}`}/>

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
