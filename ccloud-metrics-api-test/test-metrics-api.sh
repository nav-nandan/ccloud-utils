#!/bin/bash

API_KEY='<YOUR_API_KEY>'
API_SECRET='<YOUR_API_SECRET>'
CLUSTER_ID='<YOUR_CLUSTER_ID>'

ccloud login
ccloud kafka cluster use "$CLUSTER_ID"

# # List the available metrics
http -v https://api.telemetry.confluent.cloud/v1/metrics/cloud/descriptors --auth "$API_KEY:$API_SECRET"

# List the available topics for a given metric in a specified interval
http -v https://api.telemetry.confluent.cloud/v1/metrics/cloud/attributes --auth "$API_KEY:$API_SECRET" < attributes_query.json

# Query for bytes sent to consumers per minute grouped by topic
http -v https://api.telemetry.confluent.cloud/v1/metrics/cloud/query --auth "$API_KEY:$API_SECRET" < sent_bytes_query.json

# Query for bytes sent by producers per minute grouped by topic
http -v https://api.telemetry.confluent.cloud/v1/metrics/cloud/query --auth "$API_KEY:$API_SECRET" < received_bytes_query.json

# Query for max retained bytes per hour over 2 hours for topic named test-topic
http -v https://api.telemetry.confluent.cloud/v1/metrics/cloud/query --auth "$API_KEY:$API_SECRET" < retained_bytes_query.json

# Query for max retained bytes per hour over 2 hours for a cluster lkc-XXXX
http -v https://api.telemetry.confluent.cloud/v1/metrics/cloud/query --auth "$API_KEY:$API_SECRET" < cluster_retained_bytes_query.json

# Query for metrics currently available for a cluster
http -v https://api.telemetry.confluent.cloud/v1/metrics/cloud/available --auth "$API_KEY:$API_SECRET" < available_query.json
