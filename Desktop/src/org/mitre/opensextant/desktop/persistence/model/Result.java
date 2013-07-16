package org.mitre.opensextant.desktop.persistence.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Result implements Serializable{

	private long id = 1L;
	
	private String placename;
	private String province;
	private String isoCc;
	private double lat;
	private double lon;
	private String matchtext;
	private String context;
	private String filename;
	private String filepath;
	private String textpath;
	private String method;
	private String featClass;
	private String featCode;
	private String confidence;
	private int precision;
	private int start;
	private int end;
	private String time;
	private long executionId;
	
	public Result() {
	    super();
	}
	
	public Result(long id, String placename, String province, String isoCc, double lat, double lon, String matchtext, String context,
            String filename, String filepath, String textpath, String method, String featClass, String featCode, String confidence,
            int precision, int start, int end, String time) {
        super();
        this.id = id;
        this.placename = placename;
        this.province = province;
        this.isoCc = isoCc;
        this.lat = lat;
        this.lon = lon;
        this.matchtext = matchtext;
        this.context = context;
        this.filename = filename;
        this.filepath = filepath;
        this.textpath = textpath;
        this.method = method;
        this.featClass = featClass;
        this.featCode = featCode;
        this.confidence = confidence;
        this.precision = precision;
        this.start = start;
        this.end = end;
        this.time = time;
    }

	public long getId() {
		return id;
	}
    public void setId(long id) {
        this.id = id;
    }
    
    public String getPlacename() {
        return placename;
    }
    public void setPlacename(String placename) {
        this.placename = placename;
    }

    public String getProvince() {
        return province;
    }
    public void setProvince(String province) {
        this.province = province;
    }

    public String getIsoCc() {
        return isoCc;
    }
    public void setIsoCc(String isoCc) {
        this.isoCc = isoCc;
    }

    public double getLat() {
        return lat;
    }
    public void setLat(Double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }
    public void setLon(Double lon) {
        this.lon = lon;
    }

    public String getMatchtext() {
        return matchtext;
    }
    public void setMatchtext(String matchtext) {
        this.matchtext = matchtext;
    }

    public String getContext() {
        return context;
    }
    public void setContext(String context) {
        this.context = context;
    }

    public String getFilename() {
        return filename;
    }
    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilepath() {
        return filepath;
    }
    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public String getTextpath() {
        return textpath;
    }
    public void setTextpath(String textpath) {
        this.textpath = textpath;
    }

    public String getMethod() {
        return method;
    }
    public void setMethod(String method) {
        this.method = method;
    }

    public String getFeatClass() {
        return featClass;
    }
    public void setFeatClass(String featClass) {
        this.featClass = featClass;
    }

    public String getFeatCode() {
        return featCode;
    }
    public void setFeatCode(String featCode) {
        this.featCode = featCode;
    }

    public String getConfidence() {
        return confidence;
    }
    public void setConfidence(String confidence) {
        this.confidence =confidence;
    }

    public int getPrecision() {
        return precision;
    }
    public void setPrecision(Integer precision) {
        this.precision = precision;
    }

    public int getStart() {
        return start;
    }
    public void setStart(Integer start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }
    public void setEnd(Integer end) {
        this.end = end;
    }

    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }
    
    
    
    public long getExecutionId() {
		return executionId;
	}

	public void setExecutionId(long executionId) {
		this.executionId = executionId;
	}

	@Override
    public String toString() {
        return "Result [id=" + id + ", execution_id=" + executionId + ", placename=" + placename + ", province=" + province + ", isoCc=" + isoCc + ", lat=" + lat + ", lon="
                + lon + ", matchtext=" + matchtext + ", context=" + context + ", filename=" + filename + ", filepath=" + filepath
                + ", textpath=" + textpath + ", method=" + method + ", featClass=" + featClass + ", featCode=" + featCode + ", confidence="
                + confidence + ", precision=" + precision + ", start=" + start + ", end=" + end + ", time=" + time + "]";
    }
    
}
