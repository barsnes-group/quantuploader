
package no.uib.probe.quantuploader.beans;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * 
 * @author Carlos Horro
 * @author Yehia Farag
 */
@Entity
public class QuantDatasetPeptideBean implements Serializable, Comparable, Identifiable  {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    private QuantDatasetBean quantDatasetBean;
    @ManyToOne
    private QuantDatasetProteinBean quantDatasetProteinBean;
    
    private String peptideSequance,peptideSequenceAnnotated,peptideModification;
    @Column(length = 1000)
    private String modificationComment;
    private Boolean rawDataAvailable;
    private Integer peptideCharge;

    private String stringPValue;
    @Column(length = 1000)
    private String pvalueComment;
    private Double pValue;

    private String significanceThreshold, stringFCValue;
    private Double fcPatientGroupIonPatientGroupII,logFC,rocAuc;

    
    
    public QuantDatasetBean getQuantDatasetBean() {
        return quantDatasetBean;
    }

    public void setQuantDatasetBean(QuantDatasetBean quantDatasetBean) {
        this.quantDatasetBean = quantDatasetBean;
    }
    
    public QuantDatasetProteinBean getQuantDatasetProteinBean() {
        return quantDatasetProteinBean;
    }

    public void setQuantDatasetProteinBean(QuantDatasetProteinBean quantDatasetProteinBean) {
        this.quantDatasetProteinBean = quantDatasetProteinBean;
    }
    
    
    
    /**
     * Derived unique id, from its study and protein related objects
     * One peptide should be identified by its sequence WITH MODIFICATIONS, if there are.
     * @return 
     */
    @Override
    public String getUniqLogicId() {
        // I've removed the uniprot accession number as it may vary over time. Publication accession number should be enough for identification purposes
        // YET: getPublicationAccNumber OR UNIPROT accession number???
        // TO REVIEW: IT SEEMS THAT, SOMETIMES, THERE IS NO MODIFIED PEPTIDE SEQUENCE BUT JUST 
        // THE NORMAL PEPTIDE SEQUENCE, AND THERE MAY BE MORE THAN 1 WITH THE NORMAL PEPTIDE SEQUENCE! TO REVIEW THESE CASES IN ORIGINAL PUBLICATIONS
        // String idPeptideSequence = (this.getPeptideModification() != null)?this.getPeptideModification():this.getPeptideSequance();        
        // return quantDatasetBean.getStudyKey() + "_" + quantDatasetProteinBean.getUniprotAccession() + "_" + idPeptideSequence;
        
        
        // TODO: FROM YEHIA'S CODE: THIS IS HOW PEPTIDE KEY WAS CALCULATED:
        // String pepKey = qProt.getPumedID() + "_" + qProt.getStudyKey() + "_" + qProt.getUniprotAccession() + "_" + qProt.getPublicationAccNumber() + "_" + qProt.getTypeOfStudy() + "_" + qProt.getSampleType() + "_" + qProt.getTechnology() + "_" + qProt.getAnalyticalApproach() + "_" + qProt.getAnalyticalMethod() + "_" + qProt.getPatientsGroupINumber() + "_" + qProt.getPatientsGroupIINumber() + "_" + qProt.getPatientSubGroupI() + "_" + qProt.getPatientSubGroupII() + "_" + qProt.getDiseaseCategory();
        return quantDatasetBean.getUniqLogicId() + "_" + quantDatasetProteinBean.getQuantProteinBean().getUniqLogicId() + "_" + this.getPeptideModification() + "_" + this.getPeptideSequance();
    }
    
    
    
    public double getFcPatientGroupIonPatientGroupII() {
        return fcPatientGroupIonPatientGroupII;
    }

    public void setFcPatientGroupIonPatientGroupII(double fcPatientGroupIonPatientGroupII) {
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
    
    public Double getpValue() {
        return pValue;
    }

    public void setpValue(Double pValue) {
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
    
    public double getRocAuc() {
        return rocAuc;
    }

    public void setRocAuc(double rocAuc) {
        this.rocAuc = rocAuc;
    }
    
    public String getPeptideSequance() {
        return peptideSequance;
    }

    public void setPeptideSequance(String peptideSequance) {
        this.peptideSequance = peptideSequance;
    }
    
    public String getPeptideSequenceAnnotated() {
        return peptideSequenceAnnotated;
    }

    public void setPeptideSequenceAnnotated(String peptideSequenceAnnotated) {
        this.peptideSequenceAnnotated = peptideSequenceAnnotated;
    }
    
    public String getPeptideModification() {
        return peptideModification;
    }

    public void setPeptideModification(String peptideModification) {
        this.peptideModification = peptideModification;
    }
    public String getModificationComment() {
        return modificationComment;
    }

    public void setModificationComment(String modificationComment) {
        this.modificationComment = modificationComment;
    }
    
    public Boolean getRawDataAvailable() {
        return rawDataAvailable;
    }

    public void setRawDataAvailable(Boolean rawDataAvailable) {
        this.rawDataAvailable = rawDataAvailable;
    }
    
    public Integer getPeptideCharge() {
        return peptideCharge;
    }

    public void setPeptideCharge(Integer peptideCharge) {
        this.peptideCharge = peptideCharge;
    }
    
    @Override
    public int compareTo(Object t) {
        if(t instanceof QuantDatasetPeptideBean){
            return this.getUniqLogicId().compareTo(((QuantDatasetPeptideBean)t).getUniqLogicId());
        } else {
            return -1;
        }
    }   
    
}
