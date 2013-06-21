package org.mitre.opensextant.processing.output.result;

import gate.Annotation;
import gate.Document;
import gate.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mitre.opensextant.desktop.ui.helpers.ConfigHelper.TimeAssociation;
import org.mitre.opensextant.processing.Geocoding;
import org.mitre.opensextant.processing.GeocodingResult;
import org.mitre.opensextant.processing.Parameters;
import org.mitre.opensextant.processing.ProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimedGeocodingResult extends GeocodingResult {

    private static Logger log = LoggerFactory.getLogger(TimedGeocodingResult.class);

    public final static String NOUN_PHRASE = "NounPhrase";
    public final static Set<String> NOUN_ANNOTATIONS = new HashSet<String>() {{
        add(NOUN_PHRASE);
    }};

    private TimeAssociation timeAssociation;

    public TimedGeocodingResult(String rid, TimeAssociation timeAssociation) {
        super(rid);
        this.timeAssociation = timeAssociation;
    }

    @Override
    public void retrieveGeocodes(Document doc, Parameters params) throws ProcessingException {
        super.retrieveGeocodes(doc, params);
        
        List<String> times = new ArrayList<String>();
        for (Annotation a : doc.getAnnotations().get(NOUN_ANNOTATIONS)) {
            if (a.getFeatures().get("EntityType") != null && ((String)a.getFeatures().get("EntityType")).startsWith("Time.date")) {
                String match = Utils.cleanStringFor(doc, a);
                times.add(match);
                
            }
        }
        
        int id = 0;
        List<Geocoding> timedGeocodings = new ArrayList<Geocoding>();
        for (Geocoding geocoding : geocodes) {
            switch (timeAssociation) {
            case CSV:
                timedGeocodings.add(new TimedGeocoding(String.valueOf(id++), geocoding, times));
                break;
            case CROSS:
                for (String time : times) {
                    timedGeocodings.add(new TimedGeocoding(String.valueOf(id++), geocoding, time));
                }
                break;
            }
        }
        
        
        geocodes = timedGeocodings;
    }

    
    
}
