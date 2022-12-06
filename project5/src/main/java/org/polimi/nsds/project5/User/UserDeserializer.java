package org.polimi.nsds.project5.User;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class UserDeserializer implements Deserializer<User> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public User deserialize(String topic, byte[] data) {
        try {
            if (data == null){
                return null;
            }
            return SerializationUtils.deserialize(data);
        } catch (Exception e) {
            throw new SerializationException("Error when deserializing byte[] to User");
        }
    }

    @Override
    public void close() {
    }
}