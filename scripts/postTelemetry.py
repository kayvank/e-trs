import requests
import json

with open('/Users/kayvan/dev/workspaces/workspace-vevo/e-trs/src/test/resources/telemetry-endo.json') as json_file:
    json_data = json.load(json_file)


with open('/Users/kayvan/dev/workspaces/workspace-vevo/e-trs/src/test/resources/ec-telemetry-endo.json') as json_file:
    json_bad_data = json.load(json_file)

##url = 'https://event-collector.vevodev.com/event/endo'
##url = 'http://p3-eventcollector.us-east-1.elasticbeanstalk.com/event/endo'
url = 'http://stg-event-collector.us-east-1.elasticbeanstalk.com/event/endo'
##url = 'http://localhost:9000/event/endo'
headers = {'Accept' : 'application/json', 'Content-Type' : 'application/json'}
r0 = requests.post(url, data=json.dumps(json_data), headers=headers, verify=False)
r1 = requests.post(url, data=json.dumps(json_bad_data), headers=headers, verify=False)
print r0.status_code
print r1.status_code
print r1.text
