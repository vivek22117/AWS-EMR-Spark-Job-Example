package com.dd.rsvp.processor.job.utility;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import org.apache.log4j.Logger;

import java.io.Serializable;

public class AWSClientUtil implements Serializable {

    private static Logger logger = Logger.getLogger(AWSClientUtil.class);

    private static AWSCredentialsProvider awsCredentialsProvider;

    public static AWSKMS getKMSClient() {
        try {
            awsCredentialsProvider = getAWSCredentialProvider();
            return AWSKMSClientBuilder.standard()
                    .withCredentials(awsCredentialsProvider)
                    .withRegion(Regions.US_EAST_1).build();

        } catch (Exception ex) {
            logger.error("Exception Occurred while creating KMS client" + ex.getMessage(), ex);
        }
        return null;
    }

    public static AmazonS3 getS3Client() {
        try {
            awsCredentialsProvider = getAWSCredentialProvider();
            return AmazonS3ClientBuilder.standard()
                    .withCredentials(awsCredentialsProvider)
                    .withRegion(Regions.US_EAST_1).build();

        } catch (Exception ex) {
            logger.error("Exception Occurred while creating s3 client" + ex.getMessage(), ex);
        }
        return null;
    }

    public static AmazonDynamoDB getDynamoDBClient() {
        try {
            awsCredentialsProvider = getAWSCredentialProvider();
//            return new SerializableDynamoDBClient(awsCredentialsProvider);
            return AmazonDynamoDBClientBuilder.standard()
                    .withCredentials(awsCredentialsProvider)
                    .withRegion(Regions.US_EAST_1).build();

        } catch (Exception ex) {
            logger.error("Exception Occurred while creating dynamoDB client" + ex.getMessage(), ex);
        }
        return null;
    }

    public static AmazonSQS getSQSClient() {
        try {
            awsCredentialsProvider = getAWSCredentialProvider();
            return AmazonSQSClientBuilder.standard()
                    .withCredentials(getAWSCredentialProvider())
                    .withRegion(Regions.US_EAST_1)
                    .build();
        } catch (Exception ex) {
            logger.error("Exception Occurred while create SQS client" + ex.getMessage(), ex);
        }
        return null;
    }

    private static AWSCredentialsProvider getAWSCredentialProvider() {

        if (awsCredentialsProvider == null) {
            boolean isRunningFromCI = Boolean.parseBoolean(PropertyLoader.getInstance().getPropertyValue("isRunningFromCI"));
            if (isRunningFromCI) {
                awsCredentialsProvider = new InstanceProfileCredentialsProvider(false);
            } else {
                awsCredentialsProvider = new ProfileCredentialsProvider("prod");
            }
        }
        return awsCredentialsProvider;
    }
}
