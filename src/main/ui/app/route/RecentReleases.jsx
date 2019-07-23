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
        fetch('/api/v1/addon/recentreleases')
            .then(response => {
                if (response.status >= 400) {
                    throw new Error(response.statusText);
                }
                else {
                    return response.json();
                }
            })
            .then(list => {
                this.setState({recentReleases: list});
            })
            .catch(err => {
                this.setState({error: err});
            });
    }

    render() {
        if (this.state && this.state.error) {
            return <div>{this.state.error}</div>
        }
        else if (this.state && this.state.recentReleases) {
            return <div>
                <h1>Latest releases</h1>

                {this.state.recentReleases.map(a => {
                    return (
                        <div className="row recently-released-item" key={a.summary.uid}>
                            <div className="col-md-10 col-sm-10 col-xs-10">
                                <AddOn key={a.summary.uid} addon={a.summary}/>
                            </div>
                            <div className="col-md-2 col-sm-2 col-xs-2">
                                <h3 className="text-center">{a.versions[0].version}</h3>
                                <h3 className="text-center">{a.downloadCount}</h3>
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