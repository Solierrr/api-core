package com.solaria.persistence.support;

import org.hibernate.boot.model.TypeContributions;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.SqlTypes;
import org.hibernate.type.descriptor.sql.internal.DdlTypeImpl;

public class NamedEnumH2Dialect extends H2Dialect {

    @Override
    protected void registerColumnTypes(TypeContributions typeContributions, ServiceRegistry serviceRegistry) {
        super.registerColumnTypes(typeContributions, serviceRegistry);
        typeContributions.getTypeConfiguration().getDdlTypeRegistry()
                .addDescriptor(new DdlTypeImpl(SqlTypes.NAMED_ENUM, "varchar(255)", this));
    }
}
