#!/bin/bash
set -x
AWSCLI=${AWSCLI:-awslocal}
REGION=${REGION:-ap-northeast-1}

TABLE_NAME=todo

#$AWSCLI dynamodb create-table \
#  --region $REGION \
#  --billing-mode PAY_PER_REQUEST \
#  --table-name $TABLE_NAME \
#  --attribute-definitions \
#    AttributeName=PK,AttributeType=S \
#  --key-schema \
#    AttributeName=PK,KeyType=HASH

# For multi users

$AWSCLI dynamodb create-table \
  --region $REGION \
  --billing-mode PAY_PER_REQUEST \
  --table-name $TABLE_NAME \
  --attribute-definitions \
    AttributeName=PK,AttributeType=S \
    AttributeName=SK,AttributeType=S \
  --key-schema \
    AttributeName=PK,KeyType=HASH \
    AttributeName=SK,KeyType=RANGE
