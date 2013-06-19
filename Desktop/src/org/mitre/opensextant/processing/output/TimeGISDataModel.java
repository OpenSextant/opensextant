package org.mitre.opensextant.processing.output;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.mitre.giscore.events.Feature;
import org.mitre.giscore.events.SimpleField;
import org.mitre.opensextant.desktop.ui.helpers.ConfigHelper.TimeAssociation;
import org.mitre.opensextant.processing.Geocoding;
import org.mitre.opensextant.processing.GeocodingResult;
import org.mitre.opensextant.processing.ProcessingException;
import org.mitre.opensextant.processing.output.GISDataModel;
import org.mitre.opensextant.processing.output.result.TimedGeocodingResult;
import org.mitre.opensextant.processing.output.result.TimedGeocoding;

public class TimeGISDataModel extends GISDataModel{

    private static final String TIME_FIELD = "time";
    private static final Set<String> CUSTOM_FIELDS = new HashSet<String>() {{
       add(TIME_FIELD);
    }};
    
    private TimeAssociation timeAssociation;

    public TimeGISDataModel(String jobName, boolean includeOffsets, boolean includeCoordinate, TimeAssociation timeAssociation) {
        super(jobName, includeOffsets, includeCoordinate, false);
        defaultFields();
        try {
            this.schema = super.buildSchema(jobName);
        } catch (ProcessingException e) {
            // could not successfully construct the schema... fail hard.
            throw new RuntimeException(e);
        }
        this.timeAssociation = timeAssociation;
    }

    @Override
    public GeocodingResult buildGeocodingResults(String name) {
        return new TimedGeocodingResult(name, timeAssociation);
    }
    
    

    @Override
    public Feature buildRow(int id, Geocoding g, Map<String, Object> rowAttributes, String recordFile, String recordTextFile) throws ProcessingException {
        Feature row = super.buildRow(id, g, rowAttributes, recordFile, recordTextFile);
        addColumn(row, getField("time"), StringUtils.join(((TimedGeocoding)g).times, ","));
        return row;
    }

    @Override
    protected SimpleField getField(String field) throws ProcessingException {
        if (CUSTOM_FIELDS.contains(field)) {
           return new SimpleField(field, SimpleField.Type.STRING);
        } else {
            return super.getField(field);
        }
    }
    
    @Override
    protected void defaultFields() {
        super.defaultFields();
        
        field_order.add(TIME_FIELD);
    }


}
