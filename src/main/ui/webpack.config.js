const ExtractTextPlugin = require("extract-text-webpack-plugin");
const HtmlWebpackPlugin = require("html-webpack-plugin");

module.exports = function (env) {
    const BUNDLE_FILENAME = env === "prod" ? "app/bundle.min.js" : "app/bundle.js";
    const CSS_FILENAME = env === "prod" ? "app/styles.min.css" : "app/styles.css";

    return {
        entry: './app/index.jsx',
        output: {
            filename: BUNDLE_FILENAME,
            path: '../resources/static'
        },
        module: {
            rules: [
                {
                    test: /\.jsx?$/,
                    exclude: 'node_modules',
                    loader: "babel-loader",
                    options: {
                        presets: ['es2015', 'react']
                    }
                },
                {
                    test: /\.scss$/,
                    exclude: /node_modules/,
                    use: ExtractTextPlugin.extract({
                                                       loader: ["css-loader", "sass-loader"],
                                                       fallbackLoader: "style-loader"
                                                   })
                    // use: ['style-loader', 'css-loader', 'sass-loader']
                }
            ]
        },
        plugins: [
            new ExtractTextPlugin(CSS_FILENAME),
            new HtmlWebpackPlugin({
                template: 'index.ejs'
            })
        ],
        resolve: {
            extensions: ['.js', '.json', '.jsx']
        },
        externals: { // JS libraries that we link from a CDN in index.ejs
            'react': 'React',
            'react-dom': 'ReactDOM',
            'react-router': 'ReactRouter'
        }
    }
}