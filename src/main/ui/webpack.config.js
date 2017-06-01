/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

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