import App from "./route/App";
import Home from "./route/Home";
import About from "./route/About";
import Show from "./route/Show";
import SearchPage from "./route/SearchPage";
import AddOnLists from "./route/AddOnLists";
import ShowList from "./route/ShowList";
import IndexingStatus from "./route/IndexingStatus";

export default {
    path: '/',
    component: App,
    indexRoute: {component: Home},
    childRoutes: [
        {path: 'about', component: About},
        {path: 'indexingStatus', component: IndexingStatus},
        {path: 'search', component: SearchPage},
        {path: 'show/:uid', component: Show},
        {path: 'lists', component: AddOnLists},
        {path: 'list/:uid', component: ShowList},
    ]
}