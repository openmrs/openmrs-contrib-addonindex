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
import AddOnList from "../component/AddOnList";

export default class TagPage extends Component {

    constructor(props) {
        super(props);
        this.state = {};
    }

    componentDidMount() {
        this.getTag();
    }

    componentDidUpdate(prevProps) {
        if (this.props.location.query !== prevProps.location.query) {
            this.getTag();
        }
    }

    getTag() {
        const tag = this.props.location.query.tag;
    
        console.log(tag);
        console.log(this.state.latestTag);

        if (this.state.latestTag === tag) {
            return;
        }

        this.handleStartTag(tag);

        let url = "/api/v1/tags?";
        if (tag) {
            url += "&tag=" + tag;
        }
        fetch(url)
                .then(response => {
                    return response.json();
                })
                .then(json => {
                    this.handleSearchResults(tag, json);
                });
    }

    handleStartTag(tag) {
        this.setState({
                          latestTag: tag
                      });
    }

    handleSearchResults(tag, tagResults) {
        if (this.state.latestTag === tag) {
            this.setState({
                              latestTag: null,
                              tagResults: tagResults
                          });
        }
    }

    render() {
        if (this.state && this.state.error) {
            return <div>{this.state.error}</div>
        }
        else {
            return (
                    <div>
                        {this.state.latestTag ?
                         <div>Searching for {this.state.latestTag}</div>
                                :
                         this.state.tagResults ?
                         <AddOnList addons={this.state.tagResults}
                                    heading={`Modules with the tag ${this.props.location.query.tag} are:`}/>
                                 :
                         <div>No results</div>
                        }
                    </div>
            )
        }
    }

}
