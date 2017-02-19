import {Component} from "react";
import SearchBox from "../component/SearchBox";
import AddOnList from "../component/AddOnList";
import NamedList from "../component/NamedList";

export default class Home extends Component {

    constructor(props) {
        super(props);
        this.state = {};
    }

    componentDidMount() {
        fetch('/api/v1/list/DEFAULT')
                .then(response => {
                    return response.json();
                })
                .then(json => {
                    this.setState({defaultList: json});
                })
    }

    handleSearchResults(searchResults) {
        this.setState({
                          searchResults: searchResults
                      });
    }

    renderDefaultList() {
        if (this.state.defaultList) {
            console.log(this.state.defaultList);
            return <NamedList list={this.state.defaultList}/>
        }
        else {
            return <div>Loading...</div>
        }
    }

    render() {
        return (
                <div>
                    <SearchBox onSearchResults={(sr) => this.handleSearchResults(sr)}/>

                    { this.state.searchResults ?
                      <AddOnList addons={this.state.searchResults} heading={`${this.state.searchResults.length} results`}/>
                            :
                      this.renderDefaultList()
                    }
                </div>
        )
    }

}