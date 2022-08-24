#!/bin/bash
set -ex

./deploy-sync.sh

stack_name=$(jq -r '.stackName' target/dev-resources/stack-properties.json)
bucket_name=$(jq -r '.[] | select(.ParameterKey == "DeploymentBucketName").ParameterValue' target/dev-resources/deployment-parameters.json)

aws cloudformation update-stack \
  --profile mggdev \
  --stack-name $stack_name \
  --template-url https://s3.amazonaws.com/$bucket_name/stack/quake-net-stack.yaml \
  --parameters file://target/dev-resources/quake-net-parameters.json