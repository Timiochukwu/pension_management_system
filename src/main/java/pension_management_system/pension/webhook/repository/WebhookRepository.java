package pension_management_system.pension.webhook.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pension_management_system.pension.webhook.entity.Webhook;

import java.util.List;

@Repository
public interface WebhookRepository extends JpaRepository<Webhook, Long> {

    List<Webhook> findByActiveTrue();

    List<Webhook> findByActiveTrueAndEventsContaining(String eventType);
}
