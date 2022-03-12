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

#### once
```bash
clj -M -m cljs.main -co build.edn -O advanced -v -c
```

#### auto
```
clj -M -m cljs.main -co build.edn -v --watch ./src -c
```

TODO hot reload

### Include
Then include out/main.js like this.
```html
<base href="/path-public-dir">
<script href="./out/main.js" type="text/javascript" />
```

## References

- [cljs webpack](https://clojurescript.org/guides/webpack)
- [0.8 Upgrade guide](https://cljdoc.org/d/reagent/reagent/1.1.0/doc/other/0-8-upgrade-guide)
- [reagent/test-environments/bundle/build.edn](https://github.com/reagent-project/reagent/blob/master/test-environments/bundle/build.edn)
- [Quick Start - github](https://github.com/clojure/clojurescript-site/blob/53de8b8af3f6b3567e1f40838bd56e8cde022edd/content/guides/quick-start.adoc)
- [clojurescript](https://github.com/clojure/clojurescript)
- [reagent simple example](https://github.com/reagent-project/reagent/blob/master/examples/simple/src/simpleexample/core.cljs)
