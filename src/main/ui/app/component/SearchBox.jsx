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
        this.setState({
                          query: query
                      });
    }

    // setSimpleStringQuery(simpleStringQuery) {
    //     this.setState({
    //         simpleStringQuery: simpleStringQuery
    //     });
    // }
    //
    // setAddonType(addonType) {
    //     this.setState({
    //         addonType: addonType
    //     });
    // }
    //
    // setTag(tag) {
    //     this.setState({
    //         tag: tag
    //     });
    // }


    parseQuery(advancedQuery){
        let querySplit = advancedQuery.split(' ');
        let type,tag,simpleQuery;

        querySplit.forEach(m => {
            if (m.includes(":")){
                let [key, value] = m.split(':');
                if (key === "type"){
                    type = value;
                }
                else if (key === "tag") {
                    tag = value;
                }
            }
            else {
                simpleQuery = m;
            }
        });
        return {
            type: this.type,
            tag: this.tag,
            simpleQuery: this.simpleQuery
        };


    }

    doSearch() {
        if (this.state.query) {
            let advanceQuery = this.state.query;
            let params = this.parseQuery(advanceQuery);
            let url = "/search?";

            if (params.type) {
                url += "type=" + params.type;
            }

            if (params.simpleQuery) {
                url += "&q=" + this.state.simpleStringQuery;
            }

            if (params.tag) {
                url += "&tag=" + params.tag;
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
