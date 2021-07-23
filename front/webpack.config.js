module.exports = {
    // モード値を production に設定すると最適化された状態で、
    // development に設定するとソースマップ有効でJSファイルが出力される
    mode: "development",

    // メインとなるJavaScriptファイル（エントリーポイント）
    entry: "./src/main.tsx",
    // ファイルの出力設定
    output: {
        //  出力ファイルのディレクトリ名
        path: `${__dirname}/dist`,
        // 出力ファイル名
        filename: "index.js"
    },
    module: {
        rules: [
            {
                // 拡張子 .ts もしくは .tsx の場合
                test: /\.tsx?$/,
                // TypeScript をコンパイルする
                use: "ts-loader"
            }
        ]
    },
    // ES5(IE11等)向けの指定（webpack 5以上で必要）
    target: ["web", "es5"],
    // target: ["web"],
    devServer: {
        publicPath: "/",
        contentBase: "./public",
        hot: true,
        port: 7999,
        public: "cbcaddf219f9.ngrok.io"
        // public: true
    },
    resolve: {
        // import 文で .ts や .tsx ファイルを解決するため
        extensions: [".ts", ".tsx", ".js", ".json"],
        fallback: {
            crypto: require.resolve("crypto-browserify"),
            stream: false,
            // crypto: false
        }
    }
};
