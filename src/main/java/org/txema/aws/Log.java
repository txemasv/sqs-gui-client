package org.txema.aws;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {
    private static SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss");

    public static void sendMessage(String body) {
        System.out.println("\n" + "--> Send message" +
                " (" + dateFormatter.format(new Date()) + ")" +
                "\n" + "body" + ": " + body + "\n");
    }

    public static void receiveMessage(String receiptHandle, String body) {
        System.out.println("\n" + "(-) Receive message" +
                " (" + dateFormatter.format(new Date()) + ")" +
                "\n" + "receiptHandle" + ": " + receiptHandle +
                "\n" + "body" + ": " + body + "\n");
    }

    public static void deleteMessage(String receiptHandle) {
        System.out.println("\n" + "(x) Delete Message" +
                " (" + dateFormatter.format(new Date()) + ")" +
                "\n" + "receiptHandle" + ": " + receiptHandle + "\n");
    }

    public static void getQueue(String name) {
        System.out.println("\nGet queue '" + name + "'.");
    }

    public static void queueLoaded(String url) {
        System.out.println("QueueUrl = '" + url + "'.");
    }

    public static void deleteQueue(String name) {
        System.out.println("\nDelete queue '" + name + "'.");
    }

    public static void purgeQueue(String name) {
        System.out.println("\nPurge queue '" + name + "'.");
    }

    public static void timeout(String name, Integer timeSeconds) {
        System.out.println("\nTimeout " + timeSeconds + "s for queue '" + name + "'.");
    }
}
