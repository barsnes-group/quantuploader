/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
 */
@Entity
public class QuantDatasetBean implements Serializable, Comparable, Identifiable  {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    private QuantStudyBean quantStudyBean;
    
    private String sampleType;
    private String sampleMatching;
    private String normalizationStrategy;
    private String patientsGroup1;
    private Integer patientsGroup1Number;
    @Column(length = 1000)
    private String patientsGroup1Comm;
    private String patientsSubGroup1;
    private String patientsGroup2;
    private int patientsGroup2Number;
    @Column(length = 1000)
    private String patientsGroup2Comm;
    private String patientsSubGroup2;
    
    
    @Transient
    private final HashMap<String,QuantDatasetProteinBean> quantDatasetProtMap = new HashMap<String,QuantDatasetProteinBean>();
    @Transient
    private final HashMap<String,QuantDatasetPeptideBean> quantDatasetPeptideMap = new HashMap<String,QuantDatasetPeptideBean>();
    
    
    
    
    /**
     * Adds a QuantDatasetProteinBean, unless it already exists!
     * @param qdp 
     */
    public void addQuantDatasetProt(QuantDatasetProteinBean qdp){
        if (qdp != null && quantDatasetProtMap.get(qdp.getUniqLogicId()) == null){
            qdp.setQuantDatasetBean(this);
            quantDatasetProtMap.put(qdp.getUniqLogicId(), qdp);
        } 
    }
    public List<QuantDatasetProteinBean> getQuantDatasetProtList(){
        return (List<QuantDatasetProteinBean>) quantDatasetProtMap.values();
    }
    
    public HashMap<String,QuantDatasetProteinBean> getQuantDatasetProtMap(){
        return quantDatasetProtMap;
    }
    
    /**
     * Adds a QuantDatasetPeptideBean, unless it already exists!
     * @param qdp 
     */
    public void addQuantDatasetPeptide(QuantDatasetPeptideBean qdp){
        if (qdp != null && quantDatasetPeptideMap.get(qdp.getUniqLogicId()) == null){
            qdp.setQuantDatasetBean(this);
            quantDatasetPeptideMap.put(qdp.getUniqLogicId(), qdp);
        } 
    }
    public List<QuantDatasetPeptideBean> getQuantDatasetPeptideList(){
        return (List<QuantDatasetPeptideBean>) quantDatasetPeptideMap.values();
    }
    
    public HashMap<String,QuantDatasetPeptideBean> getQuantDatasetPeptideMap(){
        return quantDatasetPeptideMap;
    }
    
    
    
    public QuantStudyBean getQuantStudyBean() {
        return quantStudyBean;
    }

    public void setQuantStudyBean(QuantStudyBean quantStudyBean) {
        this.quantStudyBean = quantStudyBean;
    }
        
    
    /**
     * Derived unique id, from its study key and dataset data-related objects
     * @return 
     */
    @Override
    public String getUniqLogicId() {
        return quantStudyBean.getStudyKey() + "_" + this.getSampleType()+"_"+
                this.getSampleMatching()+"_"+this.getNormalizationStrategy()+
                "_"+(this.getPatientsGroup1()!=null?this.getPatientsGroup1():"") + 
                "_"+(this.getPatientsGroup2()!=null?this.getPatientsGroup2():"") + 
                "_" + (this.getPatientsSubGroup1()!=null?this.getPatientsSubGroup1():"") + 
                "_" + (this.getPatientsSubGroup2()!=null?this.getPatientsSubGroup2():"") ;
    }   
    
    public String getSampleType() {
        return sampleType;
    }

    public void setSampleType(String sampleType) {
        this.sampleType = sampleType;
    }
    
    public String getSampleMatching() {
        return sampleMatching;
    }

    public void setSampleMatching(String sampleMatching) {
        this.sampleMatching = sampleMatching;
    } 
    
    public String getNormalizationStrategy() {
        return normalizationStrategy;
    }

    public void setNormalizationStrategy(String normalizationStrategy) {
        this.normalizationStrategy = normalizationStrategy;
    }
    
    public String getPatientsGroup1() {
        return patientsGroup1;
    }

    public void setPatientsGroup1(String patientsGroup1) {
        this.patientsGroup1 = patientsGroup1;
    }

    public Integer getPatientsGroup1Number() {
        return patientsGroup1Number;
    }

    public void setPatientsGroup1Number(Integer patientsGroup1Number) {
        this.patientsGroup1Number = patientsGroup1Number;
    }

    public String getPatientsGroup1Comm() {
        return patientsGroup1Comm;
    }

    public void setPatientsGroup1Comm(String patientsGroup1Comm) {
        this.patientsGroup1Comm = patientsGroup1Comm;
    }

    public String getPatientsSubGroup1() {
        return patientsSubGroup1;
    }

    public void setPatientsSubGroup1(String patientsSubGroup1) {
        this.patientsSubGroup1 = patientsSubGroup1;
    }

    public String getPatientsGroup2() {
        return patientsGroup2;
    }

    public void setPatientsGroup2(String patientsGroup2) {
        this.patientsGroup2 = patientsGroup2;
    }

    public Integer getPatientsGroup2Number() {
        return patientsGroup2Number;
    }

    public void setPatientsGroup2Number(Integer patientsGroup2Number) {
        this.patientsGroup2Number = patientsGroup2Number;
    }
  
    public String getPatientsGroup2Comm() {
        return patientsGroup2Comm;
    }

    public void setPatientsGroup2Comm(String patientsGroup2Comm) {
        this.patientsGroup2Comm = patientsGroup2Comm;
    }

    public String getPatientsSubGroup2() {
        return patientsSubGroup2;
    }

    public void setPatientsSubGroup2(String patientsSubGroup2) {
        this.patientsSubGroup2 = patientsSubGroup2;
    }

    
    
    
    @Override
    public int compareTo(Object t) {
        if(t instanceof QuantDatasetBean){
            return this.getUniqLogicId().compareTo(((QuantDatasetBean)t).getUniqLogicId());
        } else {
            return -1;
        }
    }   
    
}
