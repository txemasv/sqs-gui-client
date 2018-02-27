package org.txema.aws;

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
        String awsQueueUrl = createQueue(fromUrl(queueUrl));
        for (String message: messages) {
            sqs.sendMessage(new SendMessageRequest(awsQueueUrl, message).withDelaySeconds(delaySeconds));
        }
    }

    @Override
    public List<MessageOutput> receiveMessage(String queueUrl) {
        String awsQueueUrl = createQueue(fromUrl(queueUrl));
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(awsQueueUrl);
        List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
        List<MessageOutput> messagesOutput = new ArrayList<>();
        for (Message m: messages) {
            messagesOutput.add(new MessageOutput(m.getReceiptHandle(), m.getBody()));
        }
        return messagesOutput;
    }

    @Override
    public void deleteMessage(String queueUrl, String receiptHandle) {
        String queueName = fromUrl(queueUrl);
        String awsQueueUrl = createQueue(queueName);
        sqs.deleteMessage(new DeleteMessageRequest(awsQueueUrl, receiptHandle));
    }

    @Override
    public void setVisibilityTimeout(String queueUrl, Integer timeSeconds) {
        String queueName = fromUrl(queueUrl);
        String awsQueueUrl = createQueue(queueName);
        Map<String, String> attributes = new HashMap<>();
        attributes.put("VisibilityTimeout", timeSeconds.toString());
        sqs.setQueueAttributes(new SetQueueAttributesRequest(awsQueueUrl, attributes));
    }

    @Override
    public String createQueue(String queueName) {
        CreateQueueRequest createQueueRequest = new CreateQueueRequest(queueName);
        CreateQueueResult response = sqs.createQueue(createQueueRequest);
        return response.getQueueUrl();
    }

    @Override
    public void purgeQueue(String queueUrl) {
        String queueName = fromUrl(queueUrl);
        String awsQueueUrl = createQueue(queueName);
        sqs.purgeQueue(new PurgeQueueRequest(awsQueueUrl));
    }

    @Override
    public void deleteQueue(String queueUrl) {
        String awsQueueUrl = createQueue(fromUrl(queueUrl));
        sqs.deleteQueue(new DeleteQueueRequest(awsQueueUrl));
    }

    private String fromUrl(String queueUrl) {
        String queueName;
        String[] parts = queueUrl.split("/");
        queueName = parts[parts.length - 1];
        return queueName;
    }
}
