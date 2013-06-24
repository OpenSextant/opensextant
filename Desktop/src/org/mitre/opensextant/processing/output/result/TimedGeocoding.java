package org.mitre.opensextant.processing.output.result;

import java.util.ArrayList;
import java.util.List;

import org.mitre.opensextant.processing.Geocoding;

public class TimedGeocoding extends Geocoding {

    public List<ParsedTime> times;
    
    public TimedGeocoding(String id, Geocoding other, final ParsedTime time) {
        this(id, other, new ArrayList<ParsedTime>() {{ add(time); }});
    }
    
    public TimedGeocoding(String id, Geocoding other, List<ParsedTime> times) {
        super(other);
        this.times = times;
        this.id = id;
    }

}
