package varlet.gaetan.cave.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString

@Entity
public class Vin {
	
	@Id
	@GeneratedValue (strategy = GenerationType.AUTO)
	private Integer id;
	
	private String chateau;
	private String appellation;
	private Double prix;

}
