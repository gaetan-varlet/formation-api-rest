package varlet.gaetan.cave;

import org.junit.Test;

import lombok.extern.log4j.Log4j2;
import varlet.gaetan.cave.model.Vin;

@Log4j2
public class Main {
	
	@Test
	public void test(){
		Vin vin = new Vin();
		vin.setChateau("Château Margaux");
		vin.setAppellation("Margaux");
		vin.setPrix(500.0);
		log.info(vin);
	}

}
