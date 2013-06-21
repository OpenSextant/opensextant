package org.mitre.opensextant.desktop.persistence.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;

@Entity
@Table(name="executions")
public class Execution {

    private Long id;
    private Date timestamp;
    private Set<Result> results = new HashSet<Result>();

    @Id
    @GeneratedValue
    @Column(name="execution_id")
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    
    @Column
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    public Date getTimestamp() {
        return timestamp;
    }
    
    
    @OneToMany(mappedBy="execution", cascade=CascadeType.ALL)
    public Set<Result> getResults() {
        return results;
    }
    public void setResults(Set<Result> results) {
        this.results = results;
        for (Result result : results) {
            result.setExecution(this);
        }
    }
    
}
