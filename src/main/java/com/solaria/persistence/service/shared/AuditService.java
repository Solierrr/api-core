package com.solaria.persistence.service.shared;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class AuditService {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void purgeAuditLog(int retentionDays) {
        entityManager.createNativeQuery("CALL sp_purge_audit_log(:retentionDays)")
                .setParameter("retentionDays", retentionDays)
                .executeUpdate();
    }
}
