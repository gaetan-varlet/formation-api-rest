---
title: "Les tests unitaires"
---

Un **test unitaire** vérifie une classe ou une méthode métier **en mémoire, sans démarrer le contexte Spring** ni base de données. Les classes de test se placent dans `src/test/java` (dans le même package que la classe testée) et se terminent par **`Test`** pour être exécutées lors de `mvn test`.

*(Comme vu dans [Stratégies de tests](/formation-api-rest/tests/strategie-tests/), privilégiez les tests unitaires purs sur la logique métier sans dépendance technique ; lorsque vous devez isoler une dépendance comme un repository, utilisez **Mockito**.)*

## Test unitaire pur (sans Spring ni mock)

Pour une règle de calcul ou un validateur sans dépendance externe, JUnit 5 et AssertJ suffisent — l'exécution prend quelques millisecondes :

```java title="TarifCalculateurTest.java"
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class TarifCalculateurTest {

    private final TarifCalculateur calculateur = new TarifCalculateur();

    @Test
    void appliqueRemiseDe10PourCentDesSixBouteilles() {
        BigDecimal prixUnitaire = new BigDecimal("20.00");
        int quantite = 6;

        BigDecimal total = calculateur.calculerTotal(prixUnitaire, quantite);

        // 6 × 20 € = 120 €, avec 10 % de remise -> 108 €
        assertThat(total).isEqualByComparingTo("108.00");
    }
}
```

## Isoler une dépendance avec Mockito (`VinServiceTest`)

Lorsque la classe testée collabore avec un `@Repository` ou un autre service, **`@ExtendWith(MockitoExtension.class)`** initialise les doublures déclarées avec `@Mock` et les injecte dans la classe testée via `@InjectMocks` :

```java title="src/test/java/fr/insee/formationapirest/service/VinServiceTest.java"
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VinServiceTest {

    @Mock
    private VinRepository vinRepository;

    @InjectMocks
    private VinService vinService;

    @Test
    void recupererTousLesVins() {
        // Given
        Vin vin1 = creerVin(1L, "Château 1", "Saint-Julien", 10.0);
        Vin vin2 = creerVin(2L, "Château 2", "Pomerol", 25.0);
        when(vinRepository.findAll()).thenReturn(List.of(vin1, vin2));

        // When
        List<Vin> vinsRetournes = vinService.getAll();

        // Then
        assertThat(vinsRetournes).hasSize(2);
        assertThat(vinsRetournes.get(0).getChateau()).isEqualTo("Château 1");
    }

    @Test
    void getByIdLeveExceptionSiVinInconnu() {
        // Given
        when(vinRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> vinService.getById(99L))
            .isInstanceOf(VinInconnuException.class);
    }

    private Vin creerVin(Long id, String chateau, String appellation, double prix) {
        Vin vin = new Vin();
        vin.setId(id);
        vin.setChateau(chateau);
        vin.setAppellation(appellation);
        vin.setPrix(prix);
        return vin;
    }
}
```

:::tip[Bonnes pratiques JUnit 5 & Mockito]
- **Pas de `public` requis :** en JUnit 5, les classes et méthodes `@Test` peuvent rester en visibilité package-private.
- **Stubber dans chaque test plutôt que dans `@BeforeEach` :** cela garde chaque scénario autonome et évite d'avoir à contourner le mode strict par défaut de Mockito avec `@MockitoSettings(strictness = Strictness.LENIENT)` lorsqu'un test n'utilise pas tous les `when(...)` du `setUp()`.
- **Privilégier `AssertJ` (`assertThat`, `assertThatThrownBy`) :** plus lisible et mieux typé que les assertions JUnit classiques.
:::
