package com.merlin.PropertManagmentSystem.ScheduledTask;


import com.merlin.PropertManagmentSystem.Notification.NotificationService;
import com.merlin.PropertManagmentSystem.Notification.NotificationType;
import com.merlin.PropertManagmentSystem.Notification.channel;
import com.merlin.PropertManagmentSystem.Subscription.Subscription;
import com.merlin.PropertManagmentSystem.Subscription.SubscriptionRepository;
import com.merlin.PropertManagmentSystem.Subscription.SubscriptionStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

import java.util.List;


@Component
@RequiredArgsConstructor
public class ScheduledTask {

    private final SubscriptionRepository subscriptionRepository;
    private final NotificationService notificationService;


    @Scheduled(cron = "0 0 0 * * *", zone = "Africa/Nairobi")
    @Transactional
    public void endSubscriptions(){

        List<Subscription> subscriptions = subscriptionRepository.findAllBySubscriptionStatusOrderByCreatedAtDesc(SubscriptionStatus.ACTIVE);

        for (Subscription subscription : subscriptions) {
            if(subscription.getEndDate().isBefore(LocalDate.now())){
                subscription.setSubscriptionStatus(SubscriptionStatus.EXPIRED);
                subscriptionRepository.save(subscription);

                String message = "Your subscription has expired";
                notificationService.systemNotification(message,subscription.getSubscriptionMadeBy(), NotificationType.SUBSCRIPTION_EXPIRED, channel.SMS);

            }
        }

    }

}
