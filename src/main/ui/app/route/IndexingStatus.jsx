import {Component} from "react";

export default class IndexingStatus extends Component {

    componentDidMount() {
        fetch('/api/v1/indexingstatus')
                .then(response => {
                    return response.json();
                })
                .then(status => {
                    this.setState({status: status});
                })
    }

    render() {
        if (this.state && this.state.status) {
            return (
                    <div>
                        <h3>Indexing Status</h3>
                        <table>
                            { this.state.status.toIndex.toIndex.map(i =>
                                               <tr>
                                                    <td>{i.uid } </td>
                                                    <td>
                                                      <pre>{JSON.stringify(this.state.status.statuses[i.uid], null, 2)}</pre>
                                                     </td>
                                              </tr>
                            )}
                        </table>
                    </div>
            )
        }
        else {
            return <div>Loading...</div>
        }
    }

}
