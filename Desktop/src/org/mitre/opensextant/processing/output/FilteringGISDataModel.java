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
import org.mitre.opensextant.processing.output.result.FilteredGeocodingResult;
import org.mitre.opensextant.processing.output.result.ParsedTime;
import org.mitre.opensextant.processing.output.result.TimedGeocoding;
import org.mitre.opensextant.processing.output.result.TimedGeocodingResult;

public class FilteringGISDataModel extends GISDataModel {

    protected GeoExtraction geoExtraction;

    public FilteringGISDataModel(String jobName, boolean includeOffsets, boolean includeCoordinate, GeoExtraction geoExtraction) {
        super(jobName, includeOffsets, includeCoordinate, false);
        defaultFields();
        try {
            this.schema = super.buildSchema(jobName);
        } catch (ProcessingException e) {
            // could not successfully construct the schema... fail hard.
            throw new RuntimeException(e);
        }
        this.geoExtraction = geoExtraction;
    }

    @Override
    public GeocodingResult buildGeocodingResults(String name) {
        return new FilteredGeocodingResult(name, geoExtraction);
    }


}
