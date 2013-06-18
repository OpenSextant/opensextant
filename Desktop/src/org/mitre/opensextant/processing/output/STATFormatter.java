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
    }

    @Override
    public void writeRowsFor(Document doc) {
        // Is there a doc ID?
        
        IdentifierResult identityAnnotations = new IdentifierResult(doc.getName());
        identityAnnotations.recordFile = (String) doc.getFeatures().get(OpenSextantSchema.FILEPATH_FLD);
        identityAnnotations.recordTextFile = doc.getSourceUrl().getPath();
        log.info("Writing identifiers for " + identityAnnotations.recordFile);

        try {
            identityAnnotations.retrieveGeocodes(doc);

            writeGeocodingResult(identityAnnotations);

        } catch (Exception err) {
            log.error("Error writing out row ROW=" + doc.getName(), err);
        }
    }
    
    
    @Override
    public void writeGeocodingResult(GeocodingResult rowdata) {        
        Feature row;
        boolean error = false;

        if (log.isDebugEnabled()) {
            log.debug("Adding data for File " + rowdata.recordFile + " Count=" + rowdata.geocodes.size());
        }

        int coords = 0;
        int places = 0;
        int objs = 0;
        int admin = 0;
        int countries = 0;
        int dups = 0;
        int overlaps = 0;
        int subs = 0;
        
        for (Geocoding g : rowdata.geocodes) {

            if (filterOut(g)) {
           //     continue;
            }
            // Increment ID
            if(g.is_coordinate) coords ++;
            if(g.is_place) places ++;
            if(g.is_administrative) admin ++;
            if(g.is_country) countries ++;
            if(g.is_duplicate) dups ++;
            if(g.is_overlap) overlaps ++;
            if(g.is_submatch) subs ++;
            objs ++;
            
       //     System.out.println("    GEO: " + g.toString() + " -- " + g.getContext() + " -- " + g.is_country );
   
        }
        System.out.println("  Objs: " + objs);
        System.out.println("  Coords: " + coords);
        System.out.println("  Place: " + places);
        System.out.println("  Admin: " + admin);
        System.out.println("  Country: " + countries);
        System.out.println("  Duplicate: " + dups);
        System.out.println("  Overlap: " + overlaps);
        System.out.println("  Subs: " + subs);
    }

    @Override
    protected void createOutputStreams() throws Exception {
   //     throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
