package org.mitre.opensextant.processing.output;

import gate.Corpus;
import gate.Document;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.mitre.opensextant.desktop.ui.table.OSRow;
import org.mitre.opensextant.desktop.util.JobStatistics;
import org.mitre.opensextant.processing.Geocoding;

import org.mitre.opensextant.processing.GeocodingResult;
import org.mitre.opensextant.processing.ProcessingException;
import org.mitre.opensextant.processing.output.AbstractFormatter;

public class MultiFormatter extends AbstractFormatter {

	List<AbstractFormatter> children = new ArrayList<AbstractFormatter>();
	private OSRow row;
    
    public MultiFormatter(OSRow row) throws ProcessingException {
        super();
        this.row = row;
    }

    
	public void addChild(AbstractFormatter child) {
		children.add(child);
	}
	
	@Override
	public void start(String nm) throws ProcessingException {
		for (AbstractFormatter child : children) {
			child.start(nm);
		}
	}

	@Override
	public void finish() {
		for (AbstractFormatter child : children) {
			child.finish();
		}
	}

	@Override
	protected void createOutputStreams() throws Exception {
		for (AbstractFormatter child : children) {
			Method m = child.getClass().getDeclaredMethod("createOutputStreams", null);
			m.setAccessible(true);
			m.invoke(child, null);
		}
	}

	@Override
	protected void closeOutputStreams() throws Exception {
		for (AbstractFormatter child : children) {
			Method m = child.getClass().getDeclaredMethod("closeOutputStreams", null);
			m.setAccessible(true);
			m.invoke(child, null);
		}
	}

	@Override
	public void writeOutput(Corpus corpus) throws Exception {
	//	for (AbstractFormatter child : children) {
	//		child.writeOutput(corpus);
	//	}
        for(Document doc : corpus) {
            writeRowsFor(doc);
        }
	}

	@Override
	public void writeRowsFor(Document doc) throws IOException {
		for (AbstractFormatter child : children) {
			child.writeRowsFor(doc);
		}
	}

	@Override
	public void writeGeocodingResult(GeocodingResult rowdata) {
		for (AbstractFormatter child : children) {
			child.writeGeocodingResult(rowdata);
		}
        for (Geocoding g : rowdata.geocodes) {
            JobStatistics s = row.getStatistics();
            if(g.is_coordinate) s.addGeo(g, JobStatistics.COORDINATE);
            if(g.is_place) s.addGeo(g, JobStatistics.PLACE);
            if(g.is_country) s.addGeo(g, JobStatistics.COUNTRY);;
            s.incrementObjCount();
        }
	}

}
