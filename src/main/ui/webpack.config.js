/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
const { resolve } = require("path");

const { CleanWebpackPlugin } = require("clean-webpack-plugin");
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const HtmlWebpackPlugin = require("html-webpack-plugin");
const webpack = require("webpack");

module.exports = (env) => {
  return {
    entry: "./app/index.tsx",
    output: {
      filename: env.prod
        ? "app/bundle.[contenthash].min.js"
        : "app/bundle.[contenthash].js",
      publicPath: "/",
      path: resolve(__dirname, "../../../target/classes/static"),
    },
    optimization: {
      runtimeChunk: "single",
      splitChunks: {
        chunks: "all",
      },
    },
    module: {
      rules: [
        {
          test: /\.(?:j|t)sx?$/,
          exclude: resolve(__dirname, "node_modules"),
          loader: "babel-loader",
        },
        {
          test: /\.(j|t)sx?$/,
          enforce: "pre",
          use: ["source-map-loader"],
        },
        {
          test: /\.scss$/,
          use: [
            MiniCssExtractPlugin.loader,
            {
              loader: "css-loader",
              options: {
                sourceMap: !env.prod,
              },
            },
            {
              loader: "sass-loader",
              options: {
                sourceMap: !env.prod,
              },
            },
          ],
        },
      ],
    },
    plugins: [
      new MiniCssExtractPlugin({
        filename:
          env === "prod"
            ? "app/styles.[contenthash].min.css"
            : "app/styles.[contenthash].css",
      }),
      new CleanWebpackPlugin({
        cleanOnceBeforeBuildPatterns: ["app/"],
      }),
      new webpack.ids.HashedModuleIdsPlugin(),
      new HtmlWebpackPlugin({
        template: resolve(__dirname, "index.html"),
      }),
      new webpack.DefinePlugin({
        GA_ID: env.prod ? JSON.stringify("UA-16695719-3") : undefined,
      }),
    ],
    resolve: {
      extensions: [".ts", ".json", ".tsx", ".js", ".jsx", ".d.ts"],
    },
  };
};
