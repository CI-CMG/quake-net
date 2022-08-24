#!/bin/bash
set -ex

version=$(jq -r '.version' target/dev-resources/stack-properties.json)
bucket_name=$(jq -r '.[] | select(.ParameterKey == "DeploymentBucketName").ParameterValue' target/dev-resources/deployment-parameters.json)

rm -rf target/quake-net-cloudformation-$version

unzip -d target target/quake-net-cloudformation-$version.zip

aws --profile mggdev s3 sync target/quake-net-cloudformation-$version s3://$bucket_name/