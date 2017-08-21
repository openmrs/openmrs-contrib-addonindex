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
import Select from 'react-select';

export default class SelectUserVersions extends Component {

    constructor() {
        super();
        this.state = {};
    }

    componentDidMount() {
        fetch('/api/v1/coreversions')
            .then(response => {
                return response.json();
            })
            .then(json => {
                this.setState({coreversions: json});
            });
    }

    render() {
        let openmrsCoreVersionOptions = [];
        if (this.state.coreversions) {
            this.state.coreversions.slice(0).reverse().forEach(versions =>
                openmrsCoreVersionOptions.push({
                    value: versions,
                    label: versions
                })
            )
        }

        return (<Select value={this.props.value}
                        placeholder='Version'
                        options={openmrsCoreVersionOptions}
                        name="selected-state"
                        onChange={selection => selection ? this.props.updateValue(selection.value): this.props.updateValue(null)}
                        searchable="true"/>)


    }
}