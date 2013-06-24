package org.mitre.opensextant.processing.output.result;

import java.util.Calendar;
import java.util.Date;

public class ParsedTime {

    private String matchText;
    private Calendar normalizedDate;
    
    public ParsedTime(String matchText, Date normalizedDate) {
        super();
        this.matchText = matchText;
        this.setNormalizedDate(normalizedDate);
    }
    
    public String getMatchText() {
        return matchText;
    }
    public void setMatchText(String matchText) {
        this.matchText = matchText;
    }
    public Date getNormalizedDate() {
        return normalizedDate.getTime();
    }
    public void setNormalizedDate(Date normalizedDate) {
        this.normalizedDate = Calendar.getInstance();
        this.normalizedDate.setTime(normalizedDate);
    }
    public int getYear() {
        return this.normalizedDate.get(Calendar.YEAR);
    }
    
}
