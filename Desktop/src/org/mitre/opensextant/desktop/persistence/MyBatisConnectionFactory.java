package org.mitre.opensextant.desktop.persistence;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;

import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class MyBatisConnectionFactory {

    public static final int BATCH_SIZE = 500;

	public static SqlSessionFactory getSqlSessionFactory(File file) {

		SqlSessionFactory sqlSessionFactory = null;
		try {

			String resource = "org/mitre/opensextant/desktop/persistence/SqlMapConfig.xml";
			InputStream stream = Resources.getResourceAsStream(resource);
			
			Properties properties = new Properties();
			properties.put("dbfile", file.getAbsolutePath());
			
			if (sqlSessionFactory == null) {
				sqlSessionFactory = new SqlSessionFactoryBuilder().build(stream, properties);
			}
			
		}

		catch (FileNotFoundException fileNotFoundException) {
			fileNotFoundException.printStackTrace();
		}
		catch (IOException iOException) {
			iOException.printStackTrace();
		}

		return sqlSessionFactory;
	}

}
