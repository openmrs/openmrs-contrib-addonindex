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

    handleStartSearch(searchKey) {
        this.setState({
                          latestSearch: searchKey
                      });
    }

    handleSearchResults(searchKey, searchResults) {
        if (this.state.latestSearch === searchKey) {
            this.setState({
                              latestSearch: null,
                              searchResults: searchResults
                          });
        }
    }

    renderDefaultList() {
        if (this.state.defaultList) {
            return <NamedList list={this.state.defaultList}/>
        }
        else {
            return <div>Loading...</div>
        }
    }

    render() {
        return (
                <div>
                    <SearchBox
                            onStartSearch={(key) => this.handleStartSearch(key)}
                            onSearchResults={(key, results) => this.handleSearchResults(key, results)}
                    />

                    {this.state.latestSearch ?
                     <div>Searching for {this.state.latestSearch}</div>
                            :
                     this.state.searchResults ?
                     <AddOnList addons={this.state.searchResults} heading={`${this.state.searchResults.length} result(s)`}/>
                             :
                     this.renderDefaultList()
                    }
                </div>
        )
    }

}