
e-trs Splunk 
=======

## Configuration
Configuring e-trs Splunk :
* Activate Splunk Http Event Collector, HEC
* Create Token
* configure AWS Splunk lambda parameters to use the Splunk Token & URL

### Activate Splunk HEC
HEC is activated form the [Splunk Admin screen](https://prd-p-xshtn2lq6jj6.cloud.splunk.com/en-US/manager/search/http-eventcollector) by:
1. Global Settings
2. Enable All tokens
3. Default Source Type = _json
4. Default Index = Default

### Create Token
Splunk Token is used by HEC to route the paylods to right indexer.  Follow the general Splunk instruction for setting up the token with the followring properties:
* Source Type = _json
* disable  "Enable indexer acknowledgement"

### Configure AWS Lambda
e-trs-splunk-lambda streams events from AWS Kinesis, telmetry-1.0 stream to Splunk Cloud service. To configure this lambda:

```
const loggerConfig = {
    url: process.env.SPLUNK_HEC_URL || 'https://input-prd-p-xshtn2lq6jj6.cloud.splunk.com:8088/services/collector',
    token: process.env.SPLUNK_HEC_TOKEN || '3C3EE205-6DA0-4750-8C72-D3C053CF6310',
};
```

## e-trs Splunk related links

- [splunk-dev-ur](https://prd-p-xshtn2lq6jj6.cloud.splunk.com/en-US)
- [splunk-token](https://prd-p-xshtn2lq6jj6.cloud.splunk.com/en-US/manager/search/http-eventcollector)
- [Sample endo-event with telemetry data](https://github.com/VEVO/e-trs/blob/master/src/test/resources/telemetry-endo.json)

## Splunk Search
A typical search starts with:
```
index=* sourcetype=_json
```
From there you may adjust the temporal sliding window, click on fields of intetest to add them to the search criteria.  Here is a sampl:
```
index=* sourcetype=_json| spath "telemetry.android-key1" | search "telemetry.android-key1"=val-1

```

## References

 [Splunk docs](https://docs.splunk.com/Documentation)
