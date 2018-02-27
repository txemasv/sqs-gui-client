package org.txema.aws;

import java.util.List;

public interface SqsClient {
    /**
     * Send a message/s onto a queue.
     *
     * @param queueUrl:     the url of the queue.
     * @param delaySeconds: the time in seconds to wait before having active the message in the queue.
     * @param messages:     the message or array-messages to push in the queue.
     */
    void sendMessage (String queueUrl, Integer delaySeconds, String... messages);

    /**
     * Receives a message / list_of_messages from a queue.
     *
     * @param queueUrl: the url of the queue.
     * @return a message from the queue.
     */
    List<MessageOutput>  receiveMessage(String queueUrl);

    /**
     * Deletes a message from the queue that was received by receiveMessage.
     *
     * @param queueUrl:      the url of the queue.
     * @param receiptHandle: the identifier associated with the action of receiving the message by receiveMessage.
     */
    void deleteMessage(String queueUrl, String receiptHandle);

    /**
     * Change the visibility timeout for a single queue.
     *
     * @param queueUrl:    the url of the queue.
     * @param timeSeconds: the new value of visibility timeout (in seconds)
     */
    void setVisibilityTimeout(String queueUrl, Integer timeSeconds);

    /**
     * Creates a new queue or gets one existent if already exists specified by the queueName parameter.
     *
     * @param queueName: the name of the queue
     */
    String createQueue(String queueName);

    /**
     * Deletes the messages in a queue specified by the queueURL parameter.
     *
     * @param queueUrl: the queueUrl of the queue
     */
    void purgeQueue(String queueUrl);

    /**
     * Deletes the queue specified by the queueURL parameter.
     *
     * @param queueUrl: the queueUrl of the queue
     */
    void deleteQueue(String queueUrl);
}
