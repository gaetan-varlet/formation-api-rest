package fr.insee.formationapirest.integrationtests.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class DatabaseManager {

    private static final Pattern DATE_TIME_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
    private static final Pattern DATE_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final List<String> TABLES_A_EXCLURE = List.of("MODULE");

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    @PersistenceContext private final EntityManager entityManager;

    @Transactional
    public void insertInTable(String nomSchema, String nomTable, List<Map<String, String>> data) {
        log.info("Chargement de la table {}.{}", nomSchema, nomTable);
        if (nomTable == null || data == null || data.isEmpty()) {
            return;
        }
        // récupération des colonnes sur la première ligne de données
        List<String> colonnes = new ArrayList<>(data.getFirst().keySet());
        if (colonnes.isEmpty()) {
            return;
        }
        // construction dynamique de la requête SQL (ex: INSERT INTO schema.table (col1, col2) VALUES (?, ?))
        String colonnesSql = String.join(", ", colonnes);
        String placeholders = colonnes.stream().map(c -> "?").collect(Collectors.joining(", "));
        String requete = """
                INSERT INTO %s.%s (%s) VALUES (%s)
                """.formatted(nomSchema, nomTable, colonnesSql, placeholders);

        // insertion des données
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(requete)) {
            for (Map<String, String> ligne : data) {
                if (estLigneVide(ligne)) {
                    continue;
                }
                for (int i = 0; i < colonnes.size(); i++) {
                    String value = ligne.get(colonnes.get(i));
                    if (StringUtils.isEmpty(value)) {
                        ps.setObject(i + 1, null);
                    } else if (isDateTime(value)) {
                        ps.setObject(i + 1, convertToDateTime(value));
                    } else if (isDate(value)) {
                        ps.setObject(i + 1, convertToDate(value));
                    } else {
                        ps.setObject(i + 1, value);
                    }
                }
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'insertion en batch dans " + nomSchema + "." + nomTable, e);
        }
    }

    private boolean estLigneVide(Map<String, String> ligne) {
        return ligne.values().stream().allMatch(StringUtils::isEmpty);
    }

    private boolean isDateTime(String value) {
        return DATE_TIME_PATTERN.matcher(value).matches();
    }

    private LocalDateTime convertToDateTime(String value) {
        return LocalDateTime.parse(value, DATE_TIME_FORMATTER);
    }

    private boolean isDate(String value) {
        return DATE_PATTERN.matcher(value).matches();
    }

    private LocalDate convertToDate(String value) {
        return LocalDate.parse(value, DATE_FORMATTER);
    }

    @Transactional
    public void clearDatabase() {
        log.info("CLEAR DATABASE");
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();

        String schema = "FORMATION";
        List<String> tables = tablesSchema(schema);
        truncateTables(schema, tables);
        restartIds(schema, tables);

        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
    }

    private List<String> tablesSchema(String schema) {
        String requete = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE UPPER(TABLE_SCHEMA) = ?";
        List<String> tables = jdbcTemplate.queryForList(requete, String.class, schema.toUpperCase());
        log.trace("LISTE DES TABLES DU SCHEMA {} : {}", schema, tables);
        return tables;
    }

    private void truncateTables(String schema, List<String> tables) {
        tables.forEach(table -> truncateTable(schema, table));
    }

    private void truncateTable(String schema, String table) {
        entityManager.createNativeQuery("TRUNCATE TABLE " + schema + "." + table).executeUpdate();
    }

    private void restartIds(String schema, List<String> tables) {
        tables.stream()
                .filter(table -> !TABLES_A_EXCLURE.contains(table.toUpperCase()))
                .forEach(table -> restartId(schema, table));
    }

    private void restartId(String schema, String table) {
        if (!TABLES_A_EXCLURE.contains(table.toUpperCase())) {
            String requete = """
                    ALTER TABLE %s.%s 
                    ALTER COLUMN id RESTART WITH (SELECT COALESCE(MAX(id) + 1, 1) FROM %s.%s)
                    """.formatted(schema, table, schema, table);
            entityManager.createNativeQuery(requete).executeUpdate();
        }
    }
}
