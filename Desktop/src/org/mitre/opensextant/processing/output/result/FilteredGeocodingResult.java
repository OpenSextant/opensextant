package org.mitre.opensextant.processing.output.result;

import gate.Annotation;
import gate.Document;
import gate.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mitre.opensextant.desktop.ui.helpers.ConfigHelper.GeoExtraction;
import org.mitre.opensextant.desktop.ui.helpers.ConfigHelper.TimeAssociation;
import org.mitre.opensextant.processing.Geocoding;
import org.mitre.opensextant.processing.GeocodingResult;
import org.mitre.opensextant.processing.Parameters;
import org.mitre.opensextant.processing.ProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilteredGeocodingResult extends GeocodingResult {

    private static Logger log = LoggerFactory.getLogger(FilteredGeocodingResult.class);

    public final static String NOUN_PHRASE = "NounPhrase";
    @SuppressWarnings("serial")
    public final static Set<String> NOUN_ANNOTATIONS = new HashSet<String>() {{
        add(NOUN_PHRASE);
    }};

    private GeoExtraction geoExtraction;

    public FilteredGeocodingResult(String rid, GeoExtraction geoExtraction) {
        super(rid);
        this.geoExtraction = geoExtraction;
    }

    @Override
    public void retrieveGeocodes(Document doc, Parameters params) throws ProcessingException {
        super.retrieveGeocodes(doc, params);
        
        List<Geocoding> filteredGeocodings = new ArrayList<Geocoding>();
        for (Geocoding geocoding : geocodes) {
            if ((geocoding.is_place && geoExtraction.extractPlaces()) || (geocoding.is_coordinate && geoExtraction.extractCoordinates())) {
                filteredGeocodings.add(geocoding);
            }
        }
        
        
        geocodes = filteredGeocodings;
    }

    
    
}
