package org.mitre.opensextant.processing.output;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opensextant.giscore.events.Feature;
import org.opensextant.giscore.events.SimpleField;
import org.opensextant.giscore.utils.SafeDateFormat;
import org.mitre.opensextant.desktop.ui.helpers.ConfigHelper.GeoExtraction;
import org.mitre.opensextant.desktop.ui.helpers.ConfigHelper.TimeAssociation;
import org.mitre.opensextant.processing.Geocoding;
import org.mitre.opensextant.processing.GeocodingResult;
import org.mitre.opensextant.processing.ProcessingException;
import org.mitre.opensextant.processing.output.result.ParsedTime;
import org.mitre.opensextant.processing.output.result.TimedGeocodingResult;

public class TimeGISDataModel extends FilteringGISDataModel {

    private static final String DATE_FMT = "yyyy-MM-dd";
    private SafeDateFormat dateFormatter;
    
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
        return new TimedGeocodingResult(name, geoExtraction);
    }
    
    // Thread-safe date formatter helper method
    private SafeDateFormat getDateFormatter() {
        if (dateFormatter == null) {
            dateFormatter = new SafeDateFormat(DATE_FMT);
        }
        return dateFormatter;
    }


    @Override
    public List<Feature> buildRows(int id, Geocoding g, Map<String, Object> rowAttributes, String recordFile, String recordTextFile, GeocodingResult allResults) throws ProcessingException {
        
        List<Feature> timedRows = new ArrayList<Feature>();
        List<ParsedTime> allParsedTimes = ((TimedGeocodingResult)allResults).getParsedTimes();

        if (timeAssociation == TimeAssociation.CSV) {

            List<Feature> untimedRows = super.buildRows(id, g, rowAttributes, recordFile, recordTextFile, allResults);

            String timesString = null;

            for (Feature untimedRow : untimedRows) {
                if (timesString == null) {
                    String delim = "";
                    StringBuffer times = new StringBuffer();
                    for (ParsedTime time : allParsedTimes) {
                        times.append(delim).append(getDateFormatter().format(time.getNormalizedDate()));
                        delim = ",";
                    }
                    timesString = times.toString();
                }
                
                addColumn(untimedRow, getField("time"), timesString);
                timedRows.add(untimedRow);
            }
            
        } else {
            // the || i == 0 is there to make sure we make at least one pass
            for (int i=0; i < allParsedTimes.size() || i == 0; i++) {
                List<Feature> untimedRows = super.buildRows(id, g, rowAttributes, recordFile, recordTextFile, allResults);

                // get the parsed time if there is one, if not, leave it null
                ParsedTime parsedTime = null;
                if (allParsedTimes.size() > 0) {
                    parsedTime = allParsedTimes.get(i);
                }
                
                for (Feature untimedRow : untimedRows) {
                    // if we don't have any times, just add the row without the time.
                    if (parsedTime != null) {
                        addColumn(untimedRow, getField("time"), parsedTime.getNormalizedDate());
                    }
                    timedRows.add(untimedRow);
                }
            }
        }

        return timedRows;
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
