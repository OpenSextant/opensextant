package org.mitre.opensextant.desktop.persistence.dao;

import java.io.File;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mitre.opensextant.desktop.persistence.MyBatisConnectionFactory;
import org.mitre.opensextant.desktop.persistence.model.Execution;


public class ExecutionDao {
	
	private SqlSessionFactory sqlSessionFactory; 
	
	public ExecutionDao(File file){
		sqlSessionFactory = MyBatisConnectionFactory.getSqlSessionFactory(file);
		
	}
	
	public void createTable() {
		SqlSession session = sqlSessionFactory.openSession();
		
		try {
			session.update("Execution.createNewTable");
			session.commit();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			session.close();
		}

	}
	
	public void insert(Execution execution){

		SqlSession session = sqlSessionFactory.openSession();
		
		try {
			session.insert("Execution.insert", execution);
			session.commit();
		} finally {
			session.close();
		}
	}

	public Execution selectById(Long id) {
		SqlSession session = sqlSessionFactory.openSession();
		
		try {
			Execution execution = session.selectOne("Execution.getById", id);
			session.commit();
			return execution;
		} finally {
			session.close();
		}
	}
}
