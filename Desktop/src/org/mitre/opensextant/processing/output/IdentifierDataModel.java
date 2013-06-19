package org.mitre.opensextant.processing.output;

import java.util.Map;

import org.mitre.giscore.events.Feature;
import org.mitre.opensextant.processing.Geocoding;
import org.mitre.opensextant.processing.GeocodingResult;
import org.mitre.opensextant.processing.OpenSextantSchema;
import org.mitre.opensextant.processing.ProcessingException;
import org.mitre.opensextant.processing.output.GISDataModel;
import org.mitre.opensextant.processing.output.result.Identifier;
import org.mitre.opensextant.processing.output.result.IdentifierResult;

public class IdentifierDataModel extends GISDataModel {

    public IdentifierDataModel(String jobName, boolean includeOffsets, boolean includeCoordinate) {
        super(jobName, includeOffsets, includeCoordinate);
    }

    @Override
    public GeocodingResult buildGeocodingResults(String name) {
        return new IdentifierResult(name);
    }

    @Override
    public Feature buildRow(int id, Geocoding g, Map<String, Object> rowAttributes, String recordFile, String recordTextFile) throws ProcessingException {
        
        Feature row = new Feature();
        row.setSchema(schema.getId());
        row.putData(OpenSextantSchema.SCHEMA_OID, id);

        if (includeOffsets) {
            addOffsets(row, g);
        }
        
        addColumn(row, OpenSextantSchema.FEATURE_CLASS, ((Identifier)g).getFeatureType());
        addColumn(row, OpenSextantSchema.FEATURE_CODE, ((Identifier)g).getEntityType());

        addContext(row, g);

        addMatchText(row, g);
        addMatchMethod(row, g);
        
        addAdditionalAttributes(row, rowAttributes);
        addFilePaths(row, recordFile, recordTextFile);
        
        return row;
        
    }
    
    protected void defaultFields() {

        // Textual context.
        field_order.add("matchtext");
        field_order.add("context");
        field_order.add("filename");
        field_order.add("filepath");
        field_order.add("textpath");

        // File mechanics
        field_order.add("method");
        field_order.add("feat_class");
        field_order.add("feat_code");
        field_order.add("start");
        field_order.add("end");

    }

    
    

}
