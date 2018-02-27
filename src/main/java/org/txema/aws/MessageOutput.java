package org.txema.aws;


/**
 * A {@code MessageOutput} represents a message when is pulled.
 */
public class MessageOutput {
    private String message;
    private String receiptHandle;

    public MessageOutput(String receiptHandle, String message) {
        this.message = message;
        this.receiptHandle = receiptHandle;
    }

    public String getMessage() {
        return message;
    }

    public String getReceiptHandle() {
        return receiptHandle;
    }

    public String toJSON() {
        return "{\"" + receiptHandle + "\":\"" + message + "\"}";
    }
}

