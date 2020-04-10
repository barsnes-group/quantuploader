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
 * @author Yehia Farag
 */
@Entity
public class QuantDatasetProteinBean implements Serializable, Comparable, Identifiable {
    
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    private QuantDatasetBean quantDatasetBean;
    @ManyToOne
    private QuantProteinBean quantProteinBean;

    private String publicationAccNumber,publicationProteinName;
    @Column(length = 1000)
    private String additionalComments;		
    private Integer peptideIdNumb,quantifiedPeptidesNumber;    
    @Column(length = 1000)
    private String modificationComment; 
    private Boolean rawDataAvailable;
    //private boolean peptideObject;
    private String sequance;

    private String stringPValue;
    @Column(length = 1000)
    private String pvalueComment; 
    private Double pValue,rocAuc;
    
    private String significanceThreshold,stringFCValue;
    private Double fcPatientGroupIonPatientGroupII,logFC; // TODO: CHECK THAT fcPatientGroupIonPatientGroupII SHOULD BE HERE AND NOT IN STUDY
    
    private String analyticalMethod;

    @Transient
    private final HashMap<String,QuantDatasetPeptideBean> quantDatasetPeptideMap = new HashMap<String,QuantDatasetPeptideBean>();

    
    
    public QuantDatasetBean getQuantDatasetBean() {
        return quantDatasetBean;
    }

    public void setQuantDatasetBean(QuantDatasetBean quantDatasetBean) {
        this.quantDatasetBean = quantDatasetBean;
    }
    
    public QuantProteinBean getQuantProteinBean() {
        return quantProteinBean;
    }

    public void setQuantProteinBean(QuantProteinBean quantProteinBean) {
        this.quantProteinBean = quantProteinBean;
    }
    
    /**
     * Adds a QuantDatasetPeptideBean, unless it already exists!
     * @param qdp 
     */
    public void addQuantDatasetPeptide(QuantDatasetPeptideBean qdp){
        if (qdp != null  && quantDatasetPeptideMap.get(qdp.getUniqLogicId()) == null){
            qdp.setQuantDatasetProteinBean(this);
            quantDatasetPeptideMap.put(qdp.getUniqLogicId(),qdp);  
        }     
    }
    
    public List<QuantDatasetPeptideBean> getQuantDatasetPeptideList(){
        return (List<QuantDatasetPeptideBean>)quantDatasetPeptideMap.values();
    }
    
    public HashMap<String,QuantDatasetPeptideBean> getQuantDatasetPeptideMap(){
        return quantDatasetPeptideMap;
    }
    
    
    /**
     * Derived unique id, from its study and protein related objects
     * @return 
     */
    @Override
    public String getUniqLogicId() {
        return this.getQuantDatasetBean().getUniqLogicId()+"_"+this.getQuantProteinBean().getUniqLogicId();
    }
    
    
    //move to peptideProt?? 
    
    // dublicate in both??
     public Double getFcPatientGroupIonPatientGroupII() {
        return fcPatientGroupIonPatientGroupII;
    }

    public void setFcPatientGroupIonPatientGroupII(Double fcPatientGroupIonPatientGroupII) {
        this.fcPatientGroupIonPatientGroupII = fcPatientGroupIonPatientGroupII;
    } 
    
    public String getStringFCValue() {
        return stringFCValue;
    }

    public void setStringFCValue(String StringFC) {
        this.stringFCValue = StringFC;
    }
    
    public Double getLogFC() {
        return logFC;
    }

    public void setLogFC(Double logFC) {
        this.logFC = logFC;
    }
    
    public double getpValue() {
        return pValue;
    }

    public void setpValue(double pValue) {
        this.pValue = pValue;
    }
    
    public String getStringPValue() {
        return stringPValue;
    }

    public void setStringPValue(String stringPValue) {
        this.stringPValue = stringPValue;
    }
    
    public String getPvalueComment() {
        return pvalueComment;
    }

    public void setPvalueComment(String pvalueComment) {
        this.pvalueComment = pvalueComment;
    }
    
    public String getSignificanceThreshold() {
        return significanceThreshold;
    }

    public void setSignificanceThreshold(String significanceThreshold) {
        this.significanceThreshold = significanceThreshold;
    }
    
    public Double getRocAuc() {
        return rocAuc;
    }

    public void setRocAuc(Double rocAuc) {
        this.rocAuc = rocAuc;
    }
        
    public String getPublicationAccNumber() {
        return publicationAccNumber;
    }

    public void setPublicationAccNumber(String publicationAccNumber) {
        this.publicationAccNumber = publicationAccNumber;
    }
     public String getPublicationProteinName() {
        return publicationProteinName;
    }

    public void setPublicationProteinName(String publicationProteinName) {
        this.publicationProteinName = publicationProteinName;
    }
    

    public Boolean getRawDataAvailable() {
        return rawDataAvailable;
    }

    public void setRawDataAvailable(Boolean rawDataAvailable) {
        this.rawDataAvailable = rawDataAvailable;
    }
    
    
    public Integer getPeptideIdNumb() {
        return peptideIdNumb;
    }

    public void setPeptideIdNumb(Integer peptideIdNumb) {
        this.peptideIdNumb = peptideIdNumb;
    }
    
      public Integer getQuantifiedPeptidesNumber() {
        return quantifiedPeptidesNumber;
    }

    public void setQuantifiedPeptidesNumber(Integer quantifiedPeptidesNumber) {
        this.quantifiedPeptidesNumber = quantifiedPeptidesNumber;
    }

    public String getAdditionalComments() {
        return additionalComments;
    }

    public void setAdditionalComments(String additionalComments) {
        this.additionalComments = additionalComments;
    }
    
    
    public String getModificationComment() {
        return modificationComment;
    }

    public void setModificationComment(String modificationComment) {
        this.modificationComment = modificationComment;
    }

/*
    public boolean isPeptideObject() {
        return peptideObject;
    }

    public void setPeptideObject(boolean peptideObject) {
        this.peptideObject = peptideObject;
    }
*/
    

    public String getAnalyticalMethod() {
        return analyticalMethod;
    }

    public void setAnalyticalMethod(String analyticalMethod) {
        this.analyticalMethod = analyticalMethod;
    }

    public String getSequance() {
        return sequance;
    }

    public void setSequance(String sequance) {
        this.sequance = sequance;
    }
    
    
     
   @Override
    public int compareTo(Object t) {
        if(t instanceof QuantDatasetProteinBean){
            return this.getUniqLogicId().compareTo(((QuantDatasetProteinBean)t).getUniqLogicId());
        } else {
            return -1;
        }
    }   


   
   

   
    
}
