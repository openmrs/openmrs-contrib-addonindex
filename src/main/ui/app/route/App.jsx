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
import ListOfLists from "../component/ListOfLists";

export default class App extends Component {

    render() {
        return (
                <div className="container-fluid">
                    <header className="clearfix row vertical-align-center">
                        <h1 className="col-sm-5">
                            <a href="#/">
                                <img className="logo logo1" src="/images/logo.png" alt="OpenMRS Add-Ons Logo"/>
                            </a>
                        </h1>
                        <div className="col-sm-7">
                            <ListOfLists/>
                        </div>
                    </header>
                    {this.props.children}
                </div>
        )
    }

}