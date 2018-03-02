package org.txema.aws;

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

    private static SqsClient sqsClient;
    private static final ApplicationContext singleton = new ApplicationContext();

    private ApplicationContext() {
        instanceSqsClient();
    }

    public static ApplicationContext getInstance() {
        return singleton;
    }

    private void instanceSqsClient() {
        String accessKey = getProperties().getProperty("accessKey", "");
        String secretKey = getProperties().getProperty("secretKey", "");
        AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
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

    private static void setCredentials(String accessKey, String secretKey) {
        Properties prop = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        File file = new File(loader.getResource("credentials.properties").getPath());
        try (OutputStream output = new FileOutputStream(file)) {
            // set the properties value
            prop.setProperty("accessKey", accessKey);
            prop.setProperty("secretKey", secretKey);

            // save properties to project root folder
            prop.store(output, null);

        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public static void setCredentials(Credentials credentials) {
        setCredentials(credentials.getAccessKey(), credentials.getSecretKey());
    }

    public static Credentials getCredentials() {
        return new Credentials(
                getProperties().getProperty("accessKey"),
                getProperties().getProperty("secretKey"));
    }

    public SqsClient renewCredentials(Credentials credentials) {
        setCredentials(credentials);
        instanceSqsClient();
        return getSqsClient();
    }
}