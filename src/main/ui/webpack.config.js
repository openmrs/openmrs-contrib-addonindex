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
            }
        ]
    },
    resolve: {
        extensions: ['.js', '.json', '.jsx']
    },
    externals: {
        'react': 'React',
        'react-dom': 'ReactDOM'
    }
}