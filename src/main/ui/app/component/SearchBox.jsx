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

class SearchBox extends Component {

    constructor(props) {
        super(props);
        this.state = {};
    }

    setQuery(query) {
        this.setState({ query: query }, function() {
            this.parseQuery();
        })}

    setSplitQuery(splitQuery) {
        this.setState({
            splitQuery: splitQuery
        });
    }

    parseQuery(){
        if (this.state.query){
            let advancedQuery = this.state.query;
            //Basic Regex Matching to fix query inconsistencies
            //Removing all extra spaces i.e. two or more
            advancedQuery = advancedQuery.replace(/\s+/g,' ').trim();
            //Removing all spaces to the right of search keys
            advancedQuery = advancedQuery.replace(new RegExp("\\s+:","g"),":");
            //Removing all spaces to the left of search keys
            advancedQuery = advancedQuery.replace(new RegExp(":+\\s","g"),":");
            let queryComponents = {};
            //Determining query type
            if(advancedQuery.includes(":")){
                let querySplit = advancedQuery.split(' ');
                let tempQuery = "";
                querySplit.forEach(m => {
                    if (m.includes(":")){
                        let [key, value] = m.split(':');
                        if (key === "type"){
                            queryComponents["type"] = value.toUpperCase();
                        }
                        else if (key === "tag") {
                            queryComponents["tag"] = value.toLowerCase();
                        }
                    }
                    else {
                        tempQuery = tempQuery + m;
                        queryComponents["query"] = tempQuery;
                    }
                });
            }
            else{
                queryComponents["query"] = advancedQuery;
            }
            this.setSplitQuery(queryComponents);
        }
    }

    doSearch() {
        if (this.state.splitQuery) {
            let url = "/search?";

            if (this.state.splitQuery.type) {
                url += "type=" + this.state.splitQuery.type;
            }

            if (this.state.splitQuery.query) {
                url += "&q=" + this.state.splitQuery.query;
            }

            if (this.state.splitQuery.tag) {
                url += "&tag=" + this.state.splitQuery.tag;
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
