package com.merlin.PropertManagmentSystem.PaymentDispute;

import org.springframework.stereotype.Component;

@Component
public class PaymentDisputeMapper {

    public PDResponseDTO toPDResponseDTO(PaymentDispute pd) {

        String resolvedBy = pd.getResolvedBy().getFirstName() + " " + pd.getResolvedBy().getLastName();

        return new PDResponseDTO(pd.getId(), pd.getTransactionLog().getId(),pd.getOriginalLease().getId(),pd.getCorrectLease().getId(), pd.getPaymentDisputeType(),resolvedBy,pd.getResolvedAt(),pd.getNote());
    }


}
