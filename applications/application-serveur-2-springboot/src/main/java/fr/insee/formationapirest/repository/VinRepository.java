package fr.insee.formationapirest.repository;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.querydsl.binding.SingleValueBinding;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;

import fr.insee.formationapirest.model.QVin;
import fr.insee.formationapirest.model.Vin;

@Repository
public interface VinRepository extends JpaRepository<Vin, Integer>, QuerydslPredicateExecutor<Vin>, QuerydslBinderCustomizer<QVin> {
	
	@Override
   default void customize(QuerydslBindings bindings, QVin vin) {
       // Make case-insensitive 'like' filter for all string properties 
       bindings.bind(String.class).first((SingleValueBinding<StringPath, String>) StringExpression::containsIgnoreCase);
       bindings.bind(vin.prix).all((path, value) -> {
          Iterator<? extends Double> it = value.iterator();
          Double from = it.next();
          if (value.size() >= 2) {
              Double to = it.next();
              return Optional.of(path.between(from, to)); // between
          } else {
              return Optional.of(path.goe(from)); // greater or equal
          }
      });
       bindings.excluding(vin.id);
   }
	
	List<Vin> findByAppellation(String appellation);
	
}