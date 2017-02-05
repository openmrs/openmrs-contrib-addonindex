import {Component} from "react";
import fetch from "isomorphic-fetch";
import AddOnList from "./AddOnList";
import DebounceInput from "react-debounce-input";


export default class AddOnSearch extends Component {

    constructor(props) {
        super(props);
        this.state = {};
    }

    componentDidMount() {
        fetch('/api/v1/addon')
                .then(response => {
                    return response.json();
                })
                .then(json => {
                    this.setState({allAddOns: json});
                })
    }

    setType(type) {
        this.setState({
                          type: type,
                          searchResults: null
                      }, this.doSearch);

    }

    setQuery(query) {
        this.setState({
                          query: query,
                          searchResults: null
                      }, this.doSearch);
    }

    doSearch() {
        if (this.state.type || this.state.query) {
            var url = "/api/v1/addon?";
            if (this.state.type) {
                url += "type=" + this.state.type;
            }
            if (this.state.query) {
                url += "&q=" + this.state.query;
            }
            fetch(url)
                    .then(response => {
                        return response.json();
                    })
                    .then(json => {
                        this.setState({searchResults: json});
                    });
        }
    }

    render() {
        return (
                <div className="row col-md-12 col-sm-12 col-xs-12 pushdown">
                    <div className="col-lg-12">
    <div className="input-group">
    
      <div className="input-group-btn">
        <button type="button" className="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">Type <span className="caret"></span></button>
        <select className="dropdown-menu" onChange={event => this.setType(event.target.value)}>
          <option value="">All Types</option>
          <option value="OMOD">Module (OMOD)</option>
          <option value="OWA">Open Web App (OWA)</option>
        </select>
      </div>
  
  
                    <DebounceInput
                            placeholder="Search..."
                            className="form-control col-md-10 col-sm-10 col-xs-10"
                            minLength={1}
                            debounceTimeout={500}
                            onChange={event => this.setQuery(event.target.value)}/>
        
                   
        
                
                  </div>
                  </div>

                    { (this.state.query || this.state.type) ?
                      <AddOnList addons={this.state.searchResults}/>
                            :
                      <AddOnList addons={this.state.allAddOns}/>
                    }
                </div>
                
        )
    }
}