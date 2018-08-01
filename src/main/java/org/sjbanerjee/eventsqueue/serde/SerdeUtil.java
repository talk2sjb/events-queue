package org.sjbanerjee.eventsqueue.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.sjbanerjee.eventsqueue.model.EventMessage;

import java.io.IOException;

public final class SerdeUtil {
    private static final ObjectMapper mapper = new ObjectMapper();

    private SerdeUtil() {
    }

    public static EventMessage getDeserializedObject(String jsonString) throws IOException {
        return mapper.readValue(jsonString, EventMessage.class);
    }
}