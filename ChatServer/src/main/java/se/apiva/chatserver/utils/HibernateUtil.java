package se.apiva.chatserver.utils;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

    private static SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            if(sessionFactory == null) {
                Configuration configuration = new Configuration().configure("hibernate.cfg.xml");

                setConfigFromEnvironment(configuration, "DB_DRIVER", "hibernate.connection.driver_class");
                setConfigFromEnvironment(configuration, "DB_DIALECT", "hibernate.dialect");
                setConfigFromEnvironment(configuration, "DB_URL", "hibernate.connection.url");
                setConfigFromEnvironment(configuration, "DB_USERNAME", "hibernate.connection.username");
                setConfigFromEnvironment(configuration, "DB_PASSWORD", "hibernate.connection.password");

                sessionFactory = configuration.buildSessionFactory();
            }
            return sessionFactory;
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    private static void setConfigFromEnvironment(Configuration configuration, String environmentVariable, String configParameter) {
        if(System.getenv(environmentVariable) != null) {
            System.out.println("Overriding " + environmentVariable + ": " +  System.getenv(environmentVariable));
            configuration.setProperty(
                    configParameter,
                    System.getenv(environmentVariable)
            );
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        sessionFactory.close();
    }

}
