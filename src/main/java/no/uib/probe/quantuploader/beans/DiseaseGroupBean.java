
package no.uib.probe.quantuploader.beans;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

/**
 * 
 * @author Carlos Horro
 * Utility bean for an easy management of information from the 'disease_groups' sql table
 * This bean itself is NOT persisted to the DB
 */
//@Entity
public class DiseaseGroupBean implements Serializable, Comparable  {
    
    private Integer id;
    
    private QuantStudyBean quantStudyBean;
    
    private String acronym;
    private String description;
    
    private final HashMap<Integer,DiseaseGroupAliasBean> diseaseGroupAliasBeanMap = new HashMap<Integer,DiseaseGroupAliasBean>();
    private final HashMap<Integer,DiseaseBean> diseaseBeanMap = new HashMap<Integer,DiseaseBean>();
    
    
    /*
    * Id should not be assigned manually, but just using data.sql loading
    */
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Integer getId() {
        return id;
    }
    
    /**
     * Adds a DiseaseBean, unless it already exists!
     * @param disease 
     */
    public void addDiseaseBean(DiseaseBean disease){
        if (disease != null && diseaseBeanMap.get(disease.getId()) == null){
            disease.setDiseaseGroupBean(this);
            diseaseBeanMap.put(disease.getId(), disease);
        } 
    }
    public List<DiseaseBean> getDiseaseBeanList(){
        return (List<DiseaseBean>) diseaseBeanMap.values();
    }
    
    public HashMap<Integer,DiseaseBean> getDiseaseBeanMap(){
        return diseaseBeanMap;
    }
    
    
    /**
     * Adds a DiseaseGroupAliasBean, unless it already exists!
     * @param diseaseGroupAlias 
     */
    public void addDiseaseGroupAliasBean(DiseaseGroupAliasBean diseaseGroupAlias){
        if (diseaseGroupAlias != null && diseaseBeanMap.get(diseaseGroupAlias.getId()) == null){
            diseaseGroupAlias.setDiseaseGroupBean(this);
            diseaseGroupAliasBeanMap.put(diseaseGroupAlias.getId(), diseaseGroupAlias);
        } 
    }
    public List<DiseaseGroupAliasBean> getDiseaseGroupAliasBeanList(){
        return (List<DiseaseGroupAliasBean>) diseaseGroupAliasBeanMap.values();
    }
    
    public HashMap<Integer,DiseaseGroupAliasBean> getDiseaseGroupAliasBeanMap(){
        return diseaseGroupAliasBeanMap;
    }
    
    
    public QuantStudyBean getQuantStudyBean() {
        return quantStudyBean;
    }

    public void setQuantStudyBean(QuantStudyBean quantStudyBean) {
        this.quantStudyBean = quantStudyBean;
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
        if(t instanceof DiseaseGroupBean){
            return this.id.compareTo(((DiseaseGroupBean)t).id);
        } else {
            return -1;
        }
    }   
    
    
    
}
