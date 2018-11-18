package varlet.gaetan.cave.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.log4j.Log4j2;
import varlet.gaetan.cave.dao.VinDao;
import varlet.gaetan.cave.model.Vin;

@RestController
@Log4j2
public class VinController {
	
	@Autowired
	private VinDao vinDao;

	
	@RequestMapping(value = "/vin", method = RequestMethod.GET)
	public List<Vin> getAllVin() {
		return vinDao.findAll();
	}
	
	@RequestMapping(value = "/vin/{id}", method = RequestMethod.GET)
	public Vin getVin(@PathVariable Integer id) {
		return vinDao.findById(id).orElse(null);
	}
	
	@RequestMapping(value = "/vin/add/{chateau}", method = RequestMethod.GET)
	public void addVin(@PathVariable String chateau) {
		Vin vin = Vin.builder().chateau(chateau).build();
		vinDao.save(vin);
	}

}
