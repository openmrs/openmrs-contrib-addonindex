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
import {Label, Media} from "react-bootstrap";

export default class AddOn extends Component {

    render() {
        const addon = this.props.addon;
        const link = this.props.version ? `/show/${addon.uid}?highlightVersion=${this.props.version}` : `/show/${addon.uid}`;

        const title = (
                <div>
                    {addon.name}
                    &nbsp;
                    {addon.status === "DEPRECATED" ?
                     <Label bsStyle="danger">{addon.status}</Label>
                            :
                     addon.status === "INACTIVE" ?
                     <Label bsStyle="warning">{addon.status}</Label>
                             :
                     null
                    }
                    <div className="pull-right">{addon.type}</div>
                </div>
        );

        return (
                <Link to={link}>
                    <Media className="addon">
                        <Media.Left>
                            <i className={`fa fa-3x fa-fw fa-${addon.icon}`}></i>
                        </Media.Left>
                        <Media.Body>
                            <Media.Heading>{title}</Media.Heading>
                            <p>{addon.description}</p>
                        </Media.Body>
                    </Media>
                </Link>
        )
    }

}