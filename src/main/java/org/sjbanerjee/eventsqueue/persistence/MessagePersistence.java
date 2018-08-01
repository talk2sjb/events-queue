package org.sjbanerjee.eventsqueue.persistence;

import org.sjbanerjee.eventsqueue.model.EventMessage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class MessagePersistence {
    private static final String PATH = "C://Users/sb329e/files";
    private static final int PARTITIONS_PER_TOPIC = 5;
    private static MessagePersistence _instance = null;
    private static Map<String, Partition> partitionMap = new HashMap<>();

    private MessagePersistence() {
        //When the class loads, load all the partitions that are present in the broker
        traverseFileSystemForTopics();
    }

    public static synchronized MessagePersistence getInstance() {
        if (_instance == null) {
            _instance = new MessagePersistence();
        }
        return _instance;
    }

    public void writeSerializedObject(EventMessage message) throws IOException {
        System.out.println("Received: " + message.getMessageTopic() + " : " + message.getMessage());
        String messageFileParentPath = new StringBuilder(PATH)
                .append(File.separator)
                .append(message.getMessageTopic())
                .toString();

        //Determine Partition Key
        String topic = message.getMessageTopic();
        String partitionKey = topic + "_partition0";
        message.setPartitionKey(partitionKey);

        //Create partition per topic if not present
        createPartitionIfNotPresent(message, messageFileParentPath);

        //Segment file to use
        String messageFilePath = new StringBuilder(messageFileParentPath)
                .append(File.separator)
                .append(getPartitionToWrite(partitionKey).getName())
                .toString();
        String fileName = getLastSegmentFile(messageFilePath);
        File file = new File(fileName);

        if (!file.exists()) {
            Files.createFile(file.toPath());
        }

        writeToSegmentFile(message, file);
    }

    private void createPartitionIfNotPresent(EventMessage message, String messageFileParentPath) throws IOException {
        //Create the directory before looking for the segment file
        for (int i = 0; i < PARTITIONS_PER_TOPIC; i++) {
            String partionKey = new StringBuilder()
                    .append(message.getMessageTopic())
                    .append("_partition")
                    .append(i)
                    .toString();

            String partitionPath = new StringBuilder(messageFileParentPath)
                    .append(File.separator)
                    .append(partionKey)
                    .toString();

            Path parentDirectory = Paths.get(partitionPath);

            if (!Files.exists(parentDirectory)) {
                Files.createDirectories(parentDirectory);
                partitionMap.put(message.getPartitionKey(), new Partition(partionKey));
            }
        }
    }

    private void writeToSegmentFile(EventMessage message, File file) {
        try (FileWriter fw = new FileWriter(file.getAbsolutePath(), true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)
        ) {
            String messageString = getMessageToWrite(message);
            out.println(messageString);
        } catch (IOException e) {
            System.out.println("Exception while persisting data. Please retry!");
            e.printStackTrace();
        }
    }

    private String getMessageToWrite(EventMessage message) {
        StringBuilder stringBuilder = new StringBuilder();
        Partition partitionToWrite = getPartitionToWrite(message.getPartitionKey());
        partitionToWrite.setOffset(partitionToWrite.getOffset() + 1);
        stringBuilder.append(partitionToWrite.getOffset())
                .append(" ")
                .append(0)//TODO - get position
                .append(" ")
                .append(message.getMessage())
                .append("\n");

        return stringBuilder.toString();
    }

    private String getLastSegmentFile(String path) {
        Path dir = Paths.get(path);

        try (Stream<Path> files = Files.list(dir)) {
            // here we get the stream with full directory listing
            return files
                    // exclude subdirectories from listing
                    .filter(f -> !Files.isDirectory(f))
                    // finally get the last file using simple comparator by lastModified field
                    .max(Comparator.comparingLong(f -> f.toFile().lastModified()))
                    // If any files exist
                    .filter(filePath -> Files.exists(filePath))
                    // Otherwise default file
                    .orElse(Paths.get(path + File.separator + "0"))
                    .toString();
        } catch (IOException e) {
            System.out.println("Couldn't get segment files");
            e.printStackTrace();
        }

        return Paths.get(path + File.separator + "0").toString();
    }

    private String getSegmentName(String filePath) {
        return Paths.get(filePath).getFileName().toString();
    }

    private static Partition getPartitionToWrite(String partitionKey) {
        //TODO - partitioning logic
        return partitionMap.get(partitionKey);
    }

    private static void findPartitionsAndLoad(Path topic) {
        try (Stream<Path> path = Files.list(topic)) {
            path.forEach(partition -> {
                if (Files.isDirectory(partition)) {
                    //This is a partition
                    String partitionKey = partition.getFileName().toString();
                    partitionMap.put(partitionKey, new Partition(partitionKey));
                }
            });
        } catch (IOException e) {
            System.out.println("Failed ot load partiotion for topic : " + topic.toString());
        }
    }

    private void traverseFileSystemForTopics() {
        try (Stream<Path> paths = Files.list(Paths.get(PATH))) {
            paths.forEach(topic -> {
                if (Files.isDirectory(topic)) {
                    //this is a topic
                    findPartitionsAndLoad(topic);
                }
            });
        } catch (IOException e) {
            System.out.println("Error traversing file system for topics!");
        }
    }
}
