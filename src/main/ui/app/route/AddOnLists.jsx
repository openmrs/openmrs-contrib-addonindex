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
                        <ul>
                            {this.state.lists.map(list =>
                                                          <li>
                                                              <Link to={`/list/${list.uid}`}>
                                                                  <strong>{list.name}</strong>
                                                              </Link>
                                                              {list.description}
                                                              ...{list.addOns.length} add-on(s)
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