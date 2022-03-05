select by json data.
```
select * from raw_device_log where json_contains(data, '"11:22:33:44"', "$.mac_address");
```
