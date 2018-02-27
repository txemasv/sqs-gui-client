package org.txema.aws;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {
    private static SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss");

    private static String info = "";

    public static void sendMessage(String body) {
        info = "\n" + "--> Send message" +
                " (" + dateFormatter.format(new Date()) + ")" +
                "\n" + "body" + ": " + body + "\n";
        print(info);
    }

    public static void receiveMessage(String receiptHandle, String body) {
        info = "\n" + "(-) Receive message" +
                " (" + dateFormatter.format(new Date()) + ")" +
                "\n" + "receiptHandle" + ": " + receiptHandle +
                "\n" + "body" + ": " + body + "\n";
        print(info);
    }

    public static void deleteMessage(String receiptHandle) {
        info = "\n" + "(x) Delete Message" +
                " (" + dateFormatter.format(new Date()) + ")" +
                "\n" + "receiptHandle" + ": " + receiptHandle + "\n";
        print(info);
    }

    public static void getQueue(String name) {
        info = "\nGet queue '" + name + "'.";
        print(info);
    }

    public static void queueLoaded(String url) {
        info = "\nQueueUrl = '" + url + "'.";
        print(info);
    }

    public static void deleteQueue(String name) {
        info = "\nDelete queue '" + name + "'.";
        print(info);
    }

    public static void purgeQueue(String name) {
        info = "\nPurge queue '" + name + "'.";
        print(info);
    }

    public static void timeout(String name, Integer timeSeconds) {
        info = "\nTimeout " + timeSeconds + "s for queue '" + name + "'.";
        print(info);
    }

    public static void exception(String message) {
        info = "\n" + message;
        print(info);
    }

    public static String getInfo() {
        String output = info;
        info = "";
        return output;
    }

    private static void print(String info) {
        System.out.println(info);
    }

}
