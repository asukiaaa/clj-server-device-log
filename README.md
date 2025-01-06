# clj-server-device-log

A project to practice of creating web server in clojure.

## Setup

Install docker compose

## Usage

Run

```bash
docker compose up
```

See http://localhost:3000

Sample curl commands are written on [curl.md](./back/samples/curl.md).

Access to mariadb

```bash
./bin/mariadb
```

## Test

```bash
docker compose run back clj -X:test:runner
```

## References

- [clojure cliプロジェクトをherokuで動かす](https://asukiaaa.blogspot.com/2022/03/clojure-cli-on-heroku.html)
