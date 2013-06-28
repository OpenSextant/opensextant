package org.mitre.opensextant.processing.output.result;

import java.util.Calendar;
import java.util.Date;

public class ParsedTime implements Comparable<ParsedTime> {

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((matchText == null) ? 0 : matchText.hashCode());
        result = prime * result + ((normalizedDate == null) ? 0 : normalizedDate.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ParsedTime other = (ParsedTime) obj;
        if (matchText == null) {
            if (other.matchText != null)
                return false;
        } else if (!matchText.equals(other.matchText))
            return false;
        if (normalizedDate == null) {
            if (other.normalizedDate != null)
                return false;
        } else if (!normalizedDate.equals(other.normalizedDate))
            return false;
        return true;
    }

    @Override
    public int compareTo(ParsedTime o) {
        int result = this.getNormalizedDate().compareTo(o.getNormalizedDate());
        if (result == 0) result = this.getMatchText().compareToIgnoreCase(o.getMatchText());
        return result;
    }
    
    
}
