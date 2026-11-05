package com.merlin.PropertManagmentSystem.Subscription;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {


    Optional<Subscription> findByMpesaReference(String mpesaReference);
    List<Subscription> findAllByOrganisationIdOrderByCreatedAtDesc(Long organisationId);
    List<Subscription> findAllByLandlordIdOrderByCreatedAtDesc(Long landlordId);

    List<Subscription> findAllByOrderByCreatedAtDesc();
    List<Subscription> findAllBySubscriptionStatusOrderByCreatedAtDesc(SubscriptionStatus subscriptionStatus);
}
