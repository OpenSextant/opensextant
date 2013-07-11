package org.mitre.opensextant.processing.output.os;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.mitre.abi.timePlayer.remote.client.ABIRMIClient;
import org.mitre.abi.timePlayer.remote.client.SimpleTimeDot;
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

public class ABIOutputStream extends StreamVisitorBase implements IGISOutputStream {

    private static Logger log = LoggerFactory.getLogger(ABIOutputStream.class);

    private Schema schema;
    private String name;
    private List<SimpleTimeDot> dots = new ArrayList<SimpleTimeDot>();

    public ABIOutputStream(String name) {
        this.name = name;
    }

    @Override
    public void close() throws IOException {
        ABIRMIClient.writeToMap(name, dots);
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

        SimpleTimeDot dot = createSimpleTimeDot(schema.getFields(), row);
        dots.add(dot);

    }

    private SimpleTimeDot createSimpleTimeDot(Collection<SimpleField> fields, Row row) {

        double latitude = 0;
        double longitude = 0;
        for (SimpleField field : fields) {
            if ("lat".equals(field.getName())) {
                latitude = (Double)row.getData(field);
            } else if ("lon".equals(field.getName())) {
                longitude = (Double)row.getData(field);
            }
        }

        return new SimpleTimeDot(latitude, longitude, -1, -1);
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

    }

}
