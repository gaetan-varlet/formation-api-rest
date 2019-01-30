package fr.insee.formationapirest.repository;

import java.util.List;

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
   }
	
	List<Vin> findByAppellation(String appellation);
	
}