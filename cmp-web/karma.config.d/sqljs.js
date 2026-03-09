const path = require("path");
const CopyWebpackPlugin = require("copy-webpack-plugin");

config.set({
    webpack: {
        plugins: [
            new CopyWebpackPlugin({
                patterns: [
                    {
                        from: path.resolve(
                            __dirname,
                            "../node_modules/sql.js/dist/sql-wasm.wasm"
                        ),
                        to: path.resolve(__dirname, "../kotlin/"),
                    },
                ],
            }),
        ],
    },
});