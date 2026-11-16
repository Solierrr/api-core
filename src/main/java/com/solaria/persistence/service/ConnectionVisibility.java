package com.solaria.persistence.service;

import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.repository.identity.UserCompanyRepository;
import com.solaria.persistence.repository.identity.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConnectionVisibility {
    private final UserCompanyRepository memberships;
    private final PersonRepository persons;

    public boolean visible(User owner, User target) {
        if (!Boolean.TRUE.equals(owner.getActive()) || !Boolean.TRUE.equals(target.getActive())
                || owner.getId().equals(target.getId()) || !persons.existsByUserId(target.getId())) return false;
        var company = owner.getManagedByCompanyId();
        if (company != null && !company.equals(target.getManagedByCompanyId())
                && !memberships.existsByUserIdAndCompanyId(target.getId(), company)) return false;
        var targetCompany = target.getManagedByCompanyId();
        return targetCompany == null || targetCompany.equals(owner.getManagedByCompanyId())
                || memberships.existsByUserIdAndCompanyId(owner.getId(), targetCompany);
    }
}
