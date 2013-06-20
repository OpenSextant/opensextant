/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mitre.opensextant.desktop.util;

import gate.Document;
import org.mitre.giscore.events.Feature;
import org.mitre.opensextant.processing.Geocoding;
import org.mitre.opensextant.processing.OpenSextantSchema;
import org.mitre.opensextant.processing.output.result.IdentifierResult;

/**
 *
 * @author GBLACK
 */
public class JobStatistics {
    
    private int objs = 0;
    private int coords = 0;
    private int place = 0;
    private int administrative = 0;
    private int country = 0;
    private int duplicate = 0;
    private int overlap = 0;
    private int submatch = 0;
    
    public int getCoords() { return coords; }
    public int getPlaces() { return coords; }
    public int getCountries() { return coords; }

    
}
