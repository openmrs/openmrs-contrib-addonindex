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
import {Button, Col, Glyphicon, Label, OverlayTrigger, Row, Table, Tooltip} from "react-bootstrap";
import moment from "moment";

export default class Show extends Component {

    componentDidMount() {
        fetch('/api/v1/addon/' + this.props.params.uid)
                .then(response => {
                    if (response.status >= 400) {
                        throw new Error(response.statusText);
                    }
                    else {
                        return response.json();
                    }
                })
                .then(addon => {
                    this.setState({addon: addon});
                })
                .catch(err => {
                    this.setState({error: err});

                });

        this.getLatestSupportedVersion(this.props.selectedVersion)
    }

    getLatestSupportedVersion(openmrsCoreVersion){
        let url = '/api/v1/addon/' + this.props.params.uid + '/latestVersion';
        if (openmrsCoreVersion) {
            url += '?&coreversion=' + openmrsCoreVersion;
        }
        fetch(url)
            .then(response => {
                if (response.status >= 400) {
                    throw new Error(response.statusText);
                }
                //Handles case when no compatible module version is found
                else if (response.status === 204){
                    return null;
                }
                else {
                    return response.json();
                }
            })
            .then(version => {
                this.setState({latestVersion: version});
            })
            .catch(err => {
                this.setState({error: err});
                console.log(this.state.error);
            })
    }

    componentWillReceiveProps(nextProps) {
        if(nextProps.selectedVersion !== this.props.selectedVersion){
            this.getLatestSupportedVersion(nextProps.selectedVersion)
        }
    }

    formatRequiredModules(version) {
        let requirements = [];
        if (version.requireModules) {
            version.requireModules.forEach(m => {
                requirements.push(`${m.module.replace("org.openmrs.module.", "")} ${m.version || ""}`)
            })
        }
        return requirements.join(", ");
    }

    formatDateTime(dt) {
        const m = moment(dt);
        return <span>
            {m.fromNow()}
            <br/>
            {m.format("ll")}
        </span>
    }

    formatTags(addon) {
        let statusClass = "default";
        let status = addon.status;
        switch (addon.status) {
            case 'ACTIVE':
                statusClass = "success";
                break;
            case 'INACTIVE':
                statusClass = "warning";
                break;
            case 'DEPRECATED':
                statusClass = "danger";
                break;
            default:
                // if we want to display a placeholder here when status is unspecified, uncomment the next line:
                // status = "Unknown Activity";
                break;
        }
        return <div>
            <Label bsStyle={statusClass}>{status}</Label>
            {addon.tags ?
             addon.tags.map(t =>
                 <Link to={`/search?&tag=${t}`}><Label bsStyle="default">{t}</Label></Link>
             ) :
             null
            }
        </div>
    }

