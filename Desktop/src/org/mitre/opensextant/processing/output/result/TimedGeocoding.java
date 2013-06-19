package org.mitre.opensextant.processing.output.result;

import java.util.ArrayList;
import java.util.List;

import org.mitre.opensextant.processing.Geocoding;

public class TimedGeocoding extends Geocoding {

    public List<String> times;
    
    public TimedGeocoding(String id, Geocoding other, final String time) {
        this(id, other, new ArrayList<String>() {{ add(time); }});
    }
    
    public TimedGeocoding(String id, Geocoding other, List<String> times) {
        super(other);
        this.times = times;
        this.id = id;
    }

}
