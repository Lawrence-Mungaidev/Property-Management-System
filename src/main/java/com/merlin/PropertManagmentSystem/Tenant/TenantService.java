package com.merlin.PropertManagmentSystem.Tenant;

import com.merlin.PropertManagmentSystem.Exception.BusinessRuleException;
import com.merlin.PropertManagmentSystem.Exception.ResourceNotFound;
import com.merlin.PropertManagmentSystem.Organisation.Organisation;
import com.merlin.PropertManagmentSystem.Property.Property;
import com.merlin.PropertManagmentSystem.Property.PropertyRepository;
import com.merlin.PropertManagmentSystem.PropertyAsign.PropertyAsign;
import com.merlin.PropertManagmentSystem.PropertyAsign.PropertyAsignRepository;
import com.merlin.PropertManagmentSystem.User.Role;
import com.merlin.PropertManagmentSystem.User.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;
    private final TenantMapper tenantMapper;
    private final PropertyRepository propertyRepository;
    private final PropertyAsignRepository  propertyAsignRepository;

    public TenantResponseDto createTenant(TenantDto dto, User authenticatedUser, Long propertyId){
        Tenant tenant = tenantMapper.toTenant(dto);

        boolean isOfficer = authenticatedUser.getRole().equals(Role.CHAIRMAN)
                || authenticatedUser.getRole().equals(Role.SECRETARY);

        if(authenticatedUser.getRole().equals(Role.LANDLORD)){
            tenant.setLandlord(authenticatedUser);
        } else if (isOfficer) {
            Organisation organisation = authenticatedUser.getOrganisation();
            tenant.setOrganisation(organisation);

        }else if(authenticatedUser.getRole().equals(Role.CARETAKER)){
            Property property = propertyRepository.findById(propertyId).
                    orElseThrow(()-> new ResourceNotFound("The property isn't found"));

            boolean isAssigned = propertyAsignRepository
                    .existsByCareTakerAndPropertyAndUnAssignedAtIsNull(authenticatedUser, property);

            if(!isAssigned){
                throw new BusinessRuleException("You are not assigned to this property");
            }

            if(property.getOrganisation() != null){
                tenant.setOrganisation(property.getOrganisation());
            } else if (property.getLandlord() != null) {
                tenant.setLandlord(property.getLandlord());

            }
        }else {
            throw new BusinessRuleException("You cant create a tenant");
        }

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(()-> new ResourceNotFound("The property isn't found"));

        if(tenantRepository.existsByNationalIdAndLeases_Unit_Property_Id(dto.nationalId(), property.getId())){
            throw new BusinessRuleException("The tenant's Id already exists");
        }

        var savedTenant = tenantRepository.save(tenant);

        return tenantMapper.toTenantResponseDto(savedTenant);
    }

    public TenantResponseDto updateTenant(TenantDto dto, User authenticatedUser, Long tenantId){
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(()-> new ResourceNotFound("The tenant isn't found"));

        boolean isOfficer = authenticatedUser.getRole().equals(Role.CHAIRMAN)
                || authenticatedUser.getRole().equals(Role.SECRETARY);

        if(authenticatedUser.getRole().equals(Role.LANDLORD)){
            if(!tenant.getLandlord().equals(authenticatedUser)){
                throw new BusinessRuleException("You cannot update the tenant since they already moved out ");
            }
        } else if (isOfficer) {
            Organisation organisation = authenticatedUser.getOrganisation();
            if(!tenant.getOrganisation().equals(organisation)){
                throw new BusinessRuleException("You cannot update the tenant since they already moved out");
            }
        }else if(authenticatedUser.getRole().equals(Role.CARETAKER)){

            Property property = tenant.getLandlord().getProperty();

            boolean isAssigned = propertyAsignRepository
                    .existsByCareTakerAndPropertyAndUnAssignedAtIsNull(authenticatedUser, property);

            if(!isAssigned){
                throw new BusinessRuleException("You are not assigned to this property");
            }

        }else {
            throw new BusinessRuleException("You are not assigned to this property");
        }


        if(dto.firstName() != null){
            tenant.setFirstName(dto.firstName());
        }

        if(dto.lastName() != null){
            tenant.setLastName(dto.lastName());
        }
        if(dto.email() != null){
            tenant.setEmail(dto.email());
        }
        if(dto.phoneNumber() != null){
            tenant.setPhoneNumber(dto.phoneNumber());
        }
        if(dto.nationalId() != null){
            tenant.setNationalId(dto.nationalId());
        }

        var savedTenant = tenantRepository.save(tenant);

        return tenantMapper.toTenantResponseDto(savedTenant);
    }

    public List<TenantResponseDto> getAllTenants(User authenticatedUser){
        List<Tenant> tenants = new ArrayList<>();

        boolean isOfficer = authenticatedUser.getRole().equals(Role.CHAIRMAN)
                || authenticatedUser.getRole().equals(Role.SECRETARY);

        if(authenticatedUser.getRole().equals(Role.LANDLORD)){
            tenants = tenantRepository.findAllByLandlord(authenticatedUser);
        } else if (isOfficer) {
            Organisation organisation = authenticatedUser.getOrganisation();

            tenants = tenantRepository.findAllByOrganisation(organisation);

        }else if(authenticatedUser.getRole().equals(Role.CARETAKER)){

            List<Property> assignedProperties = propertyAsignRepository
                    .findByCareTakerAndUnAssignedAtIsNull(authenticatedUser);

            User landLord = authenticatedUser.getLandlord();

            if(authenticatedUser.getLandlord() != null ){
                tenants = tenantRepository.findAllByLandlord(landLord);
            }else if(authenticatedUser.getOrganisation() != null ){
                tenants = tenantRepository.findAllByOrganisation(authenticatedUser.getOrganisation());
            }

        }

        return tenants
                .stream()
                .map(tenantMapper :: toTenantResponseDto)
                .toList();
    }


    public TenantResponseDto getTenantById(Long tenantId, User authenticatedUser){
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(()-> new ResourceNotFound("The tenant isn't found"));


        Tenant rightTenant  = null;

        boolean isOfficer = authenticatedUser.getRole().equals(Role.CHAIRMAN)
                || authenticatedUser.getRole().equals(Role.SECRETARY)
                || authenticatedUser.getRole().equals(Role.TREASURER);

        if(authenticatedUser.getRole().equals(Role.LANDLORD)){
            if(tenant.getLandlord().equals(authenticatedUser)){
                rightTenant = tenant;
            }


        } else if (isOfficer) {

            if(tenant.getOrganisation().equals(authenticatedUser.getOrganisation())){
                rightTenant = tenant;
            }

        }else if(authenticatedUser.getRole().equals(Role.CARETAKER)){
            if(tenant.getLandlord().equals(authenticatedUser.getLandlord())){
                rightTenant = tenant;
            }
        }


        return tenantMapper.toTenantResponseDto(rightTenant);
    }
}
