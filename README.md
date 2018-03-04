# sqs-gui-client
Desktop Application to test and execute basic operations in your AWS/SQS queues easily.

![queues](/src/main/resources/screeens/queues.PNG?raw=true)


## Features
- Java8

## Run the project
You can run the project from 3 different ways:

### A) Using Maven
mvn package  
mvn exec:java -Dexec.mainClass="org.txema.aws.MainGUI"

### B) Using Java
mvn package  
java -jar target/sqs_client-1.0-SNAPSHOT-jar-with-dependencies.jar 

### C) Download executable
- Windows: [sqsclient.exe](https://www.dropbox.com/s/wr3ngk1ujh7qgj0/sqsclient.exe?dl=0)
- Linux: [sqsclient.run](https://www.dropbox.com/s/q3i0unbqqko4xdv/sqsclient.run?dl=0)
