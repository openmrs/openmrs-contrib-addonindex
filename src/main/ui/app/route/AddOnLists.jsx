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

export default class AddOnLists extends Component {

    componentDidMount() {
        fetch('/api/v1/list')
                .then(response => {
                    return response.json();
                })
                .then(json => {
                    this.setState({lists: json});
                })
    }

    render() {
        if (this.state && this.state.lists) {
            return (
                    <div>

                        <h3>Lists</h3>
                        <ul className="list-group">
                            {this.state.lists.map(list =>
                                                          <li className="list-group-item">
                                                              <Link to={`/list/${list.uid}`}>
                                                                  <h3>
                                                                      {list.name}
                                                                      <small className="push-right">
                                                                          {list.description}
                                                                      </small>
                                                                  </h3>
                                                              </Link>
                                                          </li>
                            )}
                        </ul>
                    </div>
            )
        }
        else {
            return <div>Loading...</div>
        }
    }

}
