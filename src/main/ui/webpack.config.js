/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

const { CleanWebpackPlugin } = require('clean-webpack-plugin');
const path = require('path');
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const HtmlWebpackPlugin = require("html-webpack-plugin");
const webpack = require('webpack');

module.exports = function (env) {

    return {
        entry: './app/index.jsx',
        output: {
            filename:  env === 'prod' ? 'app/bundle.[contenthash].min.js': 'app/bundle.[contenthash].js',
            publicPath: '/',
            path: path.resolve(__dirname, '../resources/static/')
        },
        optimization: {
            runtimeChunk: 'single',
            splitChunks: {
                cacheGroups: {
                    vendor: {
                        test: /[\\/]node_modules[\\/]/,
                        name: 'vendors',
                        chunks: 'all'
                    }
                }
            }},
        module: {
            rules: [
                {
                    test: /\.jsx?$/,
                    exclude: path.resolve(__dirname, 'node_modules'),
                    loader: "babel-loader",
                    options: {
                        presets: ['@babel/env', '@babel/preset-react']
                    }
                },
                {
                    test: /\.scss$/,
                    exclude: /node_modules/,
                    use: [
                        MiniCssExtractPlugin.loader,
                        'css-loader','sass-loader'
                    ]
                }
            ]
        },
        plugins: [
            new MiniCssExtractPlugin({
                    filename: env === 'prod' ? 'app/styles.[contenthash].min.css': 'app/styles.[contenthash].css'
                }),
            new CleanWebpackPlugin({
                    cleanOnceBeforeBuildPatterns: ['app/']
            }
            ),
            new webpack.HashedModuleIdsPlugin(),
            new HtmlWebpackPlugin({
                template: 'index.ejs'
            }),
            new webpack.DefinePlugin({
                GA_ID: env === 'prod' ? JSON.stringify('UA-16695719-3') : undefined
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