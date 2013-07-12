package org.mitre.opensextant.desktop.persistence.dao;

import java.io.File;
import java.util.List;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mitre.opensextant.desktop.persistence.MyBatisConnectionFactory;
import org.mitre.opensextant.desktop.persistence.model.Result;


public class ResultDao {
	
	private SqlSessionFactory sqlSessionFactory; 
	
	public ResultDao(File file){
		sqlSessionFactory = MyBatisConnectionFactory.getSqlSessionFactory(file);
		
	}
	
	public void createTable() {
		SqlSession session = sqlSessionFactory.openSession();
		
		try {
			session.update("Result.createNewTable");
			session.commit();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			session.close();
		}

	}
	
	public void insertBatch(List<Result> results, int batchSize) {

		
		
		SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH);
		
		
		try {
			int inserted = 0;
			for (Result result : results) {
				session.insert("Result.insert", result);
				inserted++;
				if (inserted % batchSize == 0) {
					session.flushStatements();
				}
			}
			
			session.flushStatements();
			session.commit();
		} finally {
			session.close();
		}
	}
	
	public void insert(Result result){

		SqlSession session = sqlSessionFactory.openSession();
		
		try {
			session.insert("Result.insert", result);
			session.commit();
		} finally {
			session.close();
		}
	}

	public Result selectById(Long id) {
		SqlSession session = sqlSessionFactory.openSession();
		
		try {
			Result result = session.selectOne("Result.getById", id);
			session.commit();
			return result;
		} finally {
			session.close();
		}
	}
}
