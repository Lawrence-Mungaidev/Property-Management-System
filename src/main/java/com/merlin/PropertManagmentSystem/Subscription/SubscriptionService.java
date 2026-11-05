package com.merlin.PropertManagmentSystem.Subscription;

import com.merlin.PropertManagmentSystem.Exception.BusinessRuleException;
import com.merlin.PropertManagmentSystem.Exception.ResourceNotFound;
import com.merlin.PropertManagmentSystem.Mpesa.StkPushService;
import com.merlin.PropertManagmentSystem.Unit.UnitRepository;
import com.merlin.PropertManagmentSystem.User.Role;
import com.merlin.PropertManagmentSystem.User.User;
import com.merlin.PropertManagmentSystem.User.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final StkPushService stkPushService;
    private final UnitRepository unitRepository;

    @Value("${subscription.amount}")
    private BigDecimal subscriptionAmount;

    @Transactional
    public SubscriptionResponseDto subscribe(SubscriptionDto dto, User authenticatedUser) {
        boolean isOfficer = authenticatedUser.getRole().equals(Role.CHAIRMAN)
                ||  authenticatedUser.getRole().equals(Role.SECRETARY) || authenticatedUser.getRole().equals(Role.TREASURER);

        Subscription subscription = new Subscription();

        long numberOfUnit;

        if(authenticatedUser.getRole().equals(Role.LANDLORD)) {
            subscription.setLandlord(authenticatedUser);

            numberOfUnit = unitRepository.countByPropertyLandlordId(authenticatedUser.getId());

        } else if (isOfficer && authenticatedUser.isActive()) {

            subscription.setOrganisation(authenticatedUser.getOrganisation());

            numberOfUnit = unitRepository.countByPropertyOrganisationId(authenticatedUser.getOrganisation().getId());

        }else {
            throw new BusinessRuleException("Only officer or landlord roles can make a subscription");
        }


        BigDecimal totalAmount =   subscriptionAmount.multiply(new BigDecimal(numberOfUnit));


        String mpesaReference = stkPushService.initialiseSTKPush(dto.phoneNumber(), totalAmount );

        subscription.setSubscriptionStatus(SubscriptionStatus.PENDING);
        subscription.setCreatedAt(LocalDateTime.now());


        subscription.setMpesaReference(mpesaReference);
        subscription.setSubscriptionAmount(totalAmount);
        subscriptionRepository.save(subscription);

        var savedSubscription = subscriptionRepository.save(subscription);

        return subscriptionMapper.toSubscriptionResponseDto(savedSubscription);
    }

    public List<SubscriptionResponseDto> getAllSubscription(User authenticatedUser){
        if(!authenticatedUser.getRole().equals(Role.ADMIN)) {
            throw new BusinessRuleException("Sorry you're not allowed to access this");
        }

        return subscriptionRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(subscriptionMapper :: toSubscriptionResponseDto)
                .toList();

    }

    public List<SubscriptionResponseDto> getMySubscription(User authenticatedUser){
        boolean isOfficer = authenticatedUser.getRole().equals(Role.CHAIRMAN)
                ||  authenticatedUser.getRole().equals(Role.SECRETARY) || authenticatedUser.getRole().equals(Role.TREASURER);

        List<Subscription>  mySubscription;

        if(authenticatedUser.getRole().equals(Role.LANDLORD)) {
            mySubscription = subscriptionRepository.findAllByLandlordIdOrderByCreatedAtDesc(authenticatedUser.getId());
        } else if (isOfficer && authenticatedUser.isActive()) {
            mySubscription = subscriptionRepository.findAllByOrganisationIdOrderByCreatedAtDesc(authenticatedUser.getOrganisation().getId());
        }else {
            throw new BusinessRuleException("You cant view this subscriptions");
        }

        return  mySubscription
                .stream()
                .map(subscriptionMapper :: toSubscriptionResponseDto)
                .toList();
    }

    public SubscriptionResponseDto getSubscriptionById(Long id, User authenticatedUser) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFound("Subscription not found"));

        boolean isOfficer = authenticatedUser.getRole().equals(Role.CHAIRMAN)
                ||  authenticatedUser.getRole().equals(Role.SECRETARY) || authenticatedUser.getRole().equals(Role.TREASURER);

        Subscription rightSubscription;

        if(authenticatedUser.getRole().equals(Role.ADMIN)) {
            rightSubscription = subscription;
        } else if (authenticatedUser.getRole().equals(Role.LANDLORD)) {

            if(!subscription.getLandlord().getId().equals(authenticatedUser.getId())) {
                throw new BusinessRuleException("You cant view this subscription");
            }

            rightSubscription = subscription;

        } else if (isOfficer && authenticatedUser.isActive()) {

            if(!subscription.getOrganisation().getId().equals(authenticatedUser.getOrganisation().getId())) {
                throw new BusinessRuleException("You cant view this subscription");
            }

            rightSubscription = subscription;
        }else {
            throw new BusinessRuleException("You cant view this subscription");
        }

        return subscriptionMapper.toSubscriptionResponseDto(rightSubscription);
    }

}
