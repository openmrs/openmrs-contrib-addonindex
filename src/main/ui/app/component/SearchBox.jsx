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
import {withRouter} from "react-router";
import {Button, Col, Form, FormControl, InputGroup} from "react-bootstrap";
import searchQuery from "search-query-parser";

class SearchBox extends Component {

    constructor(props) {
        super(props);
        this.state = {};
    }

    setQuery(query) {
        this.setState({
            query: query
        });
    }

    doSearch() {
        if (this.state.query) {
            let query = this.state.query.toLowerCase();
            const options = {keywords: ['uid', 'type', 'tag', 'query', 'moduleid', 'status', 'name'], offsets: false};
            //Basic Regex Matching to fix query inconsistencies
            //Removing all extra spaces i.e. two or more
            query = query.replace(/\s+/g,' ').trim();
            //Removing all spaces to the left of search keys
            query = query.replace(new RegExp("\\s+:","g"),":");
            //Removing all spaces to the right of search keys
            query = query.replace(new RegExp(":\\s+","g"),":");
            let url = "/search?";
            // Check if query is an advanced query. We want to use the Parser only if the query is advanced
            if (query.includes(":") || query.includes("-")) {
                let searchQueryObj = searchQuery.parse(query, options);
                Object.keys(searchQueryObj).forEach(function (key) {
                    if (searchQueryObj[key]){
                        if (key === "text" || key === "query") {
                            url += "&q=" + searchQueryObj[key];
                        }
                        else if (key === "type"){
                            url += "&type=" + searchQueryObj[key].toUpperCase();
                        }
                        else if (key === "exclude"){
                            if (searchQueryObj[key].text){
                                url += "&exclude=" + searchQueryObj[key].text;
                            }
                        }
                        else if (key === "status"){
                            url += "&status=" + searchQueryObj[key].toUpperCase();
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

            this.props.router.push(url);
        }
    }

    render() {
        return (
                <div className="row pushdown">
                    <Form onSubmit={(evt) => {
                        evt.preventDefault();
                        this.doSearch()
                    }}>
                        <Col sm={12} md={8}>
                            <InputGroup className="input-group-lg">
                                <FormControl controlId="query"
                                             type="text"
                                             name="query"
                                             defaultValue={this.props.initialQuery}
                                             placeholder="Search..."
                                             autoFocus
                                             onChange={evt => this.setQuery(evt.target.value)}
                                />
                                <InputGroup.Button>
                                    <Button type="submit">
                                        <i className="fa fa-search"></i>
                                    </Button>
                                </InputGroup.Button>
                            </InputGroup>
                        </Col>
                    </Form>
                </div>

        )
    }
}

export default withRouter(SearchBox)
