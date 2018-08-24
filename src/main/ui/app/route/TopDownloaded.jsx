/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

import React from "react";
import AddOn from "../component/AddOn";

export default class TopDownloaded extends React.Component {

    componentDidMount() {
        fetch('/api/v1/topdownloaded')
                .then(response => {
                    if (response.status >= 400) {
                        throw new Error(response.statusText);
                    }
                    else {
                        return response.json();
                    }
                })
                .then(list => {
                    this.setState({topDownloaded: list});
                })
                .catch(err => {
                    this.setState({error: err});
                });
    }

    render() {
        if (this.state && this.state.error) {
            return <div>{this.state.error}</div>
        }
        else if (this.state && this.state.topDownloaded) {
            return <div>
                <h1>Most Downloaded in the last 30 days</h1>
                {this.state.topDownloaded.map(a => {
                    return (
                            <div className="row most-downloaded-item" key={a.summary.uid}>
                                <div className="col-md-1 col-sm-1 col-xs-1">
                                    <h3 className="text-center">{a.downloadCount}</h3>
                                </div>
                                <div className="col-md-11 col-sm-11 col-xs-11">
                                    <AddOn key={a.summary.uid} addon={a.summary}/>
                                </div>
                            </div>
                    )
                })}
            </div>
        }
        else {
            return <div>Loading...</div>
        }
    }
}