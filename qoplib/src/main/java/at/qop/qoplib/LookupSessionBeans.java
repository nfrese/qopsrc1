/* 
 * Copyright (C) 2018 Norbert Frese
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
*/

package at.qop.qoplib;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import at.qop.qoplib.domains.AbstractDomain;
import at.qop.qoplib.domains.AddressDomain;
import at.qop.qoplib.domains.ConfigDomain;
import at.qop.qoplib.domains.GenericDomain;
import at.qop.qoplib.domains.IAddressDomain;
import at.qop.qoplib.domains.IConfigDomain;
import at.qop.qoplib.domains.IGenericDomain;
import at.qop.qoplib.domains.IProfileDomain;
import at.qop.qoplib.domains.ProfileDomain;

public class LookupSessionBeans {
	
	public static boolean inTestContext = false;
	public static String jdbcTestConnectionUrl;
	public static String jdbcTestConnectionUserName;
	public static String jdbcTestConnectionPassword;	

	public static IConfigDomain configDomain() {
		return lookupDomain(ConfigDomain.class, IConfigDomain.class);
	}

	public static IAddressDomain addressDomain() {
		return lookupDomain(AddressDomain.class, IAddressDomain.class);
	}

	public static IProfileDomain profileDomain() {
		return lookupDomain(ProfileDomain.class, IProfileDomain.class);
	}
	
	public static IGenericDomain genericDomain() {
		return lookupDomain(GenericDomain.class, IGenericDomain.class);
	}	
	
	@SuppressWarnings("unchecked")
	public static <B extends AbstractDomain,I> I lookupDomain(Class<B> beanClass, Class<I> beanInterface) {
		if (inTestContext)
		{
			final Class<?> thisClass = LookupSessionBeans.class;
			return (I) Proxy.newProxyInstance(thisClass.getClassLoader(), new Class<?>[] {beanInterface}, new InvocationHandler() {

				@Override
				public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
					Map<String,String> map = new TreeMap<>();
					map.put("hibernate.connection.url", jdbcTestConnectionUrl);
					map.put("hibernate.connection.username", jdbcTestConnectionUserName);
					map.put("hibernate.connection.password", jdbcTestConnectionPassword);
					
//		            <property name="hibernate.connection.url" value="jdbc:derby://localhost:1527/EmpServDB;create=true"/>
//		            <property name="hibernate.connection.username" value="APP"/>
//		            <property name="hibernate.connection.password" value="APP"/>
					
					
					EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory( "qopTestPU" , map);
					EntityManager em = entityManagerFactory.createEntityManager();
					
					EntityTransaction tx = em.getTransaction();
					if (!tx.isActive()) tx.begin();
					try {
						
						String interfaceName = beanClass.getSimpleName();
//						String className = interfaceName.replaceAll("^I", "");
//						Class<B> beanClazz = (Class<B>) thisClass.forName("at.qop.qoplib.domains." + className);
						B inst = beanClass.newInstance();
						inst.injectEm(em);
						Object result = null;
						
						for (Method m : beanClass.getMethods())
						{
							if (m.getName().equals(method.getName()))
							{
								result = m.invoke(inst, args);
							}
						}
						tx.commit();
						return result;
					} catch (Exception ex)
					{
						tx.rollback();
						throw new RuntimeException(ex);
					}
				}
			});
		}
		else
		{

			String appName = Utils.getEarName();

			try {
				Properties props = new Properties();
				props.put("java.naming.factory.url.pkgs","org.jboss.ejb.client.naming");
				InitialContext context = new InitialContext(props);

				String beanName = beanClass.getSimpleName();        	 
				String name = "java:global/" + appName + "/qoplib/" + beanName;
				@SuppressWarnings("unchecked")
				I bean = (I)context.lookup(name);
				return bean;
			} catch (NamingException e)
			{
				throw new RuntimeException(e);
			}
		}
	}

}
