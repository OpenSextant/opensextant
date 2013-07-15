package org.mitre.opensextant.processing.output.os;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.WordUtils;
import org.mitre.opensextant.desktop.persistence.MyBatisConnectionFactory;
import org.mitre.opensextant.desktop.persistence.dao.ExecutionDao;
import org.mitre.opensextant.desktop.persistence.dao.ResultDao;
import org.mitre.opensextant.desktop.persistence.model.Execution;
import org.mitre.opensextant.desktop.persistence.model.Result;
import org.opensextant.giscore.events.Feature;
import org.opensextant.giscore.events.IGISObject;
import org.opensextant.giscore.events.Row;
import org.opensextant.giscore.events.Schema;
import org.opensextant.giscore.events.SimpleField;
import org.opensextant.giscore.output.IGISOutputStream;
import org.opensextant.giscore.output.StreamVisitorBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import org.apache.poi.ss.usermodel.Row;

public class SQLITEGISOutputStream extends StreamVisitorBase implements IGISOutputStream {

    private static Logger log = LoggerFactory.getLogger(SQLITEGISOutputStream.class);

    private Schema schema;
    private Execution execution;
    private int counter = 0;
    
    private ResultDao resultDao;
    private ExecutionDao executionDao;
    
    private List<Result> batchOfResults = new ArrayList<Result>();

    public SQLITEGISOutputStream(File dbFile) {

    	resultDao = new ResultDao(dbFile);
    	executionDao = new ExecutionDao(dbFile);
    	
    	executionDao.createTable();
    	resultDao.createTable();

    }

    @Override
    public void close() throws IOException {
        saveResultsBatch();
    }

    private void saveResultsBatch() {
		resultDao.insertBatch(batchOfResults, MyBatisConnectionFactory.BATCH_SIZE);
		batchOfResults.clear();
	}

	@Override
    public void write(IGISObject gisData) {
        gisData.accept(this);
    }

    @Override
    public void visit(Row row) {
        if (row == null) {
            throw new IllegalArgumentException("row should never be null");
        }

        if (counter % MyBatisConnectionFactory.BATCH_SIZE == 0 && counter > 0) {
            log.info("Committing batch");
            saveResultsBatch();
        }
        counter ++;
        
        URI schemauri = row.getSchema();
        if (schemauri == null || !schemauri.equals(schema.getId())) {
            throw new RuntimeException("Row schema doesn't match schema given");
        }

        Result result = createResult(schema.getFields(), row);
        
        batchOfResults.add(result);

    }

    private Result createResult(Collection<SimpleField> fields, Row row) {
        Result result = new Result();

        for (SimpleField field : fields) {
            // skip id... that's auto incremented
            if ("id".equals(field.getName())) {
                continue;
            }
                
            try {
                Object value = row.getData(field);
                if (value == null)
                    continue;
                String methodName = "set" + WordUtils.capitalizeFully(field.getName(), new char[] { '_' }).replaceAll("_", "");
                Method method = Result.class.getMethod(methodName, value.getClass());
                method.invoke(result, value);

            } catch (Exception e) {
                log.error("Error writing data fro SQLITE field: " + field, e);
            }
        }

        result.setExecutionId(execution.getId());  

        return result;
    }

    @Override
    public void visit(Feature feature) {
        visit((Row) feature);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.mitre.giscore.output.StreamVisitorBase#visit(org.mitre.giscore.events
     * .Schema)
     */
    @Override
    public void visit(Schema s) {
        if (schema != null) {
            throw new RuntimeException("Can't set the schema after a schema has already been set");
        }
        schema = s;
        this.execution = new Execution();
        this.execution.setTimestamp(new Date());

        executionDao.insert(execution);

    }

}
