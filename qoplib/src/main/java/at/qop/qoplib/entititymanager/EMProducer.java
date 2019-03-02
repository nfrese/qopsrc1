package at.qop.qoplib.entititymanager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;


@Stateless
public class EMProducer {

	public EMProducer() {
		System.out.println("*****************************************!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
	}
	
	@Produces @RequestScoped
	public EntityManager createEntityMnager()  {
		return fetchEntityManagerFactory().createEntityManager(/* SynchronizationType.SYNCHRONIZED */);
	}

	public void closeEntityManager(@Disposes EntityManager entityManager) {
		if (entityManager.isOpen()) {
			entityManager.close();
		}
	}

	private ConcurrentHashMap<String, EntityManagerFactory> factoryMap = new ConcurrentHashMap<>();

	private EntityManagerFactory fetchEntityManagerFactory() {

		String url = "java:/qopDS";

		// Falls nötig, Map-Eintrag für URL erstellen
		this.factoryMap.computeIfAbsent(url, u -> {
			Map<String, String> prop = new HashMap<>();

			//prop.put("provider", "org.hibernate.jpa.HibernatePersistenceProvider");
			prop.put("hibernate.hbm2ddl.auto", "update");
            prop.put("hibernate.dialect", "org.hibernate.spatial.dialect.postgis.PostgisDialect" );
			prop.put("javax.persistence.jdbc.driver", "org.postgresql.Driver" );
            prop.put("hibernate.show_sql", "true" );
            prop.put("hibernate.cache.default_cache_concurrency_strategy"
               , "nonstrict-read-write" );
            prop.put("hibernate.max_fetch_depth", "1" );

			return Persistence.createEntityManagerFactory("qopPU", prop);
		});

		return this.factoryMap.get(url);
	}

}
