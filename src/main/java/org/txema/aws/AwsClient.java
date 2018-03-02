package org.txema.aws;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AwsClient implements SqsClient {

    private final AmazonSQS sqs;  //d.i.

    public AwsClient(AmazonSQSClient client) {
        this.sqs = client;
    }

    @Override
    public void sendMessage(String queueUrl, Integer delaySeconds, String... messages) {
        try {
            String awsQueueUrl = getQueue(fromUrl(queueUrl));
            for (String message : messages) {
                sqs.sendMessage(new SendMessageRequest(awsQueueUrl, message).withDelaySeconds(delaySeconds));
                Log.sendMessage(awsQueueUrl, message);
            }
        } catch (AmazonServiceException ex) {
            Log.exception(ex.getErrorMessage());
        }
    }

    @Override
    public List<MessageOutput> receiveMessage(String queueUrl) {
        List<MessageOutput> messagesOutput = new ArrayList<>();
        try {
            String awsQueueUrl = getQueue(fromUrl(queueUrl));
            ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(awsQueueUrl);
            List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
            if (messages.isEmpty()) Log.empty();
            for (Message m : messages) {
                messagesOutput.add(new MessageOutput(m.getReceiptHandle(), m.getBody()));
                Log.receiveMessage(awsQueueUrl, m.getReceiptHandle(), m.getBody());
            }
        } catch (AmazonServiceException ex) {
            Log.exception(ex.getErrorMessage());
        }
        return messagesOutput;
    }

    @Override
    public void deleteMessage(String queueUrl, String receiptHandle) {
        try {
            String awsQueueUrl = getQueue(fromUrl(queueUrl));
            sqs.deleteMessage(new DeleteMessageRequest(awsQueueUrl, receiptHandle));
            Log.deleteMessage(queueUrl, receiptHandle);
        } catch (AmazonServiceException ex) {
            Log.exception(ex.getErrorMessage());
        }
    }

    @Override
    public void setVisibilityTimeout(String queueUrl, Integer timeSeconds) {
        try {
            String awsQueueUrl = getQueue(fromUrl(queueUrl));
            Map<String, String> attributes = new HashMap<>();
            attributes.put("VisibilityTimeout", timeSeconds.toString());
            sqs.setQueueAttributes(new SetQueueAttributesRequest(awsQueueUrl, attributes));
            Log.timeout(fromUrl(queueUrl), timeSeconds);
        } catch (AmazonServiceException ex) {
            Log.exception(ex.getErrorMessage());
        }
    }

    @Override
    public String getVisibilityTimeout(String queueUrl) {
        String timeout = null;
        try {
            String awsQueueUrl = getQueue(fromUrl(queueUrl));
            List<String> attributeNames = new ArrayList<>();
            attributeNames.add("VisibilityTimeout");
            GetQueueAttributesResult result = sqs.getQueueAttributes(new GetQueueAttributesRequest(awsQueueUrl, attributeNames));
            timeout = result.getAttributes().get("VisibilityTimeout");
        } catch (AmazonServiceException ex) {
            Log.exception(ex.getErrorMessage());
        }
        return timeout;
    }

    @Override
    public String createQueue(String queueName) {
        String awsQueueUrl = null;
        Log.getQueue(queueName);
        try {
            CreateQueueRequest createQueueRequest = new CreateQueueRequest(fromUrl(queueName));
            CreateQueueResult response = sqs.createQueue(createQueueRequest);
            awsQueueUrl = response.getQueueUrl();
            Log.queueLoaded(awsQueueUrl);
        } catch (AmazonServiceException ex) {
            Log.exception(ex.getErrorMessage());
        }
        return awsQueueUrl;
    }

    private String getQueue(String queueName) {
        String awsQueueUrl = null;
        try {
            CreateQueueRequest createQueueRequest = new CreateQueueRequest(fromUrl(queueName));
            CreateQueueResult response = sqs.createQueue(createQueueRequest);
            awsQueueUrl = response.getQueueUrl();
        } catch (AmazonServiceException ex) {
            System.out.println(ex.getErrorMessage());
        }
        return awsQueueUrl;
    }

    @Override
    public void purgeQueue(String queueUrl) {
        try {
            String awsQueueUrl = getQueue(fromUrl(queueUrl));
            sqs.purgeQueue(new PurgeQueueRequest(awsQueueUrl));
            Log.purgeQueue(fromUrl(queueUrl));
        } catch (AmazonServiceException ex) {
            Log.exception(ex.getErrorMessage());
        }
    }

    @Override
    public void deleteQueue(String queueUrl) {
        try {
            String awsQueueUrl = getQueue(fromUrl(queueUrl));
            sqs.deleteQueue(new DeleteQueueRequest(awsQueueUrl));
            Log.deleteQueue(fromUrl(queueUrl));
        } catch (AmazonServiceException ex) {
            Log.exception(ex.getErrorMessage());
        }
    }

    @Override
    public void listQueues() {
        try {
            String queuesList = "";
            for (final String queueUrl : sqs.listQueues().getQueueUrls()) {
                queuesList = queuesList.concat(queueUrl).concat("\n");
            }
            Log.queuesList(queuesList);
        } catch (AmazonServiceException ex) {
            Log.exception(ex.getErrorMessage());
        }
    }

    @Override
    public boolean testCredentials(String accessKey, String secretKey) {
        try {
            AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
            AmazonSQSClient client = new AmazonSQSClient(awsCredentials);
            client.listQueues();
            return true;
        } catch (AmazonServiceException ex) {
            Log.exception(ex.getErrorMessage() + "\nCredentials will not be saved.");
        } catch (Exception ex) {
            Log.exception(ex.getMessage());
        }
        return false;
    }

    private String fromUrl(String queueUrl) {
        String queueName;
        String[] parts = queueUrl.split("/");
        queueName = parts[parts.length - 1];
        return queueName;
    }
}
