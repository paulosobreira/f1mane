package br.nnpe;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;

/**
 * @author Paulo Sobreira [sowbreira@gmail.com]
 * @author Rafael Carneiro [rafaelcarneirob@gmail.com]
 */
public class HibernateUtil {

	private static SessionFactory sessionFactory;

	static {

		if (sessionFactory == null) {

			try {
				sessionFactory = new AnnotationConfiguration().configure()
						.buildSessionFactory();
			} catch (Throwable e) {
				throw new ExceptionInInitializerError(e);
			}

		}

	}

	public static SessionFactory getSessionFactory() {
		if (sessionFactory != null && Logger.novaSession) {
			sessionFactory.close();
		}
		if (Logger.novaSession) {
			try {
				sessionFactory = new AnnotationConfiguration().configure()
						.buildSessionFactory();
				Logger.novaSession = false;
			} catch (Throwable e) {
				Logger.logarExept(e);
			}
		}
		return sessionFactory;
	}

}
