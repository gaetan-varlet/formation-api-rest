package varlet.gaetan.cave.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import varlet.gaetan.cave.model.Vin;

@Repository
public interface VinDao extends JpaRepository<Vin, Integer> {

}
