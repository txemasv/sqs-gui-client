package org.txema.aws;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQSClient;

/**
 * A {@code ApplicationContext} represents the entry to the Application.
 * It selects the configuration and the implementation for SqsClient
 */
public class ApplicationContext {

    private static SqsClient sqsClient;
    private static final ApplicationContext singleton = new ApplicationContext();

    private ApplicationContext() {
        instanceSqsClient();
    }

    public static ApplicationContext getInstance() {
        return singleton;
    }

    private void instanceSqsClient() {
        /*
        YOUR_CLIENT_IMPLEMENTATION_TO_ACCESS_TO_AWS
        -------------------------------------------
        Example:
        AWSCredentials awsCredentials = new BasicAWSCredentials("YOUR_ACCESS_KEY", "YOUR_SECRET_KEY");
        AmazonSQSClient client = new AmazonSQSClient(awsCredentials);
        */
        AWSCredentials awsCredentials = new BasicAWSCredentials("AKIAJKGMFBCL7OGC3CEA", "0NQKjTDTYbXJqfKeazp08LAAeescR2pzzkXYpsAx");
        AmazonSQSClient client = new AmazonSQSClient(awsCredentials); //YOUR_CLIENT_TO_ACCESS_AWS
        sqsClient = new AwsClient(client);
    }

    public SqsClient getSqsClient() {
        return sqsClient;
    }

}