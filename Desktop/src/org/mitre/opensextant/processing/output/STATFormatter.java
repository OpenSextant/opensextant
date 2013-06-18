package org.mitre.opensextant.processing.output;

import gate.Document;

import org.apache.commons.io.FilenameUtils;
import org.mitre.giscore.events.Feature;
import org.mitre.giscore.geometry.Point;
import org.mitre.opensextant.processing.Geocoding;
import org.mitre.opensextant.processing.GeocodingResult;
import org.mitre.opensextant.processing.OpenSextantSchema;
import org.mitre.opensextant.processing.ProcessingException;
import org.mitre.opensextant.processing.output.result.Identifier;
import org.mitre.opensextant.processing.output.result.IdentifierResult;

public class STATFormatter extends GISDataFormatter {

//    private int id = 0;


    public STATFormatter() throws ProcessingException {
        super();
     //   isIdentifiers = true;
    }

    @Override
    public void writeRowsFor(Document doc) {
        // Is there a doc ID?
    /*    IdentifierResult identityAnnotations = new IdentifierResult(doc.getName());
        identityAnnotations.recordFile = (String) doc.getFeatures().get(OpenSextantSchema.FILEPATH_FLD);
        identityAnnotations.recordTextFile = doc.getSourceUrl().getPath();
        log.info("Writing identifiers for " + identityAnnotations.recordFile);

        try {
            identityAnnotations.retrieveGeocodes(doc);

            writeGeocodingResult(identityAnnotations);

        } catch (Exception err) {
            log.error("Error writing out row ROW=" + doc.getName(), err);
        }*/
    }
    
    
    @Override
    public void writeGeocodingResult(GeocodingResult rowdata) {
/*        Feature row;
        boolean error = false;

        if (log.isDebugEnabled()) {
            log.debug("Adding data for File " + rowdata.recordFile + " Count=" + rowdata.geocodes.size());
        }

        for (Geocoding g : rowdata.geocodes) {

            if (filterOut(g)) {
                continue;
            }
            // Increment ID
            id ++;

            row = new Feature();
            row.setSchema(schema.getId());
            row.putData(OpenSextantSchema.SCHEMA_OID, id);

            if (includeOffsets) {
                addColumn(row, OpenSextantSchema.START_OFFSET, (int) g.start);
                addColumn(row, OpenSextantSchema.END_OFFSET, (int) g.end);
            }

            addColumn(row, OpenSextantSchema.FEATURE_CLASS, ((Identifier)g).getFeatureType());
            addColumn(row, OpenSextantSchema.FEATURE_CODE, ((Identifier)g).getEntityType());

            addColumn(row, OpenSextantSchema.CONTEXT, g.getContext());

            addColumn(row, OpenSextantSchema.MATCH_TEXT, g.getText());
            addColumn(row, OpenSextantSchema.MATCH_METHOD, g.method);


            if (rowdata.attributes != null) {

                try {
                    for (String field : rowdata.attributes.keySet()) {
                        addColumn(row, OpenSextantSchema.getField(field), rowdata.attributes.get(field));
                    }
                } catch (ProcessingException fieldErr) {
                    if (!error) {
                        log.error("OUTPUTTER, ERR=" + fieldErr);
                        error = true;
                    }
                }
            }

            // TOOD: HPATH goes here.
            if (rowdata.recordFile != null) {
                addColumn(row, OpenSextantSchema.FILENAME, FilenameUtils.getBaseName(rowdata.recordFile));
                addColumn(row, OpenSextantSchema.FILEPATH, rowdata.recordFile);
                if (rowdata.recordTextFile != null && !rowdata.recordFile.equals(rowdata.recordTextFile)) {
                    addColumn(row, OpenSextantSchema.TEXTPATH, rowdata.recordTextFile);
                }
            } else {
                log.info("No File path given");
            }

            this.os.write(row);
        }*/

    }

    @Override
    protected void createOutputStreams() throws Exception {
   //     throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
