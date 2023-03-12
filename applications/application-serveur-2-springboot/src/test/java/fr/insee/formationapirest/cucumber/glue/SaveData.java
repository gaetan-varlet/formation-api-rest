package fr.insee.formationapirest.cucumber.glue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;
import com.ninja_squad.dbsetup.operation.Insert;
import com.ninja_squad.dbsetup.operation.Insert.RowBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class SaveData {

    private final DataSource dataSource;

    public void insertInTable(String nomTable, List<Map<String, String>> data) {
        log.info("Chargement de la table {}", nomTable);
        if (nomTable == null) {
            throw new IllegalArgumentException("Il faut renseigner la table à peupler");
        }
        for (Map<String, String> ligne : data) {
            if (ligne.entrySet().stream().noneMatch(e -> !StringUtils.isEmpty(e.getValue()))) {
                // Toutes les colonnes de la ligne sont vides, on n'insère rien
                return;
            }
            RowBuilder rowBuilder = Operations.insertInto(nomTable).row();
            for (Entry<String, String> attribut : ligne.entrySet()) {
                if (!StringUtils.isEmpty(attribut.getValue())) {
                    if (Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}").matcher(attribut.getValue())
                            .matches()) {
                        rowBuilder = rowBuilder.column(attribut.getKey(), LocalDateTime.parse(attribut.getValue(),
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    } else if (Pattern.compile("\\d{4}-\\d{2}-\\d{2}").matcher(attribut.getValue()).matches()) {
                        rowBuilder = rowBuilder.column(attribut.getKey(),
                                LocalDate.parse(attribut.getValue(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                    } else {
                        rowBuilder = rowBuilder.column(attribut.getKey(), attribut.getValue());
                    }
                }
            }
            Insert insert = rowBuilder.end().build();
            DbSetup dbSetup = new DbSetup(new DataSourceDestination(dataSource), insert);
            dbSetup.launch();
        }
    }
}
