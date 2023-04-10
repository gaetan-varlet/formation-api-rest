package fr.insee.formationapirest.unittests.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import fr.insee.formationapirest.model.Vin;
import fr.insee.formationapirest.repository.VinRepository;

public class VinRepoMock implements VinRepository {

    List<Vin> vins = new ArrayList<>();

    @Override
    public List<Vin> findAll() {
        return this.vins;
    }

    @Override
    public List<Vin> findByAppellation(String app) {
        return this.vins.stream().filter(v -> v.getAppellation().equals(app)).toList();
    }

    public void clear() {
        vins = new ArrayList<>();
    }

    @Override
    public boolean existsById(Integer id) {
        throw new UnsupportedOperationException("Unimplemented method 'existsById'");
    }

    @Override
    public Optional<Vin> findById(Integer id) {
        throw new UnsupportedOperationException("Unimplemented method 'findById'");
    }

    @Override
    public Vin save(Vin vin) {
        vins.add(vin);
        return vin;
    }

    @Override
    public void deleteById(Integer id) {
        throw new UnsupportedOperationException("Unimplemented method 'deleteById'");
    }

    @Override
    public Page<Vin> findAll(Pageable p) {
        throw new UnsupportedOperationException("Unimplemented method 'findAll'");
    }

    @Override
    public List<String> getListeAppellationJpa() {
        throw new UnsupportedOperationException("Unimplemented method 'getListeAppellationJpa'");
    }

}
