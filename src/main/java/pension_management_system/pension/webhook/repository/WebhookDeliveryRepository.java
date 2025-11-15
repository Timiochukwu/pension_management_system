package pension_management_system.pension.webhook.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pension_management_system.pension.webhook.entity.WebhookDelivery;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WebhookDeliveryRepository extends JpaRepository<WebhookDelivery, Long> {

    List<WebhookDelivery> findByWebhookId(Long webhookId);

    List<WebhookDelivery> findByStatus(WebhookDelivery.DeliveryStatus status);

    List<WebhookDelivery> findByStatusAndNextRetryAtBefore(
            WebhookDelivery.DeliveryStatus status,
            LocalDateTime time
    );
}