    render() {
        if (this.state && this.state.error) {
            return <div>{this.state.error}</div>
        }
        else if (this.state && this.state.addon) {
            const addon = this.state.addon;
            const highlightVersion = this.props.location.query.highlightVersion;
            const requirementmeaning = (
                    <Tooltip id="tooltip"><strong>Minimum version of the OpenMRS Platform required.</strong></Tooltip>
            );
            let version = "";
            if (this.state.latestVersion){
                if (this.state.latestVersion.version === addon.versions[0].version){
                    version = <span>Download<br/>
                        <small>Latest Version: {this.state.latestVersion.version}</small></span>;
                }
                else {
                    version = <span>Download<br/>
                        <small>Supported Version: {this.state.latestVersion.version}</small></span>;
                }
            }
            else {
                version = <span>No module version supports<br/>
                        <small>OpenMRS Core {this.props.selectedVersion}</small></span>;
            }

            let versionDownloadUri = this.state.latestVersion ? this.state.latestVersion.downloadUri : null;
            let hostedSection = "";
            let hosted = "";
            if (addon.hostedUrl) {
                if (addon.hostedUrl.includes("bintray.com")) {
                    hosted = <a target="_blank" href={addon.hostedUrl}>Bintray</a>
                }
                else {
                    hosted = <a target="_blank" href={addon.hostedUrl}>{addon.hostedUrl}</a>
                }
                hostedSection = (<tr>
                    <th>Hosted at</th>
                    <td><h5>{hosted}</h5></td>
                </tr>);
            }

            return (

                    <div>
                        <Row>
                            <Col sm={1} md={1} className="hidden-xs">
                                <i className={`fa fa-4x fa-${addon.icon ? addon.icon : 'file-o'} shiftdown`}></i>
                            </Col>
                            <Col sm={11} md={11} className="delete-left-padding">
                                <h2>{addon.name}</h2>
                                <h4 className="lead">{addon.description}</h4>
                                { this.formatTags(addon) }
                            </Col>
                        </Row>
                        <div className="col-md-12 col-sm-12 col-xs-12 left-margin">
                            <div className="col-md-9 col-sm-9 col-xs-9">
                            <Table condensed>
                                <colgroup>
                                    <col className="col-md-2"/>
                                    <col className="col-md-10"/>
                                </colgroup>
                                <tbody>
                                <tr>
                                    <th>Type</th>
                                    <td><h5>{addon.type}</h5></td>
                                </tr>
                                {hostedSection}
                                <tr>
                                    <th>Maintained by</th>
                                    <td><h5>{addon.maintainers.map(m => {
                                        if (m.url) {
                                            return (
                                                    <a href={m.url}>
                                                        <span className="maintainer">{m.name}</span>
                                                    </a>
                                            )
                                        } else {
                                            return (
                                                    <span className="maintainer">{m.name}</span>
                                            )
                                        }
                                    })}
                                    </h5></td>
                                </tr>
                                <tr>
                                    <th>Total downloads</th>
                                    <td>{addon.downloadCounts ? addon.downloadCounts : 0}</td>
                                </tr>
                                </tbody>
                            </Table>
                            </div>
                            <div className="col-md-3 col-sm-3 col-xs-3">
                                <a href={versionDownloadUri}>
                                    <Button className="primary" bsStyle={versionDownloadUri ? "primary" : "default"} bsSize="large"
                                            disabled={versionDownloadUri === null}>
                                        {version}
                                    </Button>
                                </a>
                            </div>
                        </div>
                        <div>
                            <Table condensed hover>
                                <thead>
                                <tr>
                                    <th className="col-md-1 col-sm-1 col-xs-1">Version</th>
                                    <th className="col-md-2 col-sm-2 col-xs-2">Release Date</th>
                                    <th className="col-md-3 col-sm-3 col-xs-3">Platform Requirement<OverlayTrigger
                                            placement="right" overlay={requirementmeaning}>
                                        <Glyphicon glyph="glyphicon glyphicon-question-sign"/>
                                    </OverlayTrigger></th>
                                    <th className="col-md-5 col-sm-5 col-xs-5">Other requirements</th>
                                    <th className="col-md-1 col-sm-1 col-xs-1">Download</th>
                                </tr>
                                </thead>
                                <tbody>
                                {addon.versions.map(v => {
                                    let className = v.version === highlightVersion ? "highlight" : "";
                                    return (
                                            <tr className={className}>
                                                <td>
                                                    {v.version}
                                                </td>
                                                <td>
                                                    {this.formatDateTime(v.releaseDatetime)}
                                                </td>
                                                <td>
                                                    {v.requireOpenmrsVersion}
                                                </td>
                                                <td>
                                                    {this.formatRequiredModules(v)}
                                                </td>
                                                <td>
                                                    <Button bsSize="small"
                                                            href={v.renameTo ? `/api/v1/addon/${addon.uid}/${v.version}/download` : v.downloadUri}>
                                                        <i className="fa fa-download"></i>
                                                        Download
                                                    </Button>
                                                </td>
                                            </tr>
                                    )
                                })}

                                </tbody>
                            </Table>

                        </div>
                    </div>

            )
        }
        else {
            return <div>Loading...</div>
        }
    }

}
