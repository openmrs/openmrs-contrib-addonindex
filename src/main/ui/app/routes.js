import App from "./route/App";
import Home from "./route/Home";
import About from "./route/About";
import Show from "./route/Show";

export default {
    path: '/',
    component: App,
    indexRoute: {component: Home},
    childRoutes: [
        {path: 'about', component: About},
        {path: 'show/:uid', component: Show}
    ]
}