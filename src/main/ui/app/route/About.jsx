import {Component} from "react";
import {Link} from "react-router";

export default class About extends Component {

    render() {
        return (
                <div>
                    <h3>About</h3>

                    <p>
                        <Link to="/indexingStatus">Indexing Status</Link>
                    </p>

                    <p>
                        <a target="_blank"
                           href="https://github.com/djazayeri/openmrs-contrib-addonindex/blob/master/README.md">Read more on
                                                                                                                GitHub</a>
                    </p>
                </div>
        )
    }

}