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
import SearchBox from "../component/SearchBox";
import AddOnList from "../component/AddOnList";
import NamedList from "../component/NamedList";

export default class Home extends Component {

    constructor(props) {
        super(props);
        this.state = {};
    }

    componentDidMount() {
        fetch('/api/v1/list/DEFAULT')
                .then(response => {
                    return response.json();
                })
                .then(json => {
                    this.setState({defaultList: json});
                })
    }

    renderDefaultList() {
        if (this.state.defaultList) {
            return <NamedList list={this.state.defaultList}/>
        }
        else {
            return <div>Loading...</div>
        }
    }

    render() {
        return (
                <div>
                    <SearchBox
                            onStartSearch={(key) => this.handleStartSearch(key)}
                            onSearchResults={(key, results) => this.handleSearchResults(key, results)}
                    />

                    {this.state.latestSearch ?
                     <div>Searching for {this.state.latestSearch}</div>
                            :
                     this.state.searchResults ?
                     <AddOnList addons={this.state.searchResults} heading={`${this.state.searchResults.length} result(s)`}/>
                             :
                     this.renderDefaultList()
                    }
                </div>
        )
    }

}