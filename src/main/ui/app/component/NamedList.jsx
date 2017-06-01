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
import AddOn from "./AddOn";


export default class NamedList extends Component {
    
    display(addon) {
        if (addon.version) {
            return (
                    <AddOn key={addon.uid} addon={addon.details} version={addon.version}/>
            )
        } else {
            return (
                    <AddOn key={addon.uid} addon={addon.details}/>
            )
        }
    }

    render() {

        const list = this.props.list;

        return (
                <div>
                    <h1>{list.name}</h1>
                    <h3>{list.description}</h3>
                    <div className="row">
                        <div className="col-md-12 col-sm-12 col-xs-12">
                            { list.addOns.map(addon => this.display(addon)) }
                        </div>
                    </div>
                </div>
        )
    }

}

