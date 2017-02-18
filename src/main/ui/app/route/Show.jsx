import {Component} from "react";
import fetch from "isomorphic-fetch";
import {Panel , Table , Button , OverlayTrigger , Tooltip , Glyphicon , Badge} from 'react-bootstrap';
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
                })
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


    render() {
        if (this.state && this.state.error) {
            return <div>{this.state.error}</div>
        }
        else if (this.state && this.state.addon) {
            const addon = this.state.addon;
            const icon = addon.type === 'OWA' ? 'fa fa-globe' : 'fa fa-puzzle-piece';
            const highlightVersion = this.props.location.query.highlightVersion;
	        const showtitle = (
                                <div><h2>{addon.name}</h2></div>
                               );
	    const versioncountmeaning = (
                                <Tooltip id="tooltip"><strong>No. of builds of {addon.name}</strong></Tooltip>
                            );
            const requirementmeaning = (
                                <Tooltip id="tooltip"><strong>Minimum version of the OpenMRS Platform required.</strong></Tooltip>
                            );
	    let check="";
            let hosted = "";
            let hostedtext="";
	    let statusClass = addon.status === "ACTIVE" ? "" :
                             addon.status === "DEPRECATED" ? "danger" : "warning";
            if (addon.hostedUrl) {
                if (addon.hostedUrl.includes("bintray.com")) {
                    hostedtext="Hosted on";
                    hosted = <a target="_blank" href={addon.hostedUrl}>Bintray</a>
                }
                else {
                    hostedtext="Hosted at";
                    hosted =<a target="_blank" href={addon.hostedUrl}>{addon.hostedUrl}</a>
                }
	    check=(<tr>
    <th>{hostedtext}</th>
    <td><h5>{hosted}</h5></td>

</tr>);
            }
          
            return (

<div className="showpage-body">
{showtitle}
<h4 className="lead">{addon.description}</h4>
<div className="col-md-12 col-sm-12 col-xs-12 left-margin">
<Table condensed>
<tbody>
<tr>
<th>Type :</th>
<td><h5>{addon.type}</h5></td>
</tr>
{check}
<tr>
        <th>Maintained by: </th>
        <td><h5>{addon.maintainers.map(m => {
                                if (m.url) {
                                    return (
                                    <Link to={m.url}>
                                   <span>{m.name},&nbsp;</span>  
				    </Link>				
	                                 )
                                } else {
                                    return(
                                    <span>{m.name},&nbsp;</span>
                                    )
                                }
                            })}
    </h5></td>
</tr>
<tr>
<th>
Addon status:
</th>
<td>
<Label bsStyle={statusClass}>{addon.status}</Label>
</td>
</tr>
<tr>
<th>
Version info:
</th>
<td>
<span>Latest version:&nbsp;<Badge>{addon.latestVersion}</Badge>&nbsp;,&nbsp;</span><span>Total versions:&nbsp;<Badge>{addon.versionCount}</Badge> </span>
</td>
</tr>
</tbody>
</Table>
</div>
<div>            
<Table condensed hover>
    <thead>
      <tr>
        <th className="col-md-1 col-sm-1 col-xs-1">Version</th>
        <th className="col-md-3 col-sm-4 col-xs-4">OpenMRS Version Requirement<OverlayTrigger placement="right" overlay={requirementmeaning}>
      <Glyphicon glyph="glyphicon glyphicon-question-sign" />
    </OverlayTrigger></th>
        <th className="col-md-7 col-sm-6 col-xs-6">Prerequisite requirements</th>
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
                                                {v.requireOpenmrsVersion}
                                            </td>
                                            <td>
                                                {this.formatRequiredModules(v)}
                                            </td>
                                            <td>
                                            <Button bsSize="small" href={v.renameTo ? `/api/v1/addon/${addon.uid}/${v.version}/download` : v.downloadUri}>
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
