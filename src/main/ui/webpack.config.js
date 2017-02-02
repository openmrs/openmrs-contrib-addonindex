const ExtractTextPlugin = require("extract-text-webpack-plugin");

module.exports = {
    entry: './app/index.jsx',
    output: {
        filename: 'bundle.js',
        path: '../resources/static/app'
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
        new ExtractTextPlugin("styles.css")
    ],
    resolve: {
        extensions: ['.js', '.json', '.jsx']
    },
    externals: { // JS libraries that we link from a CDN in index.html
        'react': 'React',
        'react-dom': 'ReactDOM',
        'react-router': 'ReactRouter'
    }
}