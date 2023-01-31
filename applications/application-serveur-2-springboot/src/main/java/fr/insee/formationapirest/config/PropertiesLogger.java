package fr.insee.formationapirest.config;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Listener Spring : se déclenche au démarrage de l'application Spring boot sur l'évènvement
 * ApplicationEnvironmentPreparedEvent.
 *
 * <p>Permet d'afficher les props dans les logs
 */
@Slf4j
public class PropertiesLogger implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private static final Set<String> motsCaches =
            Set.of("password", "pwd", "jeton", "token", "secret");

    @Override
    public void onApplicationEvent(@NonNull ApplicationEnvironmentPreparedEvent event) {
        Environment environment = event.getEnvironment();

        log.info("============================================================================");
        log.info("                            Valeurs des properties                          ");
        log.info("============================================================================");

        ((AbstractEnvironment) environment)
                .getPropertySources().stream()
                        .map(
                                propertySource -> {
                                    if (propertySource instanceof EnumerablePropertySource) {
                                        return ((EnumerablePropertySource<?>) propertySource)
                                                .getPropertyNames();
                                    } else {
                                        log.warn(
                                                propertySource
                                                        + " n'est pas EnumerablePropertySource :"
                                                        + " impossible à lister");
                                        return new String[] {};
                                    }
                                })
                        .flatMap(Arrays::stream)
                        .distinct()
                        .filter(Objects::nonNull)
                        .forEach(
                                key ->
                                        log.info(
                                                key
                                                        + " = "
                                                        + resoutValeurAvecMasquePwd(
                                                                key, environment)));
        log.info("==========================================================================");
    }

    private static Object resoutValeurAvecMasquePwd(String key, Environment environment) {
        if (StringUtils.isNotBlank(environment.getProperty(key))
                && motsCaches.stream()
                        .map(String::toLowerCase)
                        .anyMatch(key.toLowerCase()::contains)) {
            return "******";
        }
        return environment.getProperty(key);
    }
}
