package com.solaria.persistence.mapper.company;

import java.util.UUID;
import com.solaria.persistence.domain.entity.company.Company;
import com.solaria.persistence.domain.entity.company.BusinessContact;
import com.solaria.persistence.domain.entity.shared.Address;
import com.solaria.persistence.dto.response.company.CompanyResponseDTO;
import com.solaria.persistence.dto.response.company.BusinessContactResponseDTO;
import com.solaria.persistence.dto.response.shared.AddressResponseDTO;

/** Pure projection; a null viewer company exposes only public company data. */
public final class CompanyResponseMapper {
    private CompanyResponseMapper() { }

    public static CompanyResponseDTO toResponse(Company company, UUID actorCompanyId) {
        if (company == null) {
            return null;
        }
        CompanyResponseDTO dto = new CompanyResponseDTO();
        dto.setId(company.getId());
        dto.setStatus(company.getStatus());
        dto.setCnpj(company.getCnpj());
        dto.setTradeName(company.getTradeName());
        dto.setCorporateName(company.getCorporateName());
        dto.setSlug(company.getSlug());
        dto.setAddress(toAddressResponse(company.getAddress()));
        dto.setBusinessContact(company.getId().equals(actorCompanyId)
                ? toBusinessContactResponse(company.getBusinessContact()) : null);
        return dto;
    }

    private static AddressResponseDTO toAddressResponse(Address address) {
        if (address == null) {
            return null;
        }
        return AddressResponseDTO.builder()
                .id(address.getId())
                .ownerCompanyId(address.getOwnerCompany().getId())
                .state(address.getState())
                .city(address.getCity())
                .neighborhood(address.getNeighborhood())
                .zipCode(address.getZipCode())
                .street(address.getStreet())
                .number(address.getNumber())
                .build();
    }

    private static BusinessContactResponseDTO toBusinessContactResponse(BusinessContact businessContact) {
        if (businessContact == null) {
            return null;
        }
        BusinessContactResponseDTO dto = new BusinessContactResponseDTO();
        dto.setId(businessContact.getId());
        dto.setCompanyEmail(businessContact.getCompanyEmail());
        dto.setPhone(businessContact.getPhone());
        dto.setWebsite(businessContact.getWebsite());
        return dto;
    }
}
