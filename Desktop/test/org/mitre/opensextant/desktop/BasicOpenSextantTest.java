package org.mitre.opensextant.desktop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gate.Annotation;
import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.Utils;
import gate.creole.ResourceInstantiationException;
import junit.framework.TestCase;

import org.apache.commons.collections.ListUtils;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.Before;
import org.junit.Test;
import org.mitre.opensextant.apps.Config;
import org.mitre.opensextant.desktop.executor.opensextant.ext.extract.OSExtractor;
import org.mitre.opensextant.processing.ProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicOpenSextantTest extends TestCase {

	static {
		System.setProperty("osd.log.root", "./");
	}

	private static Logger log;

    @Before  
    public void setUp() throws Exception {  
		DOMConfigurator.configure(Main.class.getResource("/log4j_config.xml"));
		log = LoggerFactory.getLogger(BasicOpenSextantTest.class);
    }
    
	@Test
	public void testGenericExtractor() throws ProcessingException, ResourceInstantiationException {
		
		Config.GATE_HOME = System.getProperty("gate.home");
		Config.SOLR_HOME = System.getProperty("solr.solr.home");
		Config.DEFAULT_GAPP = "OpenSextant_GeneralPurpose.gapp";
		
		OSExtractor extractor = new OSExtractor();
		extractor.initialize();
		
		Document document = Factory.newDocument("The ticket costs $100 USD in Boston and in Providence.  One hundred dollars is a lot of money, but John Smith would really like to buy the ticket for 10:15AM on Saturday June 15th, 2013. That date is 06/12/2013.  He should go to the ip 129.68.234.92 to buy the ticket on line.  Look for my car when you get there.  The license plate number is 92A-12B and the vin is 1HGBH41JXMN109186.");
		Corpus corpus = extractor.extractEntity(document);
		for (int i = 0; i < corpus.size(); i++) {
			Document doc = corpus.get(i);
			Set<String> types = new HashSet<String>();
			for (Annotation annotation : doc.getAnnotations()) {
				if (annotation.getType().equals("NounPhrase")) {
					log.info(annotation.getType() + " : " + Utils.cleanStringFor(doc, annotation) + " : " + annotation.getFeatures().get("EntityType"));
					String entityType = (String)annotation.getFeatures().get("EntityType");
					if (entityType != null) {
						types.add(entityType);
					}
				}
				
			}
			List<String> list = new ArrayList<String>(types);
			Collections.sort(list);
			for (String type : list) {
				System.out.println(type);
			}
			
		}
	}



}
