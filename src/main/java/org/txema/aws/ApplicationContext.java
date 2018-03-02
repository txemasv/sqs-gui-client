package org.txema.aws;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQSClient;

import java.io.*;
import java.util.Properties;

/**
 * A {@code ApplicationContext} represents the entry to the Application.
 * It selects the configuration and the implementation for SqsClient
 */
public class ApplicationContext {

    private static Credentials credentials;
    private static SqsClient sqsClient;
    private static final ApplicationContext singleton = new ApplicationContext();

    private ApplicationContext() {
        instanceSqsClient();
    }

    public static ApplicationContext getInstance() {
        return singleton;
    }

    private void instanceSqsClient() {
        credentials = getCredentialsFromFile();
        AWSCredentials awsCredentials = new BasicAWSCredentials(credentials.getAccessKey(), credentials.getSecretKey());
        AmazonSQSClient client = new AmazonSQSClient(awsCredentials);
        sqsClient = new AwsClient(client);
    }

    public SqsClient getSqsClient() {
        return sqsClient;
    }

    private static Properties getProperties() {
        Properties prop = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try(InputStream resourceStream = loader.getResourceAsStream("credentials.properties")) {
            prop.load(resourceStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prop;
    }

    private void setCredentials(String accessKey, String secretKey) {
        credentials.setAccessKey(accessKey);
        credentials.setSecretKey(secretKey);
    }

    public Credentials getCredentials() {
        return credentials;
    }

    private Credentials getCredentialsFromFile() {
        String accessKey = getProperties().getProperty("accessKey", "");
        String secretKey = getProperties().getProperty("secretKey", "");
        return new Credentials(accessKey, secretKey);
    }

    public boolean testCredentials(String accessKey, String secretKey) {
        try {
            AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
            AmazonSQSClient client = new AmazonSQSClient(awsCredentials);
            client.listQueues();
            return true;
        } catch (AmazonServiceException ex) {
            Log.exception(ex.getErrorMessage());
        } catch (Exception ex) {
            Log.exception(ex.getMessage());
        }
        return false;
    }

    public SqsClient renewSqsClient(String accessKey, String secretKey) {
        setCredentials(accessKey, secretKey);
        AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        AmazonSQSClient client = new AmazonSQSClient(awsCredentials);
        sqsClient = new AwsClient(client);
        return getSqsClient();
    }
}