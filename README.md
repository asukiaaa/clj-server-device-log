# clj-server-practice

[{"key": "data","json_key":"camera_id","group_by":{"key":"created_at","order"}}]

A project to practice of creating web server in clojure.

## Setup

Install docker-compose

## Usage

Run

```bash
docker-compose up
```

See http://localhost:3000


Access to mariadb

```bash
./bin/mariadb
```

## Test

```bash
docker-compose run back clj -X:test:runner
```

## Deploy to heroku

### Setup

```
heroku login
heroku plugins:install java
```

### Deploy

```bash
./bin/deploy-heroku your-heroku-app-name
```

## References

- [clojure cliプロジェクトをherokuで動かす](https://asukiaaa.blogspot.com/2022/03/clojure-cli-on-heroku.html)
