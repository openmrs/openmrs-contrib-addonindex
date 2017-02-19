import {Component} from "react";
import {Link} from "react-router";

export default class ListOfLists extends Component {

    componentDidMount() {
        fetch('/api/v1/list')
                .then(response => {
                    return response.json();
                })
                .then(json => {
                    this.setState({lists: json});
                });
    }

    render() {
        const LISTS_TO_SHOW = 3;
        // show {list.description} in a tooltip
        // consider showing ...{list.addOns.length} add-on(s)
        if (this.state && this.state.lists) {
            const toShow = this.state.lists.slice(0, LISTS_TO_SHOW);
            const anyMore = this.state.lists.length > LISTS_TO_SHOW;
            const selectedUid = this.props ? this.props.selectedUid : null;
            return (
                    <ul className="nav nav-pills">
                        <li>
                            <Link to="/">
                                <strong>Search</strong>
                            </Link>
                        </li>
                        {toShow.map(list =>
                                            <li className={list.uid === selectedUid ? "active" : ""}>
                                                <Link to={`/list/${list.uid}`}>
                                                    <strong>{list.name}</strong>
                                                </Link>
                                            </li>
                        )}
                        {anyMore ?
                         <li>
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
