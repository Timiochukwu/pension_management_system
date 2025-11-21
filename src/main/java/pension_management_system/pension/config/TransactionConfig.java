package pension_management_system.pension.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * TransactionConfig - Configure transaction behavior
 *
 * Sets default timeout for transactions to prevent long-running queries
 */
@Configuration
public class TransactionConfig {

    /**
     * Default transaction timeout in seconds
     */
    public static final int DEFAULT_TIMEOUT_SECONDS = 30;

    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setTimeout(DEFAULT_TIMEOUT_SECONDS);
        template.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        return template;
    }
}
