package pension_management_system.pension.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * KafkaConfig - Configuration for Apache Kafka messaging
 *
 * Purpose: Set up Kafka for async event processing
 *
 * What is Kafka?
 * - Distributed event streaming platform
 * - Publish-subscribe messaging system
 * - High throughput, fault-tolerant
 * - Used for async processing, microservices communication
 *
 * Why use Kafka?
 * - Decouple services
 * - Handle peak loads
 * - Reliable message delivery
 * - Event sourcing and CQRS patterns
 *
 * Use cases in our system:
 * 1. Payment notifications → Email service
 * 2. Report generation requests → Background workers
 * 3. Benefit approvals → Notification service
 * 4. Audit logging → Analytics service
 *
 * Configuration required (application.properties):
 * spring.kafka.bootstrap-servers=localhost:9092
 * spring.kafka.consumer.group-id=pension-system
 * spring.kafka.consumer.auto-offset-reset=earliest
 *
 * Annotations:
 * @Configuration - Spring configuration class
 * @EnableKafka - Enable Kafka support
 */
@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:pension-system}")
    private String groupId;

    /**
     * KAFKA PRODUCER CONFIGURATION
     *
     * Producer sends messages to Kafka topics
     *
     * Key settings:
     * - bootstrap.servers: Kafka broker addresses
     * - key.serializer: How to serialize message keys
     * - value.serializer: How to serialize message values
     *
     * @return Producer configuration map
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> config = new HashMap<>();

        // Kafka broker address
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // Serialize key as string
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // Serialize value as JSON
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return new DefaultKafkaProducerFactory<>(config);
    }

    /**
     * KAFKA TEMPLATE
     *
     * High-level API for sending messages
     *
     * Usage:
     * kafkaTemplate.send("topic-name", "key", messageObject);
     *
     * @return Configured Kafka template
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * KAFKA CONSUMER CONFIGURATION
     *
     * Consumer reads messages from Kafka topics
     *
     * Key settings:
     * - bootstrap.servers: Kafka broker addresses
     * - group.id: Consumer group (for load balancing)
     * - auto.offset.reset: Start from earliest/latest message
     * - key.deserializer: How to deserialize keys
     * - value.deserializer: How to deserialize values
     *
     * @return Consumer configuration map
     */
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> config = new HashMap<>();

        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        // Trust all packages for JSON deserialization
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        return new DefaultKafkaConsumerFactory<>(config);
    }

    /**
     * KAFKA LISTENER CONTAINER FACTORY
     *
     * Factory for creating message listener containers
     *
     * Used by @KafkaListener annotations
     *
     * @return Configured listener container factory
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}

/**
 * KAFKA TOPICS
 *
 * Define topic names as constants for consistency
 */
class KafkaTopics {
    public static final String PAYMENT_EVENTS = "payment-events";
    public static final String BENEFIT_EVENTS = "benefit-events";
    public static final String REPORT_REQUESTS = "report-requests";
    public static final String EMAIL_NOTIFICATIONS = "email-notifications";
    public static final String AUDIT_LOGS = "audit-logs";
}

/**
 * USAGE EXAMPLES:
 *
 * 1. PRODUCER - Send message to Kafka:
 *
 * @Service
 * public class PaymentService {
 *
 *     @Autowired
 *     private KafkaTemplate<String, Object> kafkaTemplate;
 *
 *     public void processPayment(Payment payment) {
 *         // Process payment...
 *
 *         if (payment.isSuccessful()) {
 *             // Send event to Kafka
 *             PaymentEvent event = new PaymentEvent(
 *                 payment.getReference(),
 *                 payment.getAmount(),
 *                 "COMPLETED"
 *             );
 *
 *             kafkaTemplate.send(
 *                 KafkaTopics.PAYMENT_EVENTS,
 *                 payment.getReference(),
 *                 event
 *             );
 *         }
 *     }
 * }
 *
 * 2. CONSUMER - Listen for messages:
 *
 * @Service
 * public class EmailNotificationConsumer {
 *
 *     @Autowired
 *     private EmailService emailService;
 *
 *     @KafkaListener(
 *         topics = KafkaTopics.PAYMENT_EVENTS,
 *         groupId = "email-service"
 *     )
 *     public void handlePaymentEvent(PaymentEvent event) {
 *         // This runs asynchronously when payment event is published
 *
 *         if ("COMPLETED".equals(event.getStatus())) {
 *             emailService.sendPaymentConfirmation(
 *                 event.getMemberEmail(),
 *                 event.getMemberName(),
 *                 event.getAmount(),
 *                 event.getReference()
 *             );
 *         }
 *     }
 * }
 *
 * 3. EVENT CLASSES:
 *
 * @Data
 * @AllArgsConstructor
 * @NoArgsConstructor
 * public class PaymentEvent {
 *     private String reference;
 *     private BigDecimal amount;
 *     private String status;
 *     private String memberEmail;
 *     private String memberName;
 *     private LocalDateTime timestamp;
 * }
 *
 * BENEFITS OF KAFKA:
 *
 * 1. Async Processing:
 *    - Payment completes immediately
 *    - Email sent asynchronously
 *    - User doesn't wait
 *
 * 2. Decoupling:
 *    - Payment service doesn't know about email service
 *    - Easy to add more consumers
 *    - Services can scale independently
 *
 * 3. Reliability:
 *    - Messages persisted to disk
 *    - If consumer is down, messages wait
 *    - Retry failed messages
 *
 * 4. Scalability:
 *    - Add more consumers to handle load
 *    - Parallel processing
 *    - Load balancing automatic
 *
 * TESTING:
 *
 * # Start Kafka locally with Docker:
 * docker run -d --name kafka \
 *   -p 9092:9092 \
 *   apache/kafka:latest
 *
 * # View messages in topic:
 * kafka-console-consumer \
 *   --bootstrap-server localhost:9092 \
 *   --topic payment-events \
 *   --from-beginning
 *
 * MONITORING:
 *
 * # Check consumer lag:
 * kafka-consumer-groups \
 *   --bootstrap-server localhost:9092 \
 *   --describe \
 *   --group pension-system
 */
