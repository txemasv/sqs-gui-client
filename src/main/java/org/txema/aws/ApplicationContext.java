package org.txema.aws;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQSClient;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
        AmazonSQSClient client = new AmazonSQSClient(awsCredentials); //YOUR_CLIENT_TO_ACCESS_AWS
        sqsClient = new AwsClient(client);
    }

    public SqsClient getSqsClient() {
        return sqsClient;
    }

    private static Properties getProperties() {
        Properties prop = new Properties();
        InputStream input = null;
        try {
            input = new FileInputStream("src/main/resources/credentials.properties");
            // load a properties file
            prop.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return prop;
    }

}