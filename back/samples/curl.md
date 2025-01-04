# curl

## device file

Example command to upload file for device

```sh
AUTHORIZATION_BEARER="device_group:1:device_group_api_key:1:aaaa"
PATH_FILE="your-local/image.png"
HOST=http://localhost:3000
curl -X POST ${HOST}/api/device_file \
   -H "Authorization: Bearer ${AUTHORIZATION_BEARER}" \
   -F "file=@$PATH_FILE"
```

## device

Example commands to create device

```sh
AUTHORIZATION_BEARER="device_group:1:device_group_api_key:1:aaaa"
HOST=http://localhost:3000
curl -X POST ${HOST}/api/device \
   -H 'Content-Type: application/json' \
   -H "Authorization: Bearer ${AUTHORIZATION_BEARER}" \
   -d "{\"device\":{\"name\":"0000999"}}"
```

## raw_device_log

Example commands to post raw_device_log.

### post with using device hash

```sh
MACHINE_ID=`cat /etc/machine-id`
AUTHORIZATION_BEARER="device:1:aaaa"
HOST=http://localhost:3000
curl -X POST ${HOST}/api/raw_device_log \
   -H 'Content-Type: application/json' \
   -H "Authorization: Bearer ${AUTHORIZATION_BEARER}" \
   -d "{\"type\":\"test\",\"machine_id\":\"${MACHINE_ID}\"}"
```

### post with using authorization bearer

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
