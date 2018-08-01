package org.sjbanerjee.eventsqueue.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class EventMessage implements Serializable {
    @JsonProperty(required = true)
    private String message;

    @JsonProperty(required = true)
    private String messageTopic;

    private String partitionKey;

    public String getMessageTopic() {
        return messageTopic;
    }

    public void setMessageTopic(String messageTopic) {
        this.messageTopic = messageTopic;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPartitionKey() {
        return partitionKey;
    }

    public void setPartitionKey(String partitionKey) {
        this.partitionKey = partitionKey;
    }

    @Override
    public String toString() {
        return "EventMessage{" +
                "message='" + message + '\'' +
                ", messageTopic='" + messageTopic + '\'' +
                ", partitionKey='" + partitionKey + '\'' +
                '}';
    }
}
