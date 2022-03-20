# cljs-reframe-practice

Latest react version is 17 but reagent requires 16.

## Setup

- clojure cli
- yarn

Downlooad node_modules.
```bash
yarn
```

## Usage

### Build

#### hot reload
```bash
clj -M:dev
```

#### once
```bash
clj -M:build
```

### Include
Then include out/main.js like this.
```html
<script href="./out-webpack/main.js" type="text/javascript" />
```

## References

- [Using NPM - figwheel-main](https://figwheel.org/docs/npm.html)
- [figwheel Configuration Options](https://figwheel.org/config-options.html)
- [cljs webpack](https://clojurescript.org/guides/webpack)
- [0.8 Upgrade guide](https://cljdoc.org/d/reagent/reagent/1.1.0/doc/other/0-8-upgrade-guide)
- [reagent/test-environments/bundle/build.edn](https://github.com/reagent-project/reagent/blob/master/test-environments/bundle/build.edn)
- [Quick Start - github](https://github.com/clojure/clojurescript-site/blob/53de8b8af3f6b3567e1f40838bd56e8cde022edd/content/guides/quick-start.adoc)
- [clojurescript](https://github.com/clojure/clojurescript)
- [reagent simple example](https://github.com/reagent-project/reagent/blob/master/examples/simple/src/simpleexample/core.cljs)
