package com.merlin.PropertManagmentSystem.Mpesa;

import com.merlin.PropertManagmentSystem.Exception.PaymentException;
import com.merlin.PropertManagmentSystem.Notification.NotificationService;
import com.merlin.PropertManagmentSystem.Notification.NotificationType;
import com.merlin.PropertManagmentSystem.Notification.channel;
import com.merlin.PropertManagmentSystem.Subscription.Subscription;
import com.merlin.PropertManagmentSystem.Subscription.SubscriptionRepository;
import com.merlin.PropertManagmentSystem.Subscription.SubscriptionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class StkPushService {
    private final WebClient webClient;
    private final SubscriptionRepository subscriptionRepository;
    private final NotificationService notificationService;


    @Value("${STK.consumer-key}")
    String consumerKey;

    @Value("${STK.consumer-secret}")
    String consumerSecret;

    @Value("${STK.short-code}")
    String shortCode;

    @Value("${STK.passkey}")
    String passkey;

    @Value("${STK.callback-url}")
    String callbackUrl;

    public StkPushService(WebClient webClient, SubscriptionRepository subscriptionRepository, NotificationService notificationService) {
        this.webClient = webClient;
        this.subscriptionRepository = subscriptionRepository;
        this.notificationService = notificationService;
    }


    private String getAccessToken() {
        String credentials = consumerKey + ":" + consumerSecret;
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());

        return webClient.get()
                .uri("https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials")
                .header("Authorization", "Basic " + encoded)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> (String) response.get("access_token"))
                .block();
    }

    public String initialiseSTKPush(String phoneNumber, BigDecimal amount) {
        String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String password = Base64.getEncoder().encodeToString((shortCode + passkey + timeStamp).getBytes());
        String formattedPhone = "254" + phoneNumber.substring(1);

        Map<String, Object> request = new HashMap<>();
        request.put("BusinessShortCode", shortCode);
        request.put("Password", password);
        request.put("Timestamp", timeStamp);
        request.put("TransactionType", "CustomerPayBillOnline");
        request.put("Amount", amount.intValue());
        request.put("PartyA", phoneNumber);
        request.put("PartyB", shortCode);
        request.put("PhoneNumber", formattedPhone);
        request.put("CallBackURL", callbackUrl);
        request.put("AccountReference", "Inventory Management System");
        request.put("TransactionDesc", "payment");

        return webClient.post()
                .uri("https://sandbox.safaricom.co.ke/mpesa/stkpush/v1/processrequest")
                .header("Authorization", "Bearer " + getAccessToken())
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), response ->
                        response.bodyToMono(String.class)
                                .doOnNext(body -> System.out.println("STK PUSH Error " + body))
                                .then(Mono.error(new PaymentException("STK PUSH FAILED"))))
                .bodyToMono(Map.class)
                .doOnNext(response -> System.out.println("STK Response: " + response))
                .map(response -> (String) response.get("CheckoutRequestID"))
                .block();
    }

    public void handleCallBack(Map<String, Object> callbackData) {
        Map<String, Object> body = (Map<String, Object>) callbackData.get("Body");
        Map<String, Object> stkCallback = (Map<String, Object>) body.get("stkCallback");
        String checkoutRequestId = (String) stkCallback.get("CheckoutRequestID");
        int resultCode = (int) stkCallback.get("ResultCode");

        Subscription subscription = subscriptionRepository.findByMpesaReference(checkoutRequestId)
                .orElseThrow(() -> new PaymentException("STK PUSH FAILED"));

        if (resultCode != 0) {
            subscription.setSubscriptionStatus(SubscriptionStatus.FAILED);
            subscriptionRepository.save(subscription);

            String message = "Subscription attempt FAILED. Please Try again";

            notificationService.systemNotification(message, subscription.getSubscriptionMadeBy(), NotificationType.SUBSCRIPTION_FAILED, channel.SMS);
            return;
        }


        subscription.setSubscriptionStatus(SubscriptionStatus.ACTIVE);

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(30);

        subscription.setStartDate(startDate);
        subscription.setEndDate(endDate);

        String message = "Your subscription is a SUCCESS.";

        notificationService.systemNotification(message, subscription.getSubscriptionMadeBy(), NotificationType.SUBSCRIPTION_SUCCESSFUL, channel.SMS);

        subscriptionRepository.save(subscription);

    }
}
