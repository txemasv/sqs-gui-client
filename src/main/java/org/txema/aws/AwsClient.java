package org.txema.aws;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AwsClient implements SqsClient {

    private final AmazonSQS client;  //d.i.

    public AwsClient(AmazonSQSClient client) {
        this.client = client;
    }

    @Override
    public void sendMessage(String queueUrl, Integer delaySeconds, String... messages) {
        String awsQueueUrl = createQueue(fromUrl(queueUrl));
        for (String message: messages) {
            client.sendMessage(new SendMessageRequest(awsQueueUrl, message).withDelaySeconds(delaySeconds));
        }
    }

    @Override
    public List<MessageOutput> receiveMessage(String queueUrl) {
        String awsQueueUrl = createQueue(fromUrl(queueUrl));
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(awsQueueUrl);
        List<Message> messages = client.receiveMessage(receiveMessageRequest).getMessages();
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
        client.deleteMessage(new DeleteMessageRequest(awsQueueUrl, receiptHandle));
    }

    @Override
    public void setVisibilityTimeout(String queueUrl, Integer timeSeconds) {
        String queueName = fromUrl(queueUrl);
        String awsQueueUrl = createQueue(queueName);
        Map<String, String> attributes = new HashMap<>();
        attributes.put("VisibilityTimeout", timeSeconds.toString());
        client.setQueueAttributes(new SetQueueAttributesRequest(awsQueueUrl, attributes));
    }

    @Override
    public String createQueue(String queueName) {
        CreateQueueRequest createQueueRequest = new CreateQueueRequest(queueName);
        CreateQueueResult response = client.createQueue(createQueueRequest);
        return response.getQueueUrl();
    }

    @Override
    public void purgeQueue(String queueUrl) {
        String queueName = fromUrl(queueUrl);
        String awsQueueUrl = createQueue(queueName);
        client.purgeQueue(new PurgeQueueRequest(awsQueueUrl));
    }

    @Override
    public void deleteQueue(String queueUrl) {
        String awsQueueUrl = createQueue(fromUrl(queueUrl));
        client.deleteQueue(new DeleteQueueRequest(awsQueueUrl));
    }

    private String fromUrl(String queueUrl) {
        String queueName;
        String[] parts = queueUrl.split("/");
        queueName = parts[parts.length - 1];
        return queueName;
    }
}
