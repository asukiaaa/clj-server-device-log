# curl

Example commands to post data.

## post with using device hash

```sh
HASH_POST="device:1:aaaa"
HOST=http://localhost:3000
MACHINE_ID=`cat /etc/machine-id`
curl -X POST ${HOST}/api/raw_device_log?key_post=${HASH_POST} \
   -H 'Content-Type: application/json' \
   -H "Authorization: Bearer ${AUTHORIZATION_BEARER}" \
   -d "{\"type\":\"test\",\"machine_id\":\"${MACHINE_ID}\"}"
```

## post with using authorization bearer

```sh
MACHINE_ID=`cat /etc/machine-id`
AUTHORIZATION_BEARER="XXYYZZ"
HOST=http://localhost:3000
curl -X POST ${HOST}/api/raw_device_log \
   -H 'Content-Type: application/json' \
   -H "Authorization: Bearer ${AUTHORIZATION_BEARER}" \
   -d "{\"type\":\"test\",\"machine_id\":\"${MACHINE_ID}\"}"
```

```sh
MACHINE_ID=`cat /etc/machine-id`
AUTHORIZATION_BEARER="XXYYZZ"
HOST=http://localhost:3000
curl -X POST ${HOST}/api/raw_device_log \
   -H 'Content-Type: application/json' \
   -H "Authorization: Bearer ${AUTHORIZATION_BEARER}" \
   -d "{\"type\":\"test\",\"machine_id\":\"${MACHINE_ID}\",\"space test\":\"value of space key\"}"
```

```sh
AUTHORIZATION_BEARER="XXYYZZ"
HOST=http://localhost:3000
curl -X POST ${HOST}/graphql \
   -H 'Content-Type: application/json' \
   -H "Authorization: Bearer ${AUTHORIZATION_BEARER}" \
   -d "{ \"query\": \"{ game_by_id(id: \\\"123abc\\\") { id name }}\"}"
```

```sh
AUTHORIZATION_BEARER="XXYYZZ"
HOST=http://localhost:3000
curl -X POST ${HOST}/graphql \
   -H 'Content-Type: application/json' \
   -H "Authorization: Bearer ${AUTHORIZATION_BEARER}" \
   -d "{ \"query\": \"{ raw_device_logs{ list { id data created_at } total }}\"}"
```

```sh
AUTHORIZATION_BEARER="XXYYZZ"
HOST=http://localhost:3000
curl -X POST ${HOST}/graphql \
   -H 'Content-Type: application/json' \
   -H "Authorization: Bearer ${AUTHORIZATION_BEARER}" \
   -d "{ \"query\": \"{ raw_device_logs(limit: 1){ list { id data created_at } total }}\"}"
```

```sh
AUTHORIZATION_BEARER="XXYYZZ"
HOST=http://localhost:3000
curl -X POST ${HOST}/graphql \
   -H 'Content-Type: application/json' \
   -H "Authorization: Bearer ${AUTHORIZATION_BEARER}" \
   -d "{ \"query\": \"{ raw_device_logs(where: \\\"[{\\\\\\\"key\\\\\\\": \\\\\\\"created_at\\\\\\\", \\\\\\\"action\\\\\\\": \\\\\\\"gt\\\\\\\", \\\\\\\"value\\\\\\\": \\\\\\\"2022-03-06 00:00:00\\\\\\\"}]\\\"){ list { id data created_at } total }}\"}"
```

```sh
AUTHORIZATION_BEARER="XXYYZZ"
HOST=http://localhost:3000
# WHERE_RAW='[{"key":"created_at","action":"gt","value":"2022-03-06 00:00:00"}]'
# WHERE_STR_ESCAPED=$(printf "%q" "$(printf "\"%q\"" "$WHERE_RAW")")
curl -X POST ${HOST}/graphql \
   -H 'Content-Type: application/json' \
   -H "Authorization: Bearer ${AUTHORIZATION_BEARER}" \
   -d "{ \"query\": \"{ raw_device_logs(where: \\\"[{\\\\\\\"key\\\\\\\": \\\\\\\"created_at\\\\\\\", \\\\\\\"action\\\\\\\": \\\\\\\"gt\\\\\\\", \\\\\\\"value\\\\\\\": \\\\\\\"2022-03-06 00:00:00\\\\\\\"}]\\\", order: \\\"[{\\\\\\\"key\\\\\\\": \\\\\\\"id\\\\\\\", \\\\\\\"dir\\\\\\\": \\\\\\\"asc\\\\\\\"}]\\\"){ list { id data created_at } total }}\"}"
```
