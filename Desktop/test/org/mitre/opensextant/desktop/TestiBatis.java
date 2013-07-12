package org.mitre.opensextant.desktop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mitre.opensextant.desktop.persistence.dao.ExecutionDao;
import org.mitre.opensextant.desktop.persistence.dao.ResultDao;
import org.mitre.opensextant.desktop.persistence.model.Execution;
import org.mitre.opensextant.desktop.persistence.model.Result;


public class TestiBatis {
	
	private static ExecutionDao executionDao;
	private static ResultDao resultDao;

	@BeforeClass
	public static  void runBeforeClass() {
		
		File file = null;
		try {
			file = File.createTempFile("testdb", "db");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		executionDao = new ExecutionDao(file);
		executionDao.createTable();
		
		resultDao = new ResultDao(file);
		resultDao.createTable();
	}

	@AfterClass
	public static void runAfterClass() {
		executionDao = null;
	}

	@Test
	public void testInsertExecution() {
		
		Execution execution = new Execution();
		
		execution.setTimestamp(new Date());
		
		executionDao.insert(execution);
		
		assertNotNull(execution.getId());
		assertNotNull(execution.getTimestamp());
		
		Execution saved = executionDao.selectById(execution.getId());
		
		
	}

	@Test
	public void testInsertResult() {
		
		Execution execution = new Execution();
		
		execution.setTimestamp(new Date());
		
		executionDao.insert(execution);
		
		Result result = new Result();
		result.setPlacename("Boston");
		result.setLat(44.2);
		result.setLon(74.1);
		result.setExecutionId(execution.getId());
		
		resultDao.insert(result);
		
		
		
	}

	@Test
	public void testInsertResults() {
		
		Execution execution = new Execution();
		
		execution.setTimestamp(new Date());
		
		executionDao.insert(execution);
		
		List<Result> results = new ArrayList<Result>();
		for (int i =0; i < 80; i++) {
			Result result = new Result();
			result.setPlacename("Boston: " + i);
			result.setLat(44.2 + i);
			result.setLon(74.1);
			result.setExecutionId(execution.getId());
			results.add(result);
		}
		
		resultDao.insertBatch(results, 50);
		
		
		
	}


}
