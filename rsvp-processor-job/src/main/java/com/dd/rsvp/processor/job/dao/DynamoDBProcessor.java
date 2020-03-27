package com.dd.rsvp.processor.job.dao;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.util.CollectionUtils;
import com.dd.rsvp.processor.job.exception.ApplicationException;
import com.dd.rsvp.processor.job.utility.AWSClientUtil;
import com.dd.rsvp.processor.job.utility.JobStatusEnum;
import com.dd.rsvp.processor.job.utility.PropertyLoader;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.dd.rsvp.processor.job.utility.ApplicationEnum.*;


public class DynamoDBProcessor implements Serializable {
    private Logger logger = Logger.getLogger(DynamoDBProcessor.class);

    private static String dbTableName = PropertyLoader.getInstance().getPropertyValue("dynamoDB.table.name");
    private AmazonDynamoDB amazonDynamoDB;

    public DynamoDBProcessor() {
        amazonDynamoDB = AWSClientUtil.getDynamoDBClient();
    }

    public List<String> getProcessIdAndS3Key() {
        List<String> record = new ArrayList<>();

        Map<String, String> attributeNames = new HashMap<String, String>();
        attributeNames.put("#verifiedStatus", VERIFIED_STATUS.getMessage());
        attributeNames.put("#migratedStatus", MIGRATED_STATUS.getMessage());

        Map<String, AttributeValue> attributeValues = new HashMap<String, AttributeValue>();
        attributeValues.put(":verifiedValue", new AttributeValue().withS(JobStatusEnum.COMPLETED.toString()));
        attributeValues.put(":migrateValue", new AttributeValue().withS(JobStatusEnum.PENDING.toString()));

        try {
            ScanRequest scanRequest = new ScanRequest()
                    .withTableName(dbTableName)
                    .withConsistentRead(false)
                    .withFilterExpression("#verifiedStatus = :verifiedValue AND #migratedStatus = :migrateValue")
                    .withExpressionAttributeNames(attributeNames)
                    .withExpressionAttributeValues(attributeValues);

            ScanResult scanResult = amazonDynamoDB.scan(scanRequest);

            if (scanResult.getCount() > 0) {
                Map<String, AttributeValue> item = scanResult.getItems().get(0);
                AttributeValue jobIdAttribute =
                        item.getOrDefault(PROCESS_ID.getMessage(), new AttributeValue());
                AttributeValue s3KeyAttribute =
                        item.getOrDefault(S3_DATA_KEY.getMessage(), new AttributeValue());

                record.add(jobIdAttribute.getS());
                record.add(s3KeyAttribute.getS());
            }

            if (!CollectionUtils.isNullOrEmpty(record)) {
                updateDynamoDBItem(record.get(0), JobStatusEnum.RUNNING, null, null, null);
            }
            return record;
        } catch (Exception ex) {
            throw new ApplicationException("Unable to fetch or updated item in dynamoDB for migration step", ex);
        }
    }

    public void updateDynamoDBItem(String jobId, JobStatusEnum status, String recordCount, String dbRecordCount,
                                   String s3KeysCount) {
        if (recordCount != null && dbRecordCount != null) {
            updateDynamoDBItemWithCountAndStatus(jobId, status.toString(), recordCount, dbRecordCount, s3KeysCount);
        } else {
            updateDynamoDBItemJobWithStatus(jobId, status.toString());
        }
    }

    private void updateDynamoDBItemJobWithStatus(String jobId, String status) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put(PROCESS_ID.getMessage(), new AttributeValue().withS(jobId));

        Map<String, AttributeValue> expressionAttributeValues = new HashMap<String, AttributeValue>();
        expressionAttributeValues.put(":migratedStatus", new AttributeValue().withS(status));

        try {
            UpdateItemRequest updateItemRequest = new UpdateItemRequest()
                    .withTableName(dbTableName)
                    .withKey(key)
                    .withUpdateExpression("set MigratedStatus=:migratedStatus")
                    .withExpressionAttributeValues(expressionAttributeValues);

            UpdateItemResult updateItemResult = amazonDynamoDB.updateItem(updateItemRequest);
        } catch (Exception ex) {
            throw new ApplicationException("Unable to update dynamoDB count for migrated csv ", ex);
        }
    }

    private void updateDynamoDBItemWithCountAndStatus(String jobId, String status, String recordCount,
                                                      String dbRecordCount, String s3KeysCount) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put(PROCESS_ID.getMessage(), new AttributeValue().withS(jobId));

        Map<String, AttributeValue> expressionAttributeValues = new HashMap<String, AttributeValue>();
        expressionAttributeValues.put(":migratedStatus", new AttributeValue().withS(status));
        expressionAttributeValues.put(":migratedCount", new AttributeValue().withN(recordCount));
        expressionAttributeValues.put(":dynamoDBCount", new AttributeValue().withN(dbRecordCount));
        expressionAttributeValues.put(":s3KeysCount", new AttributeValue().withN(s3KeysCount));

        try {
            UpdateItemRequest updateItemRequest = new UpdateItemRequest()
                    .withTableName(dbTableName)
                    .withKey(key)
                    .withUpdateExpression("ADD MigratedCount :migratedCount "
                            + "SET MigratedStatus=:migratedStatus, DynamoDBCount=:dynamoDBCount, MigratedS3KeysCount=:s3KeysCount")
                    .withExpressionAttributeValues(expressionAttributeValues);

            UpdateItemResult updateItemResult = amazonDynamoDB.updateItem(updateItemRequest);
        } catch (Exception ex) {
            throw new ApplicationException("Unable to update dynamoDB count for migrated csv ", ex);
        }
    }
}
