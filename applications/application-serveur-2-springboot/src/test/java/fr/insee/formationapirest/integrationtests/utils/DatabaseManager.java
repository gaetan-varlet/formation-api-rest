package fr.insee.formationapirest.integrationtests.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class DatabaseManager {

    private static final Pattern DATE_TIME_PATTERN =
            Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
    private static final Pattern DATE_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final Pattern FORMAT_IDENTIFIANT_SQL = Pattern.compile("^[a-zA-Z0-9_]+$");
    private static final List<String> TABLES_A_EXCLURE = List.of("MODULE");
    private static final String SCHEMA_PAR_DEFAUT = "FORMATION";

    private final DataSource dataSource;

    /**
     * Insère un jeu de données dynamique dans une table spécifique.
     *
     * @param nomSchema Le schéma de la table (ex: PUBLIC)
     * @param nomTable Le nom de la table cible
     * @param data Les données sous forme de liste clé/valeur
     */
    @Transactional
    public void insertInTable(String nomSchema, String nomTable, List<Map<String, String>> data) {
        log.info("Chargement de la table {}.{}", nomSchema, nomTable);
        if (nomTable == null || data == null || data.isEmpty()) {
            return;
        }
        List<String> colonnes = new ArrayList<>(data.getFirst().keySet());
        if (colonnes.isEmpty()) {
            return;
        }
        validerIdentifiantsSql(nomSchema, nomTable, colonnes);
        String requete = buildInsertQuery(nomSchema, nomTable, colonnes);
        executeInsertBatch(requete, colonnes, data);
        restartId(nomSchema, nomTable);
    }

    /**
     * Compte le nombre de lignes présentes dans une table.
     *
     * @param schema Le schéma cible
     * @param table La table cible
     * @return Le nombre total de lignes, 0 si la table est vide
     */
    public int countNbLignes(String schema, String table) {
        validerIdentifiantSql(schema);
        validerIdentifiantSql(table);
        String requete = "SELECT COUNT(*) FROM " + schema + "." + table;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(requete);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int resultat = rs.getInt(1);
                log.trace("NOMBRE DE LIGNES DE LA TABLE {}.{} : {}", schema, table, resultat);
                return resultat;
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erreur lors du comptage des lignes sur " + schema + "." + table, e);
        }
    }

    /**
     * Vide entièrement le schéma par défaut et réinitialise les compteurs d'auto-incrémentation.
     * Les contraintes d'intégrité référentielle sont temporairement désactivées pour permettre le
     * vidage des tables sans se soucier de l'ordre lié aux clés étrangères.
     */
    @Transactional
    public void clearDatabase() {
        log.info("CLEAR DATABASE");
        desactiverContraintesIntegrite();
        List<String> tables = extraireTablesDuSchema(SCHEMA_PAR_DEFAUT);
        truncateTables(SCHEMA_PAR_DEFAUT, tables);
        restartIds(SCHEMA_PAR_DEFAUT, tables);
        reactiverContraintesIntegrite();
    }

    /* -------------------------------------------------------------------------
     * Méthodes privées : Sécurisation et construction de requêtes
     * ------------------------------------------------------------------------- */

    private void validerIdentifiantsSql(String schema, String table, List<String> colonnes) {
        validerIdentifiantSql(schema);
        validerIdentifiantSql(table);
        colonnes.forEach(this::validerIdentifiantSql);
    }

    private void validerIdentifiantSql(String identifiant) {
        if (identifiant == null || !FORMAT_IDENTIFIANT_SQL.matcher(identifiant).matches()) {
            throw new IllegalArgumentException(
                    "Identifiant SQL invalide (caractères non autorisés) : " + identifiant);
        }
    }

    private String buildInsertQuery(String schema, String table, List<String> colonnes) {
        String colonnesSql = String.join(", ", colonnes);
        String placeholders = colonnes.stream().map(c -> "?").collect(Collectors.joining(", "));
        return "INSERT INTO %s.%s (%s) VALUES (%s)"
                .formatted(schema, table, colonnesSql, placeholders);
    }

    /* -------------------------------------------------------------------------
     * Méthodes privées : Exécution JDBC
     * ------------------------------------------------------------------------- */

    private void executeInsertBatch(
            String requete, List<String> colonnes, List<Map<String, String>> data) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(requete)) {
            for (Map<String, String> ligne : data) {
                if (estLigneVide(ligne)) {
                    continue;
                }
                populatePreparedStatement(ps, colonnes, ligne);
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'exécution du batch SQL : " + requete, e);
        }
    }

    private void populatePreparedStatement(
            PreparedStatement ps, List<String> colonnes, Map<String, String> ligne)
            throws SQLException {
        for (int i = 0; i < colonnes.size(); i++) {
            Object valeur = parseValue(ligne.get(colonnes.get(i)));
            ps.setObject(i + 1, valeur);
        }
    }

    private void executeStatement(String sql) {
        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'exécution de la requête : " + sql, e);
        }
    }

    /* -------------------------------------------------------------------------
     * Méthodes privées : Gestion du nettoyage de la base
     * ------------------------------------------------------------------------- */

    private void desactiverContraintesIntegrite() {
        executeStatement("SET REFERENTIAL_INTEGRITY FALSE");
    }

    private void reactiverContraintesIntegrite() {
        executeStatement("SET REFERENTIAL_INTEGRITY TRUE");
    }

    private List<String> extraireTablesDuSchema(String schema) {
        String requete =
                "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE UPPER(TABLE_SCHEMA) = ?";
        List<String> tables = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(requete)) {
            ps.setString(1, schema.toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tables.add(rs.getString("TABLE_NAME"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erreur lors de la récupération des tables du schéma " + schema, e);
        }
        log.trace("LISTE DES TABLES DU SCHEMA {} : {}", schema, tables);
        return tables;
    }

    private void truncateTables(String schema, List<String> tables) {
        tables.forEach(table -> executeStatement("TRUNCATE TABLE " + schema + "." + table));
    }

    private void restartIds(String schema, List<String> tables) {
        tables.forEach(table -> restartId(schema, table));
    }

    private void restartId(String schema, String table) {
        if (!TABLES_A_EXCLURE.contains(table.toUpperCase())) {
            String requete =
                    """
                    ALTER TABLE %s.%s
                    ALTER COLUMN id RESTART WITH (SELECT COALESCE(MAX(id) + 1, 1) FROM %s.%s)
                    """
                            .formatted(schema, table, schema, table);
            executeStatement(requete);
        }
    }

    /* -------------------------------------------------------------------------
     * Méthodes privées : Utilitaires de conversion
     * ------------------------------------------------------------------------- */

    private Object parseValue(String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        if (isDateTime(value)) {
            return LocalDateTime.parse(value, DATE_TIME_FORMATTER);
        }
        if (isDate(value)) {
            return LocalDate.parse(value, DATE_FORMATTER);
        }
        return value;
    }

    private boolean estLigneVide(Map<String, String> ligne) {
        return ligne.values().stream().allMatch(StringUtils::isEmpty);
    }

    private boolean isDateTime(String value) {
        return DATE_TIME_PATTERN.matcher(value).matches();
    }

    private boolean isDate(String value) {
        return DATE_PATTERN.matcher(value).matches();
    }
}
