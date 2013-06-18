package org.mitre.opensextant.processing.output.result;

import org.mitre.opensextant.processing.Geocoding;

public class Identifier extends Geocoding {

    private String featureType;
    private String entityType;
    
    public Identifier(String id, String match) {
        super(id, match);
    }

    public String getFeatureType() {
        return featureType;
    }

    public void setFeatureType(String featureType) {
        this.featureType = featureType;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }


}
