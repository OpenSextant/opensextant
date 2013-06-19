package org.mitre.opensextant.processing.output;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.mitre.giscore.events.Feature;
import org.mitre.giscore.events.Schema;
import org.mitre.giscore.events.SimpleField;
import org.mitre.giscore.geometry.Point;
import org.mitre.opensextant.processing.Geocoding;
import org.mitre.opensextant.processing.GeocodingResult;
import org.mitre.opensextant.processing.OpenSextantSchema;
import org.mitre.opensextant.processing.ProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GISDataModel {

    protected static final Logger log = LoggerFactory.getLogger(GISDataModel.class);

    protected boolean includeOffsets;
    protected boolean includeCoordinate;
    protected Schema schema = null;
    
    protected List<String> field_order = new ArrayList<String>();
    public Set<String> field_set = new HashSet<String>();

    public GISDataModel(String jobName, boolean includeOffsets, boolean includeCoordinate) {
        this(jobName, includeOffsets, includeCoordinate, true);
    }
    public GISDataModel(String jobName, boolean includeOffsets, boolean includeCoordinate, boolean buildSchema) {
        super();
        this.includeOffsets = includeOffsets;
        this.includeCoordinate = includeCoordinate;
        if (buildSchema) {
            defaultFields();
            try {
                this.schema = buildSchema(jobName);
            } catch (ProcessingException e) {
                // could not successfully construct the schema... fail hard.
                throw new RuntimeException(e);
            }
        }
    }

    protected void addPlaceData(Feature row, Geocoding g) {
        addColumn(row, OpenSextantSchema.ISO_COUNTRY, g.place.getCountryCode());
        addColumn(row, OpenSextantSchema.PROVINCE, g.place.getAdmin1());
        addColumn(row, OpenSextantSchema.FEATURE_CLASS, g.place.getFeatureClass());
        addColumn(row, OpenSextantSchema.FEATURE_CODE, g.place.getFeatureCode());
        addColumn(row, OpenSextantSchema.PLACE_NAME, g.place.getPlaceName());
        // Set the geometry to be a point, and add the feature to the list
        row.setGeometry(new Point(g.place.getLatitude(), g.place.getLongitude()));
    }
    
    protected void addPrecisionAndConfidence(Feature row, Geocoding g) {
        addColumn(row, OpenSextantSchema.PRECISION, g.precision);
        addColumn(row, OpenSextantSchema.CONFIDENCE, formatConfidence(g.confidence));
    }

    protected void addOffsets(Feature row, Geocoding g) {
        addColumn(row, OpenSextantSchema.START_OFFSET, (int) g.start);
        addColumn(row, OpenSextantSchema.END_OFFSET, (int) g.end);
    }

    protected void addLatLon(Feature row, Geocoding g) {
        addColumn(row, OpenSextantSchema.LAT, g.place.getLatitude());
        addColumn(row, OpenSextantSchema.LON, g.place.getLongitude());
    }
    
    /**
     * If the caller has additional data to attach to records, allow them to
     * add fields to schema at runtime and map their data to keys on
     * GeocodingResult
     * 
     * Similarly, you could have Geocoding row-level attributes unique to
     * the geocoding whereas attrs on GeocodingResult are global for all
     * geocodings in that result set
     * @throws ProcessingException 
     */
    protected void addAdditionalAttributes(Feature row, Map<String, Object> rowAttributes) throws ProcessingException {
        if (rowAttributes != null) {

            try {
                for (String field : rowAttributes.keySet()) {
                    if (log.isDebugEnabled()) {
                        log.debug("FIELD=" + field + " = " + rowAttributes.get(field));
                    }
                    addColumn(row, OpenSextantSchema.getField(field), rowAttributes.get(field));
                }
            } catch (ProcessingException fieldErr) {
                throw fieldErr;
            }
        }
    }
    
    protected void addFilePaths(Feature row, String recordFile, String recordTextFile) {
        // TOOD: HPATH goes here.
        if (recordFile != null) {
            addColumn(row, OpenSextantSchema.FILENAME, FilenameUtils.getBaseName(recordFile));
            addColumn(row, OpenSextantSchema.FILEPATH, recordFile);
            // Only add text path:
            // if original is not plaintext or
            // if original has not been converted
            //
            if (recordTextFile != null && !recordFile.equals(recordTextFile)) {
                addColumn(row, OpenSextantSchema.TEXTPATH, recordTextFile);
            }
        } else {
            log.info("No File path given");
        }
    }

    protected void addContext(Feature row, Geocoding g) {
        addColumn(row, OpenSextantSchema.CONTEXT, g.getContext());
    }

    protected void addMatchText(Feature row, Geocoding g) {
        addColumn(row, OpenSextantSchema.MATCH_TEXT, g.getText());
    }
    
    protected void addMatchMethod(Feature row, Geocoding g) {
        addColumn(row, OpenSextantSchema.MATCH_METHOD, g.method);
    }

    public Feature buildRow(int id, Geocoding g, Map<String, Object> rowAttributes, String recordFile, String recordTextFile) throws ProcessingException {

        Feature row = new Feature();
        // Administrative settings:
        // row.setName(getJobName());
        row.setSchema(schema.getId());
        row.putData(OpenSextantSchema.SCHEMA_OID, id);

        //
        if (includeOffsets) {
            addOffsets(row, g);
        }
        
        addPlaceData(row, g);
        addPrecisionAndConfidence(row, g);

        addContext(row, g);

        if (includeCoordinate) {
            addLatLon(row, g);
        }


        addMatchText(row, g);
        addMatchMethod(row, g);
        
        addAdditionalAttributes(row, rowAttributes);


        addFilePaths(row, recordFile, recordTextFile);
        
        return row;

    }

    private static final DecimalFormat confFmt = new DecimalFormat("0.000");    
    /** Convenience method for managing how confidence number is reported in output.
     */
    protected String formatConfidence(double conf){
        return confFmt.format(conf);
    }

    
    public Schema getSchema() {
        return this.schema;
    }
    
    /**
     * Create a schema instance with the fields properly typed and ordered
     * 
     * @return
     * @throws ProcessingException
     */
    protected Schema buildSchema(String jobName) throws ProcessingException {

        if (this.schema != null) {
            return this.schema;
        }

        URI uri = null;
        try {
            uri = new URI("urn:OpenSextant");
        } catch (URISyntaxException e) {
            // e.printStackTrace();
        }

        this.schema = new Schema(uri);
        // Add ID field to the schema
        this.schema.put(OpenSextantSchema.SCHEMA_OID);
        this.schema.setName(jobName);

        for (String field : field_order) {

            if (!this.includeOffsets && (field.equals("start") | field.equals("end"))) {
                continue;
            }

            if (!this.includeCoordinate && (field.equals("lat") | field.equals("lon"))) {
                continue;
            }

            SimpleField F = getField(field);
            this.schema.put(F);
        }
        
        this.field_set.addAll(field_order);
        
        return this.schema;
    }

    protected SimpleField getField(String field) throws ProcessingException {
        return OpenSextantSchema.getField(field);
    }

    /**
     */
    protected boolean canAdd(SimpleField f) {
        if (f == null) {
            return false;
        }
        return field_set.contains(f.getName()) && (schema.get(f.getName()) != null);
    }

    /**
     * Add a column of data to output; Field is validated ; value is not added
     * if null
     */
    protected void addColumn(Feature row, SimpleField f, Object d) {
        if (d == null) {
            return;
        }
        if (canAdd(f)) {
            row.putData(f, d);
        }
    }

    /**
     * Add a column of data to output; Field is validated
     */
    protected void addColumn(Feature row, SimpleField f, int d) {
        if (canAdd(f)) {
            row.putData(f, d);
        }
    }

    /**
     * Add a column of data to output; Field is validated
     */
    protected void addColumn(Feature row, SimpleField f, double d) {
        if (canAdd(f)) {
            row.putData(f, d);
        }
    }
    
    protected void defaultFields() {
        // ID occurs in all output.
        // id.

        // Matching data
        field_order.add("placename");

        // Geographic
        field_order.add("province");
        field_order.add("iso_cc");
        field_order.add("lat");
        field_order.add("lon");

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
        field_order.add("confidence");
        field_order.add("precision");
        field_order.add("start");
        field_order.add("end");

    }

    public GeocodingResult buildGeocodingResults(String name) {
        return new GeocodingResult(name);
    }


}
