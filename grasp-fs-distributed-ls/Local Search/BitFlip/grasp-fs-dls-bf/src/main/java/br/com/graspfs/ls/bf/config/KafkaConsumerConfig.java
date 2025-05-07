package br.com.graspfs.ls.bf.config;

import br.com.graspfs.ls.bf.dto.DataSolution;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.converter.ByteArrayJsonMessageConverter;
import org.springframework.kafka.support.converter.JsonMessageConverter;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapserver;

    @Bean
    public ConsumerFactory consumerConfig() {
        Map<String, Object> properties = new HashMap<>();
         properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapserver);
         properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
         properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(properties, new StringDeserializer(),new JsonDeserializer<>(DataSolution.class)
                .trustedPackages("*")
                .forKeys());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory jsonKafkaListenerContainer(){
        var containerFactory = new ConcurrentKafkaListenerContainerFactory();
        containerFactory.setConsumerFactory(consumerConfig());
        containerFactory.setMessageConverter(new ByteArrayJsonMessageConverter());
        return containerFactory;
    }

}
