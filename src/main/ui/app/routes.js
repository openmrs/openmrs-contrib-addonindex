import App from "./route/App";
import Home from "./route/Home";
import About from "./route/About";
import Show from "./route/Show";
import IndexingStatus from "./route/IndexingStatus";

export default {
    path: '/',
    component: App,
    indexRoute: {component: Home},
    childRoutes: [
        {path: 'about', component: About},
        {path: 'indexingStatus', component: IndexingStatus},
        {path: 'show/:uid', component: Show}
    ]
}