package com.solaria.persistence.service.analytics;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.solaria.persistence.exception.AnalyticsUnavailableException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import jakarta.annotation.PreDestroy;

/**
 * Leitura do schema {@code gold} do analyticsdb.
 *
 * <p>O banco usa um único login de leitura e RLS: cada requisição abre uma transação, informa o escopo e a
 * entidade com {@code set_config(..., true)} e só então consulta. Sem esse contexto o banco devolve zero linhas.
 * O pool fica fora do contexto do Spring de propósito, para não substituir o DataSource do coredb.</p>
 */
@Component
public class AnalyticsReader {

    private final HikariDataSource dataSource;

    public AnalyticsReader(
            @Value("${analytics.db.host:}") String host,
            @Value("${analytics.db.port:5432}") String port,
            @Value("${analytics.db.name:}") String name,
            @Value("${analytics.db.user:}") String user,
            @Value("${analytics.db.password:}") String password,
            @Value("${analytics.db.sslmode:require}") String sslmode,
            @Value("${analytics.db.pool-size:3}") int poolSize) {
        if (host.isBlank() || name.isBlank() || user.isBlank() || password.isBlank()) {
            this.dataSource = null;
            return;
        }
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://" + host + ":" + port + "/" + name);
        config.setUsername(user);
        config.setPassword(password);
        config.setMaximumPoolSize(poolSize);
        config.setReadOnly(true);
        config.setInitializationFailTimeout(-1);
        config.setPoolName("analytics-gold");
        config.addDataSourceProperty("sslmode", sslmode);
        config.addDataSourceProperty("channelBinding", "disable");
        this.dataSource = new HikariDataSource(config);
    }

    public boolean isConfigured() {
        return dataSource != null;
    }

    /**
     * Lê as tabelas pedidas no contexto informado. {@code tables} deve vir de uma lista fixa do código, nunca de
     * entrada do usuário: os nomes entram no SQL sem parâmetro.
     */
    public Map<String, List<Map<String, Object>>> read(String scope, String entityId, List<String> tables) {
        if (dataSource == null) {
            throw new AnalyticsUnavailableException("O banco de indicadores não está configurado");
        }
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                setContext(connection, scope, entityId);
                Map<String, List<Map<String, Object>>> result = new LinkedHashMap<>();
                for (String table : tables) {
                    result.put(table, query(connection, "SELECT * FROM gold." + table));
                }
                connection.commit();
                return result;
            } catch (SQLException | RuntimeException ex) {
                connection.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw new AnalyticsUnavailableException("Não foi possível consultar os indicadores");
        }
    }

    /** Data da última carga; vazio se a tabela de controle ainda não existe. */
    public String loadedAt() {
        if (dataSource == null) {
            return null;
        }
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT max(loaded_at)::text FROM gold.load_info");
                ResultSet rows = statement.executeQuery()) {
            return rows.next() ? rows.getString(1) : null;
        } catch (SQLException ex) {
            return null;
        }
    }

    private static void setContext(Connection connection, String scope, String entityId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT set_config('gold.scope', ?, true), set_config('gold.entity_id', ?, true)")) {
            statement.setString(1, scope);
            statement.setString(2, entityId);
            statement.execute();
        }
    }

    private static List<Map<String, Object>> query(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql); ResultSet rows = statement.executeQuery()) {
            ResultSetMetaData meta = rows.getMetaData();
            List<Map<String, Object>> list = new ArrayList<>();
            while (rows.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    Object value = rows.getObject(i);
                    row.put(meta.getColumnLabel(i), value instanceof java.sql.Timestamp ts ? ts.toInstant().toString()
                            : value instanceof java.sql.Date d ? d.toLocalDate().toString() : value);
                }
                list.add(row);
            }
            return list;
        }
    }

    @PreDestroy
    void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
