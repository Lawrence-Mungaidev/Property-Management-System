package com.merlin.PropertManagmentSystem.Tenant;

import org.springframework.stereotype.Component;

@Component
public class TenantMapper {

    public Tenant toTenant(TenantDto dto){
        Tenant tenant = new Tenant();
        tenant.setFirstName(dto.firstName());
        tenant.setLastName(dto.lastName());
        tenant.setNationalId(dto.nationalId());
        tenant.setPhoneNumber(dto.phoneNumber());

        return tenant;
    }

    public TenantResponseDto toTenantResponseDto(Tenant tenant){
        return new TenantResponseDto(tenant.getId(), tenant.getFirstName(), tenant.getLastName(), tenant.getNationalId(), tenant.getPhoneNumber(), tenant.getWalletBalance());
    }

}
