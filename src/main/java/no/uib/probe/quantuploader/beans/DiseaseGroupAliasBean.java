
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
 * Utility bean for an easy management of information from the 'diseases_group_alias' sql table
 * This bean itself is NOT persisted to the DB
 */
//@Entity
public class DiseaseGroupAliasBean implements Serializable, Comparable  {
    
    private Integer id;
    
    private DiseaseGroupBean diseaseGroupBean;
    
    private String alias;
    
    
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
      
    
    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
    
    
    @Override
    public int compareTo(Object t) {
        if(t instanceof DiseaseGroupAliasBean){
            return this.id.compareTo(((DiseaseGroupAliasBean)t).id);
        } else {
            return -1;
        }
    }   
    
}
