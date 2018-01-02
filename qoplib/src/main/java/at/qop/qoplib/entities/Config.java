package at.qop.qoplib.entities;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Config {
	
	@Id
	public String k;

	public String value;
	
}
