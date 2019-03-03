import requests
import json

with open('/Users/kayvan/dev/workspaces/workspace-vevo/e-trs/src/test/resources/telemetry-endo.json') as json_file:
    json_data = json.load(json_file)

url = 'http://prd-p-xshtn2lq6jj6.cloud.splunk.com:8088/services/collector/event'
headers = {'Authorization': 'Splunk AC6DDEDF-821D-40CD-898A-967881F0169B', 'Accept' : 'application/json', 'Content-Type' : 'application/json'}
r = requests.post(url, data=json.dumps(json_data), headers=headers, verify=False)
print r.status_code
print r.text
