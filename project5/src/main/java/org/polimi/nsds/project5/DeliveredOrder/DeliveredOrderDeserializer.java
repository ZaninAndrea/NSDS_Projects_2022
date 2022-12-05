package org.polimi.nsds.project5.DeliveredOrder;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class DeliveredOrderDeserializer implements Deserializer<DeliveredOrder> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public DeliveredOrder deserialize(String topic, byte[] data) {
        try {
            if (data == null){
                return null;
            }
            return SerializationUtils.deserialize(data);
        } catch (Exception e) {
            throw new SerializationException("Error when deserializing byte[] to DeliveredOrder");
        }
    }

    @Override
    public void close() {
    }
}