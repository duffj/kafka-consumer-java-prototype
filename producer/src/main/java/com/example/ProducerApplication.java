package com.example;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ProducerApplication {

    private static final String DEFAULT_TOPIC = "transactions";

    private static final String SYSTEM_PROPERTY_BOOTSTRAP_SERVERS = "bootstrap.servers";
    private static final String SYSTEM_PROPERTY_SCHEMA_REGISTRY_URL_CONFIG =
            AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
    private static final String SYSTEM_PROPERTY_TOPIC = "topic";

    public static void main(String[] args) {
        final String topic = System.getProperty(SYSTEM_PROPERTY_TOPIC, DEFAULT_TOPIC);
        Properties props = new Properties();
        props.put("bootstrap.servers", System.getProperty(SYSTEM_PROPERTY_BOOTSTRAP_SERVERS, "localhost:9092"));
        props.put("acks", "all");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
                System.getProperty(SYSTEM_PROPERTY_SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081")
        );
        System.out.println(props);

        KafkaProducer<String, Payment> paymentProducer = new KafkaProducer<>(props);
        final String orderId = "1";
        final Payment payment1 = new Payment(orderId, 1.00);
        final ProducerRecord<String, Payment> record1 = new ProducerRecord<>(topic, payment1.getId().toString(), payment1);

        try {
            paymentProducer.send(record1);
            System.out.println("Message successfully sent.");
        } catch(SerializationException e) {
            e.printStackTrace();
        }
        // When you're finished producing records, you can flush the producer to ensure it has all been written to Kafka and
        // then close the producer to free its resources.
        finally {
            paymentProducer.flush();
            paymentProducer.close();
        }
    }
}
