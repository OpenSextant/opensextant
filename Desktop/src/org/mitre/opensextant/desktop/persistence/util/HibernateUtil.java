package org.mitre.opensextant.desktop.persistence.util;

import java.io.File;
import java.net.URL;
import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.mitre.opensextant.desktop.persistence.model.Execution;
import org.mitre.opensextant.desktop.persistence.model.Result;

public class HibernateUtil {
    
    public static final int BATCH_SIZE = 500;

    public static SessionFactory getSessionFactory(File dbFile) {
        final Properties properties = new Properties();
        properties.put("hibernate.show_sql",false);
        properties.put("hibernate.format_sql",false);
        properties.put("hibernate.jdbc.batch_size",BATCH_SIZE);
        properties.put("hibernate.dialect","org.mitre.opensextant.desktop.persistence.dialect.SQLiteDialect");
        properties.put("hibernate.connection.driver_class","org.sqlite.JDBC");
        properties.put("hibernate.connection.username","");
        properties.put("hibernate.connection.password","");
        properties.put("hibernate.hbm2ddl.auto","update");
        properties.put("hibernate.connection.url", "jdbc:sqlite:"+dbFile.getAbsolutePath());

        
        SchemaExport export = new SchemaExport(new Configuration().setProperties(properties));
        export.create(false, true);

        SessionFactory sessionFactory = new Configuration().setProperties(properties)
                .addPackage("org.mitre.opensextant.desktop.persistence.model")
                .addAnnotatedClass(Result.class)
                .addAnnotatedClass(Execution.class)
                .buildSessionFactory();
        
        return sessionFactory;
    }

}
