```
MACHINE_ID=`cat /etc/machine-id`
AUTHORIZATION_BEARER="XXYYZZ"
HOST=http://localhost:3000
curl -X POST ${HOST}/api/raw_device_log \
   -H 'Content-Type: application/json' \
   -H "Authorization: Bearer ${AUTHORIZATION_BEARER}" \
   -d "{\"type\":\"test\",\"machine_id\":\"${MACHINE_ID}\"}"
```
