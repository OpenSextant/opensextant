package org.mitre.opensextant.desktop.persistence.model;

import java.util.Date;


public class Execution {

    private Long id;
    private Date timestamp;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    public Date getTimestamp() {
        return timestamp;
    }
    
}
