package org.mitre.opensextant.processing.output;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mitre.giscore.events.Feature;
import org.mitre.giscore.events.SimpleField;
import org.mitre.opensextant.desktop.ui.helpers.ConfigHelper.GeoExtraction;
import org.mitre.opensextant.desktop.ui.helpers.ConfigHelper.TimeAssociation;
import org.mitre.opensextant.processing.Geocoding;
import org.mitre.opensextant.processing.GeocodingResult;
import org.mitre.opensextant.processing.ProcessingException;
import org.mitre.opensextant.processing.output.result.ParsedTime;
import org.mitre.opensextant.processing.output.result.TimedGeocoding;
import org.mitre.opensextant.processing.output.result.TimedGeocodingResult;

public class TimeGISDataModel extends FilteringGISDataModel {

    private static final String TIME_FIELD = "time";
    @SuppressWarnings("serial")
    private static final Set<String> CUSTOM_FIELDS = new HashSet<String>() {
        {
            add(TIME_FIELD);
        }
    };

    private TimeAssociation timeAssociation;

    public TimeGISDataModel(String jobName, boolean includeOffsets, boolean includeCoordinate, GeoExtraction geoExtraction, TimeAssociation timeAssociation) {
        super(jobName, includeOffsets, includeCoordinate, geoExtraction);
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
        return new TimedGeocodingResult(name, geoExtraction, timeAssociation);
    }

    @Override
    public Feature buildRow(int id, Geocoding g, Map<String, Object> rowAttributes, String recordFile, String recordTextFile)
            throws ProcessingException {
        Feature row = super.buildRow(id, g, rowAttributes, recordFile, recordTextFile);
        if (timeAssociation == TimeAssociation.CSV) {
            String delim = "";
            StringBuffer times = new StringBuffer();
            for (ParsedTime time : ((TimedGeocoding) g).times) {
                times.append(delim).append(time.getYear());
                delim = ",";
            }
            addColumn(row, getField("time"), times);
        } else {
            addColumn(row, getField("time"), getFirst(((TimedGeocoding) g).times));
        }
        return row;
    }

    private Date getFirst(List<ParsedTime> times) {
        if (times.size() > 0) {
            return times.get(0).getNormalizedDate();
        }
        return null;
    }

    @Override
    protected SimpleField getField(String field) throws ProcessingException {
        if (CUSTOM_FIELDS.contains(field)) {
            switch (field) {
                case TIME_FIELD:
                    if (timeAssociation == TimeAssociation.CSV) {
                        return new SimpleField(field, SimpleField.Type.STRING);
                    } else {
                        return new SimpleField(field, SimpleField.Type.DATE);
                    }
                 default: 
                     throw new ProcessingException("Unknown custom field: " + field);
            }
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
