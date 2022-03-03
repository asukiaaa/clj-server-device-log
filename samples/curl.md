```
MACHINE_ID=`cat /etc/machine-id`
AUTHORIZATION_BEARER="XXYYZZ"
curl -X POST http://localhost:3000/api/raw_device_log \
   -H 'Content-Type: application/json' \
   -H "Authorization: Bearer ${AUTHORIZATION_BEARER}" \
   -d "{\"login\":\"my_login\",\"password\":\"my_password\",\"machine_id\":\"${MACHINE_ID}\"}"
```
