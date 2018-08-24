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
import {Link} from "react-router";

export default class ListOfLists extends React.Component {

    componentDidMount() {
        fetch('/api/v1/list')
                .then(response => {
                    return response.json();
                })
                .then(json => {
                    // include this special link before the ones mentioned in the addons-to-index file
                    json.unshift({
                                     name: "Top",
                                     route: "/topDownloaded"
                                 });
                    this.setState({lists: json});
                });
    }

    render() {
        const LISTS_TO_SHOW = 3;
        if (this.state && this.state.lists) {
            const toShow = this.state.lists.slice(0, LISTS_TO_SHOW);
            const anyMore = this.state.lists.length > LISTS_TO_SHOW;
            return (
                    <ul className="nav nav-pills">
                        {toShow.map(list =>
                                            <li key={list.route ? list.route : list.uid}>
                                                <Link to={list.route ? list.route : `/list/${list.uid}`}>
                                                    <strong>{list.name}</strong>
                                                </Link>
                                            </li>
                        )}
                        {anyMore ?
                         <li key="more">
                             <Link to="/lists">
                                 More Lists...
                             </Link>
                         </li>
                                :
                         null
                        }
                    </ul>
            )
        }
        else {
            return null;
        }
    }

}
