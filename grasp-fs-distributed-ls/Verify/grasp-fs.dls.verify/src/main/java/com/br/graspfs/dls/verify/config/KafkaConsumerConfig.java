package com.br.graspfs.dls.verify.config;

import com.br.graspfs.dls.verify.dto.DataSolution;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapserver;


    @Bean
    public ConsumerFactory<String, DataSolution> dataSolutionConsumerFactory() {
        JsonDeserializer<DataSolution> deserializer = new JsonDeserializer<>(DataSolution.class, false);
        deserializer.addTrustedPackages("*");
        deserializer.setRemoveTypeHeaders(true); // <--- IGNORA O __TypeId__

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapserver);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializer);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "myGroup");

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    @Bean(name = "bestSolutionListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, DataSolution> bestSolutionListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, DataSolution> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(dataSolutionConsumerFactory());
        return factory;
    }

    @Bean(name = "solutionListenerContainerFactory") 
    public ConcurrentKafkaListenerContainerFactory<String, DataSolution> solutionListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, DataSolution> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(dataSolutionConsumerFactory());
        return factory;
    }

}
