package com.solaria.persistence.repository.execution;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.solaria.persistence.domain.entity.execution.TechnicalService;

public interface MobileServiceAccessRepository extends Repository<TechnicalService, UUID> {

    @Query("""
            select service.id as serviceId, membership.position.name as positionName,
                exists (select permissionGrant.id from PositionPermission permissionGrant
                    where permissionGrant.position.id = membership.position.id
                        and permissionGrant.permission.permissionName = :acceptPermission) as canAccept,
                exists (select permissionGrant.id from PositionPermission permissionGrant
                    where permissionGrant.position.id = membership.position.id
                        and permissionGrant.permission.permissionName = :completePermission) as canComplete
            from TechnicalService service, UserCompany membership
            where service.id in :serviceIds and membership.user.id = :userId
                and membership.company.id = service.technicalProject.requester.company.id
            """)
    List<RequesterAccessRow> findRequesterAccess(@Param("serviceIds") Collection<UUID> serviceIds,
            @Param("userId") UUID userId, @Param("acceptPermission") String acceptPermission,
            @Param("completePermission") String completePermission);

    interface RequesterAccessRow {
        UUID getServiceId();
        String getPositionName();
        boolean getCanAccept();
        boolean getCanComplete();
    }
}
