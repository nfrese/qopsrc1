package at.qop.qoplib.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="q_config")
public class Config {
	
	@Id
	public String k;

	public String value;
	
}
