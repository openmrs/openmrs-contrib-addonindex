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

    doSearch() {
        if (this.state.type || this.state.query) {
            let url = "/search?";
            if (this.state.type) {
                url += "type=" + this.state.type;
            }
            if (this.state.query) {
                url += "&q=" + this.state.query;
            }

            this.props.router.push(url);
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