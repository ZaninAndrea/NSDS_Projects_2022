package org.polimi.nsds.project5.User;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class UserSerializer implements Serializer<User> {
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public byte[] serialize(String topic, User data) {
        try {
            if (data == null){
                return null;
            }
            return SerializationUtils.serialize(data);
        } catch (Exception e) {
            throw new SerializationException("Error when serializing User to byte[]");
        }
    }

    @Override
    public void close() {
    }
}
