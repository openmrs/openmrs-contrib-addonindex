import {Component} from "react";
import {Link} from "react-router";

export default class About extends Component {

    render() {
        return (
                <div>
                    
                    <h1>About</h1>

                    <h3>1)How to add your module?</h3>

                    <p> Adding your module to Add Ons for indexing is as easy as creating a pull request!
                        Before we do that, we need to ensure that your module has been hosted on one of the sites that Add Ons currently supports indexing from: <br/>
                        a) Bintray <br/>
                        b) Modulus <br/>
                    <br/>
                        Currently we only support these 2 servers but in the future we sure will add more based on user demand! Currently, we are working on adding support for Github Releases as well!
                    <br/>
                        Once you have confirmed the above prerequisite, you may head over to the <a target="_blank"
                           href="https://github.com/openmrs/openmrs-contrib-addonindex/blob/master/PUBLISHING-AN-ADD-ON.md">Publishing an Add on document</a> on Github which contains a comprehensive list of steps.
                    
                    </p>

                    <h3>2)How to contribute to Add Ons development?</h3>
                    <p>You may find the source code for Add Ons <a target="_blank"
                           href="https://github.com/openmrs/openmrs-contrib-addonindex">here</a> , for more details you may also view the <a target="_blank"
                           href="https://github.com/openmrs/openmrs-contrib-addonindex/blob/master/CONTRIBUTING.md">CONTRIBUTING.md document</a>
                    </p>

                    <h3>Addons Stats:</h3>
                    <div>
                        <Link to="/indexingStatus">Indexing Status</Link></div>
                    </div>
        )
    }

}
