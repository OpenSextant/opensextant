package org.mitre.opensextant.processing.output.result;

import java.util.HashSet;
import java.util.Set;

import gate.Annotation;
import gate.Document;
import gate.FeatureMap;
import gate.Utils;

import org.mitre.opensextant.placedata.Geocoord;
import org.mitre.opensextant.placedata.PlaceCandidate;
import org.mitre.opensextant.processing.Geocoding;
import org.mitre.opensextant.processing.GeocodingResult;
import org.mitre.opensextant.processing.Parameters;
import org.mitre.opensextant.processing.ProcessingException;
import org.mitre.opensextant.processing.ResultsUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdentifierResult extends GeocodingResult {

    private static Logger log = LoggerFactory.getLogger(IdentifierResult.class);

    public final static String NOUN_PHRASE = "NounPhrase";
    public final static Set<String> NOUN_ANNOTATIONS = new HashSet<String>();

    static {
        NOUN_ANNOTATIONS.add(NOUN_PHRASE);
    }
    
    public IdentifierResult(String rid) {
        super(rid);
    }

    @Override
    public void retrieveGeocodes(Document doc, Parameters params) throws ProcessingException {
        int incr = 0;
        String content = doc.getContent().toString();
        int content_length = content.length();

        String match;
        
        for (Annotation a : doc.getAnnotations().get(NOUN_ANNOTATIONS)) {

            Object entityType = a.getFeatures().get("EntityType");
            if (entityType != null && ((String)entityType).startsWith("Information")) {
                ++incr;
                match = Utils.cleanStringFor(doc, a);

                if (match == null) {
                    throw new ProcessingException("Match should not be null for ANNOT=" + a.getType());
                }
                Identifier geo = new Identifier(recordID + "." + incr, match);

                geo.start = Utils.start(a).longValue();
                geo.end = Utils.end(a).longValue();
                
                geo.setEntityType(a.getFeatures().get("EntityType").toString());
                geo.setFeatureType(a.getType());

                // Enrich with context field.
                if ((Parameters.RUNTIME_FLAGS & Parameters.FLAG_EXTRACT_CONTEXT) > 0) {
                    ResultsUtility.setContextFor(content, geo, (int) geo.start, match.length(), content_length);
                }
                
                geocodes.add(geo);
            }

        }
            
    }

    
}
