# clj-server-practice

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
