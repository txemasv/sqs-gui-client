package org.txema.aws;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {
    private static SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss");

    private static String info = "";

    public static void sendMessage(String queue, String body) {
        info = "\n" + "-> Sent at " + dateFormatter.format(new Date()) +
                "\n" + "to: " + queue +
                "\n" + "body: " + body + "\n";
        print(info);
    }

    public static void receiveMessage(String queue, String receiptHandle, String body) {
        info =  "\n" + "<- Received at " + dateFormatter.format(new Date()) +
                "\n" + "from: " + queue +
                "\n" + "receiptHandle: " + receiptHandle +
                "\n" + "body: " + body + "\n";
        print(info);
    }

    public static void deleteMessage(String queue, String receiptHandle) {
        info =  "\n" + "Message deleted" +
                "\n" + "from: " + queue +
                " (" + dateFormatter.format(new Date()) + ")" +
                "\n" + "receiptHandle" + ":\n" + receiptHandle + "\n";
        print(info);
    }

    public static void getQueue(String name) {
        info =  "\nGet queue '" + name + "'." + "\n";
        print(info);
    }

    public static void queueLoaded(String url) {
        info = "\nQueueUrl = '" + url + "'." + "\n";
        print(info);
    }

    public static void queuesList(String urls) {
        info =  "\nList of queues: \n\n" + urls + "\n";
        print(info);
    }

    public static void deleteQueue(String name) {
        info = "\nQueue '" + name + "' deleted" + "\n";
        print(info);
    }

    public static void purgeQueue(String name) {
        info = "\nQueue '" + name + "' purged" + "\n";
        print(info);
    }

    public static void timeout(String name, Integer timeSeconds) {
        info = "\nTimeout is now " + timeSeconds + "s for queue '" + name + "'." + "\n";
        print(info);
    }

    public static void exception(String message) {
        info = "\n" + message + "\n";
        print(info);
    }

    public static void empty() {
        info = "\n" + "{empty}" +
                " (" + dateFormatter.format(new Date()) + ")" + "\n";
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
