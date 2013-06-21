package org.mitre.opensextant.desktop;


import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.log4j.xml.DOMConfigurator;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.Before;
import org.mitre.opensextant.desktop.persistence.model.Execution;
import org.mitre.opensextant.desktop.persistence.model.Result;
import org.mitre.opensextant.desktop.persistence.util.HibernateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestSaveResult extends TestCase {
    static {
        System.setProperty("osd.log.root", "./");
    }
    private static Logger log;
    
    @Before
    public void setUp() throws Exception {
        DOMConfigurator.configure(Main.class.getResource("/log4j_config.xml"));
        log = LoggerFactory.getLogger(TestSaveResult.class);
    }
	
	public void testSave(){
	    Execution execution = new Execution();
	    Result result = new Result();
//	    result.setExecution(execution);
//	    result.setPlacename("Boston");
//        result.setProvince("MA");
	    execution.setTimestamp(new Date());
	    
	    Set<Result> results = new HashSet<Result>();
	    results.add(result);
	    execution.setResults(results);
	    
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory(new File("/tmp/OpenSextant.db"));
        Session session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		session.save(execution);
		tx.commit();
		session.close();
		sessionFactory.close();
	}
	
	public void testList() {
		try {
	        SessionFactory sessionFactory = HibernateUtil.getSessionFactory(new File("/tmp/OpenSextant.db"));
	        Session session = sessionFactory.openSession();
			Query q = session.createQuery("from Execution");
			List<Execution> executions = q.list();
			log.info("Exectuions.size = " + executions.size());
			for (Execution execution : executions) {
			    log.info("EXECUTION: " + execution.getId());
	            for (Result result : execution.getResults()) {
	                log.info(result.toString());
	            }
			}
	        session.close();
	        sessionFactory.close();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
