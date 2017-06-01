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
import {Link} from "react-router";
import fetch from "isomorphic-fetch";
import NamedList from "../component/NamedList";

export default class ShowList extends Component {

    updateList() {
        this.setState({list: null});
        return fetch('/api/v1/list/' + this.props.params.uid)
                .then(response => {
                    if (response.status >= 400) {
                        throw new Error(response.statusText);
                    }
                    else {
                        return response.json();
                    }
                })
                .then(list => {
                    this.setState({list: list});
                })
                .catch(err => {
                    this.setState({error: err});
                })
    }

    componentDidMount() {
        this.updateList();
    }

    componentDidUpdate(prevProps, prevState) {
        if (this.props.params.uid !== prevProps.params.uid) {
            this.updateList();
        }
    }

    render() {
        if (this.state && this.state.error) {
            return <div>{this.state.error}</div>
        }
        else if (this.state && this.state.list) {
            const list = this.state.list;
            return <NamedList list={list}/>
        }
        else {
            return <div>Loading...</div>
        }
    }

}