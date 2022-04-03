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
