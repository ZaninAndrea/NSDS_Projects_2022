package org.polimi.nsds.project5.ValidatedOrder;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class ValidatedOrderSerializer implements Serializer<ValidatedOrder> {
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public byte[] serialize(String topic, ValidatedOrder data) {
        try {
            if (data == null){
                return null;
            }
            return SerializationUtils.serialize(data);
        } catch (Exception e) {
            throw new SerializationException("Error when serializing ValidatedOrder to byte[]");
        }
    }

    @Override
    public void close() {
    }
}
