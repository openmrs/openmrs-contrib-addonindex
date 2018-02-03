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

export default class About extends Component {

    render() {
        return (
                <div>
                    
                    <h1><b>About</b></h1>

                    <h3><b>How to add your module?</b></h3>

                    <p> Adding your module to Add Ons for indexing is as easy as creating a pull request!
                        First you need to upload your module to Bintray, and then you may head over to the <a target="_blank"
                                                                                                              href="https://github.com/openmrs/openmrs-contrib-addonindex/blob/master/PUBLISHING-AN-ADD-ON.md">Publishing
                                                                                                                                                                                                               an
                                                                                                                                                                                                               Add
                                                                                                                                                                                                               on
                                                                                                                                                                                                               document</a>
                        on Github which contains a comprehensive list of steps.
		            <br/>
		            <br/>
                        Discussions on the working of this tool and/or proposal of new features may be done on OpenMRS talk
                        in the <a target="_blank" href="https://talk.openmrs.org/c/projects/add-on-index">projects:add-on-index</a>
                        category.
                    </p>

                    <h3><b>About the OpenMRS Add Ons tool:</b></h3>
                    <ul>
                    <li><b>Contributing: </b> You may find the source code for Add Ons <a target="_blank"
                                                                                          href="https://github.com/openmrs/openmrs-contrib-addonindex">here</a>,
                                              for more details you may also view the <a target="_blank"
                                                                                        href="https://github.com/openmrs/openmrs-contrib-addonindex/blob/master/CONTRIBUTING.md">CONTRIBUTING.md document</a>
                    </li>
                    <li>
                        <b>Discussion Forum: </b> The discussion forum for OpenMRS Add Ons is located <a target="_blank"
                           href="https://talk.openmrs.org/c/projects/add-on-index">here</a>
                    </li>
                    <li>
                        <b>License: </b>Software is available under the <a target="_blank"
                           href="http://openmrs.org/license/">Mozilla Public License 2.0 with Healthcare Disclaimer (MPL 2.0 HD)</a>.
			    <br/>
			    "OpenMRS" is a registered trademark and the OpenMRS graphic logo is a trademark of OpenMRS Inc.
                    </li>
                    </ul>

                    <h3><b>Addons Stats:</b></h3>
                    <p>
                        <Link to="/indexingStatus">Indexing Status</Link></p>
                    </div>
        )
    }

}
