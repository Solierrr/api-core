package com.solaria.persistence.repository.company;

import com.solaria.persistence.domain.entity.company.Charge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface ChargeRepository extends JpaRepository<Charge, UUID> {

    List<Charge> findBySubscription_Supplier_Company_Id(UUID companyId);

    Optional<Charge> findByIdAndSubscription_Supplier_Company_Id(UUID id, UUID companyId);

    List<Charge> findBySubscriptionId(UUID subscriptionId);

}
