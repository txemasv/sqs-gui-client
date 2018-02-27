package org.txema.aws;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AwsClient implements SqsClient {

    private final AmazonSQS sqs;  //d.i.

    public AwsClient(AmazonSQSClient sqsClient) {
        this.sqs = sqsClient;
    }

    @Override
    public void sendMessage(String queueUrl, Integer delaySeconds, String... messages) {
        try {
            String awsQueueUrl = createQueue(fromUrl(queueUrl));
            for (String message : messages) {
                sqs.sendMessage(new SendMessageRequest(awsQueueUrl, message).withDelaySeconds(delaySeconds));
                Log.sendMessage(message);
            }
        } catch (AmazonServiceException ex) {
            Log.exception(ex.getErrorMessage());
        }
    }

    @Override
    public List<MessageOutput> receiveMessage(String queueUrl) {
        List<MessageOutput> messagesOutput = new ArrayList<>();
        try {
            String awsQueueUrl = createQueue(fromUrl(queueUrl));
            ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(awsQueueUrl);
            List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
            for (Message m : messages) {
                messagesOutput.add(new MessageOutput(m.getReceiptHandle(), m.getBody()));
                Log.receiveMessage(m.getReceiptHandle(), m.getBody());
            }
        } catch (AmazonServiceException ex) {
            Log.exception(ex.getErrorMessage());
        }
        return messagesOutput;
    }

    @Override
    public void deleteMessage(String queueUrl, String receiptHandle) {
        try {
            String awsQueueUrl = createQueue(fromUrl(queueUrl));
            sqs.deleteMessage(new DeleteMessageRequest(awsQueueUrl, receiptHandle));
            Log.deleteMessage(receiptHandle);
        } catch (AmazonServiceException ex) {
            Log.exception(ex.getErrorMessage());
        }
    }

    @Override
    public void setVisibilityTimeout(String queueUrl, Integer timeSeconds) {
        try {
            String awsQueueUrl = createQueue(fromUrl(queueUrl));
            Map<String, String> attributes = new HashMap<>();
            attributes.put("VisibilityTimeout", timeSeconds.toString());
            sqs.setQueueAttributes(new SetQueueAttributesRequest(awsQueueUrl, attributes));
            Log.timeout(fromUrl(queueUrl), timeSeconds);
        } catch (AmazonServiceException ex) {
            Log.exception(ex.getErrorMessage());
        }
    }

    @Override
    public String createQueue(String queueName) {
        String awsQueueUrl = null;
        Log.getQueue(queueName);
        try {
            CreateQueueRequest createQueueRequest = new CreateQueueRequest(queueName);
            CreateQueueResult response = sqs.createQueue(createQueueRequest);
            awsQueueUrl = response.getQueueUrl();
            Log.queueLoaded(awsQueueUrl);
        } catch (AmazonServiceException ex) {
            Log.exception(ex.getErrorMessage());
        }
        return awsQueueUrl;
    }

    @Override
    public void purgeQueue(String queueUrl) {
        try {
            String awsQueueUrl = createQueue(fromUrl(queueUrl));
            sqs.purgeQueue(new PurgeQueueRequest(awsQueueUrl));
            Log.purgeQueue(fromUrl(queueUrl));
        } catch (AmazonServiceException ex) {
            Log.exception(ex.getErrorMessage());
        }
    }

    @Override
    public void deleteQueue(String queueUrl) {
        try {
            String awsQueueUrl = createQueue(fromUrl(queueUrl));
            sqs.deleteQueue(new DeleteQueueRequest(awsQueueUrl));
            Log.deleteQueue(fromUrl(queueUrl));
        } catch (AmazonServiceException ex) {
            Log.exception(ex.getErrorMessage());
        }
    }

    @Override
    public void listQueues() {
        try {
            for (final String queueUrl : sqs.listQueues().getQueueUrls()) {
                Log.queueLoaded(queueUrl);
            }
        } catch (AmazonServiceException ex) {
            Log.exception(ex.getErrorMessage());
        }
    }

    private String fromUrl(String queueUrl) {
        String queueName;
        String[] parts = queueUrl.split("/");
        queueName = parts[parts.length - 1];
        return queueName;
    }
}
