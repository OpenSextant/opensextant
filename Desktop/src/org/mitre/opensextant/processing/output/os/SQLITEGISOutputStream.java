package org.mitre.opensextant.processing.output.os;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.lang.WordUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.mitre.giscore.events.Feature;
import org.mitre.giscore.events.IGISObject;
import org.mitre.giscore.events.Row;
import org.mitre.giscore.events.Schema;
import org.mitre.giscore.events.SimpleField;
import org.mitre.giscore.output.IGISOutputStream;
import org.mitre.giscore.output.StreamVisitorBase;
import org.mitre.opensextant.desktop.persistence.model.Execution;
import org.mitre.opensextant.desktop.persistence.model.Result;
import org.mitre.opensextant.desktop.persistence.util.HibernateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import org.apache.poi.ss.usermodel.Row;

public class SQLITEGISOutputStream extends StreamVisitorBase implements IGISOutputStream {

    private static Logger log = LoggerFactory.getLogger(SQLITEGISOutputStream.class);

    private Schema schema;
    private Session session;
    private SessionFactory sessionFactory;
    private Execution execution;
    private int counter = 0;

    private Transaction transaction;

    public SQLITEGISOutputStream(File dbFile) {


        this.sessionFactory = HibernateUtil.getSessionFactory(dbFile);
        this.session = sessionFactory.openSession();

    }

    @Override
    public void close() throws IOException {
        this.transaction.commit();
        session.close();
        sessionFactory.close();
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

        if (counter % HibernateUtil.BATCH_SIZE == 0) {
            session.flush();
            if (counter > 0) {
                log.info("Committing");
                this.transaction.commit();
            }
            this.transaction = session.beginTransaction();
        }
        counter ++;
        
        URI schemauri = row.getSchema();
        if (schemauri == null || !schemauri.equals(schema.getId())) {
            throw new RuntimeException("Row schema doesn't match schema given");
        }

        Result result = createResult(schema.getFields(), row);

        session.save(result);

        // if (schema != null && row.getSchema() != null) {
        // URI schemauri = row.getSchema();
        // if (schemauri == null || !schemauri.equals(schema.getId())) {
        // throw new RuntimeException("Row schema doesn't match schema given");
        // }
        // try {
        // for (String fieldname : schema.getKeys()) {
        // SimpleField field = schema.get(fieldname);
        // // addCell(index, row, field, xlsRow);
        // }
        // } catch (IOException e) {
        // throw new RuntimeException(e);
        // }
        // } else {
        // try {
        // int index = 0;
        // for (SimpleField field : row.getFields()) {
        // addCell(index, row, field, xlsRow);
        // index ++;
        // }
        // } catch (IOException e) {
        // throw new RuntimeException(e);
        // }
        // }
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

        result.setExecution(execution);

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

        Transaction tx = session.beginTransaction();
        tx.begin();
        session.save(execution);
        tx.commit();

    }

}
