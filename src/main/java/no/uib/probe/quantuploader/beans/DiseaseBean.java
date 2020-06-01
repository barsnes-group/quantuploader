
package no.uib.probe.quantuploader.beans;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

/**
 * 
 * @author Carlos Horro
 * Utility bean for an easy management of information from the 'diseases' sql table
 * This bean itself is NOT persisted to the DB
 */
//@Entity
public class DiseaseBean implements Serializable, Comparable  {
    
    //@Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    //@ManyToOne
    private DiseaseGroupBean diseaseGroupBean;
    
    private String acronym;
    //@Column(length = 1000)
    private String description;
    
    public static final String HEALTHY_STRING = "Healthy";
    public static final String CONTROL_STRING = "Control";

    
    /*
    * Id should not be assigned manually, but just using data.sql loading
    */
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Integer getId() {
        return id;
    }
    
    public DiseaseGroupBean getDiseaseGroupBean() {
        return diseaseGroupBean;
    }

    public void setDiseaseGroupBean(DiseaseGroupBean diseaseGroupBean) {
        this.diseaseGroupBean = diseaseGroupBean;
    }
      
    
    public String getAcronym() {
        return acronym;
    }

    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }
    
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }

    
    
    @Override
    public int compareTo(Object t) {
        if(t instanceof DiseaseBean){
            return this.id.compareTo(((DiseaseBean)t).id);
        } else {
            return -1;
        }
    }   
    
}
