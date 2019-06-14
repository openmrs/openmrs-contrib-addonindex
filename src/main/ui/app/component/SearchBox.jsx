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

    setSimpleStringQuery(simpleStringQuery) {
        this.setState({
            simpleStringQuery: simpleStringQuery
        });
    }

    setAddonType(addonType) {
        this.setState({
            addonType: addonType
        });
    }

    setTag(tag) {
        this.setState({
            tag: tag
        });
    }


    parseQuery(){
        if (this.state.query){
            let advanceQuery = this.state.query;
            let querySplit = advanceQuery.split(' ');
            querySplit.forEach(m => {
                if (m.includes(":")){
                    let [key, value] = m.split(':');
                    if (key === "type"){
                        this.setAddonType(value);
                    }
                    else if (key === "tag") {
                        this.setTag(value);
                    }
                }
                else {
                    this.setSimpleStringQuery(m);
                }
            })

        }
    }

    doSearch() {
        if (this.state.query) {
            this.parseQuery();
            let url = "/search?";

            if (this.state.addonType) {
                url += "type=" + this.state.addonType;
            }

            if (this.state.simpleStringQuery) {
                url += "&q=" + this.state.simpleStringQuery;
            }

            if (this.state.tag) {
                url += "&tag=" + this.state.tag;
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
