package org.mitre.opensextant.desktop.executor.opensextant.ext;

import gate.Corpus;
import gate.CorpusController;
import gate.Document;
import gate.FeatureMap;
import gate.Resource;
import gate.creole.ConditionalSerialAnalyserController;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.event.ProgressListener;
import gate.event.StatusListener;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.mitre.opensextant.desktop.ui.table.OSRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OSDCorpusControllerWrapper implements CorpusController {

	private ConditionalSerialAnalyserController wrapped;
	private OSRow row;
	private int completed = 0;
	private boolean canceled = false;
	private static Logger log = LoggerFactory.getLogger(OSDCorpusControllerWrapper.class);

	public OSDCorpusControllerWrapper(final ConditionalSerialAnalyserController wrapped, final OSRow row) {
		this.wrapped = wrapped;
		this.row = row;
		wrapped.addProgressListener(new ProgressListener() {
			
			@Override
			public void progressChanged(int progress) {
				int calculatedProgress = progress;
				if (row.hasChildren()) {
					File docFile = new File((String)wrapped.getDocument().getFeatures().get(OSDOpenSextantRunner.ORIGINAL_FILE));
					OSRow child = row.getChildForInputFile(docFile);
					
					if (child.getPercent() != progress) {
						calculatedProgress = row.getPercent() - child.getPercent()/wrapped.getCorpus().size();
						child.setProgress(progress, OSRow.STATUS.PROCESSING);
						calculatedProgress += child.getPercent()/wrapped.getCorpus().size();
					} else {
						calculatedProgress = row.getPercent();
					}
				} 
				if (row.getPercent() != calculatedProgress) {
					row.setProgress(calculatedProgress, OSRow.STATUS.PROCESSING, completed);
				}
			}

			@Override
			public void processFinished() {
				// this does not seem to fire properly
			}
			
		});
	}
	
	public void cancelExecution() {
		canceled = true;
	}

	@Override
	public void execute() throws ExecutionException {
		
		Corpus corpus = wrapped.getCorpus();
		
		for (int indexx = 0; (indexx < corpus.size()) && !canceled; indexx++) {
			Document doc = corpus.get(indexx);
			wrapped.setDocument(doc);
			wrapped.execute();
			
			if (row.hasChildren()) {
				File docFile = new File((String)doc.getFeatures().get(OSDOpenSextantRunner.ORIGINAL_FILE));
				OSRow child = row.getChildForInputFile(docFile);
				child.setProgress(100, OSRow.STATUS.COMPLETE);
			}
			
			completed++;
			row.setProgress(row.getPercent(), OSRow.STATUS.PROCESSING, completed);

		}

	}

	@Override
	public Collection getPRs() {
		return wrapped.getPRs();
	}

	@Override
	public void setPRs(Collection arg0) {
		wrapped.setPRs(arg0);
	}

	@Override
	public void cleanup() {
		wrapped.cleanup();
	}

	@Override
	public Object getParameterValue(String paramaterName) throws ResourceInstantiationException {
		return wrapped.getParameterValue(paramaterName);
	}

	@Override
	public Resource init() throws ResourceInstantiationException {
		return wrapped.init();
	}

	@Override
	public void setParameterValue(String paramaterName, Object parameterValue) throws ResourceInstantiationException {
		wrapped.setParameterValue(paramaterName, parameterValue);

	}

	@Override
	public void setParameterValues(FeatureMap parameters) throws ResourceInstantiationException {
		wrapped.setParameterValues(parameters);

	}

	@Override
	public FeatureMap getFeatures() {
		return wrapped.getFeatures();
	}

	@Override
	public void setFeatures(FeatureMap features) {
		wrapped.setFeatures(features);
	}

	@Override
	public String getName() {
		return wrapped.getName();
	}

	@Override
	public void setName(String name) {
		wrapped.setName(name);
	}

	@Override
	public void interrupt() {
		wrapped.interrupt();
	}

	@Override
	public boolean isInterrupted() {
		return wrapped.isInterrupted();
	}

	@Override
	public Corpus getCorpus() {
		return wrapped.getCorpus();
	}

	@Override
	public void setCorpus(Corpus corpus) {
		wrapped.setCorpus(corpus);
	}

}
