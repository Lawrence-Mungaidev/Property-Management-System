package com.merlin.PropertManagmentSystem.Subscription;

import org.springframework.stereotype.Component;

@Component
public class SubscriptionMapper {

    public SubscriptionResponseDto toSubscriptionResponseDto(Subscription subscription) {
        return new SubscriptionResponseDto(subscription.getId(), subscription.getPhoneNumberPaid(),subscription.getStartDate(), subscription.getEndDate(),subscription.getSubscriptionMadeBy().getId(),subscription.getCreatedAt(),subscription.getSubscriptionStatus());
    }

}
