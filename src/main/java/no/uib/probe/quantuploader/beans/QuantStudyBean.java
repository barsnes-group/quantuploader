package no.uib.probe.quantuploader.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import no.uib.probe.quantuploader.enums.Column;
import no.uib.probe.quantuploader.enums.DiseaseCategory;

/**
 * this class represent the publication study object that has all publication
 * information
 *
 * @author Yehia Farag
 */
@Entity
public class QuantStudyBean implements Serializable, Comparable, Identifiable  {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 
     */
    private Integer pumedID;
    private String studyKey;
    private boolean pooledSample;
    @Enumerated(EnumType.STRING)
    private DiseaseCategory diseaseCategory;
    private String author;
    private Integer year;
    private Integer quantifiedProteinsNumber;
    private String analytical_method;
    //private String rawDataUrl;
    private String typeOfStudy;
    private String shotgunTargeted;
    private String technology; // TODO: MAY THE TECHNOLOGY AND OTHER SIMILAR ONES BE DEPENDENT ON THE PROTEIN TOO?
    private String analyticalApproach;
    private String enzyme;
    private String quantificationBasis;
    @javax.persistence.Column(length = 1000)
    private String quantBasisComment;
    @javax.persistence.Column(length = 1000)
    private String additionalComments;
    
    @Transient
    private final HashMap<String,QuantDatasetBean> quantDatasetMap = new HashMap<String,QuantDatasetBean>();

    
    
    /**
     * Adds a QuantDatasetBean, unless it already exists!
     * @param qd 
     */
    public void addDataset(QuantDatasetBean qd){
        if (qd != null && quantDatasetMap.get(qd.getUniqLogicId()) == null){
            qd.setQuantStudyBean(this);
            quantDatasetMap.put(qd.getUniqLogicId(), qd);
        } 
    }
    public List<QuantDatasetBean> getQuantDatasetList(){
        return (List<QuantDatasetBean>) quantDatasetMap.values();
    }
    
    public HashMap<String,QuantDatasetBean> getQuantDatasetMap(){
        return quantDatasetMap;
    }
    
    /**
     * Derived unique id  //TODO: TO REVIEW
     * Feedback by Astrid: it seems studykey WAS a unique id as Astrid added by the end
     * of the number an "A","B", etc. to differentiate them when they had the same (normalization strategy)?NO, IT SEEMS IT IS WHEN THEY HAVE DIFFERENT SAMPLE MATCHING.
     * BUT: I've checked that there are many rows with the same studykey and even with the same sample matching.
     * Feedback by Harald: when we have different comparisons, we still have the same study but with those different comparisons
     * ASTRID WILL COME BACK BY THE WEEK OF THE 2 MARCH.
     * @return 
     */
    @Override
    public String getUniqLogicId() {
        //return uniqId;
        //return this.getStudyKey()+"_"+this.getSampleMatching();
        // TODO: Would it be possible to have different Pubmed ids associated to the same study? It it is not, pubmed id should be removed
        //String uniqueId = this.getPumedID() + "_" + this.getStudyKey() + "_" + "_" + this.getTypeOfStudy() + "_" + this.getSampleType() + "_" + this.getTechnology() + "_" + this.getAnalyticalApproach() + "_" + this.getAnalyticalMethod() + "_" + this.getPatientsGroup1Number() + "_" + this.getPatientsGroup2Number() + "_" + this.getPatientsSubGroup1() + "_" + this.getPatientsSubGroup2() + "_" + this.getDiseaseCategory();
        return this.getStudyKey();
    }
    
        
        
    
    public String getStudyKey() {
        return studyKey;
    }

    public void setStudyKey(String studyKey) {
        this.studyKey = studyKey;
    }
    
    
    public boolean isPooledSample() {
        return pooledSample;
    }

    public void setPooledSample(boolean pooledSample) {
        this.pooledSample = pooledSample;
    }
    
/*
    public String getDiseaseCategory() {
        return diseaseCategory;
    }

    public void setDiseaseCategory(String diseaseCategory) {
        this.diseaseCategory = diseaseCategory;
    }
*/
    
    public DiseaseCategory getDiseaseCategory() {
        return diseaseCategory;
    }

    public void setDiseaseCategory(DiseaseCategory diseaseCategory) {
        this.diseaseCategory = diseaseCategory;
    }
    


    /*
    public Object[] getValues() {
        return values;
    }
    private final Object[] values = new Object[29];
    private final Map<String, Object> valuesMap = new HashMap<String, Object>();
    private String uniqueValues;
    */
    /*
    private final String[] headers = new String[]{"Author", "Year", "#Identified Proteins", "#Quantified Proteins", "Disease Groups", "Raw Data", "#Files", "Study Type", "Sample Type", "Sample Matching", "Shotgun/Targeted", "Technology", "Analytical Approach", "Enzyme", "Quantification Basis", "Quantification Basis Comment","Normalization Strategy", "PumedID","Patients Gr.I", "#Patients Gr.I", "Patients Gr.I Comments", "Patients Sub-Gr.I", "Patients Gr.II","#Patients Gr.II", "Patients Gr.II Comments", "Patients Sub-Gr.II", "Additional Comments","studyKey"};

    public String getFilterTitle(int index) {
        return headers[index];
    }*/

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getQuantifiedProteinsNumber() {
        return quantifiedProteinsNumber;
    }

    public void setQuantifiedProteinsNumber(Integer quantifiedProteinsNumber) {
        this.quantifiedProteinsNumber = quantifiedProteinsNumber;
    }

    public String getAnalyticalMethod() {
        return analytical_method;
    }

    public void setAnalyticalMethod(String analytical_method) {
        this.analytical_method = analytical_method;
    }
/*
    public String getRawDataUrl() {
        return rawDataUrl;
    }

    public void setRawDataUrl(String rawDataUrl) {
        this.rawDataUrl = rawDataUrl;
    }
*/

    public String getTypeOfStudy() {
        return typeOfStudy;
    }

    public void setTypeOfStudy(String typeOfStudy) {
        this.typeOfStudy = typeOfStudy;
    }

    public String getShotgunTargeted() {
        return shotgunTargeted;
    }

    public void setShotgunTargeted(String shotgunTargeted) {
        this.shotgunTargeted = shotgunTargeted;
    }

    public String getTechnology() {
        return technology;
    }

    public void setTechnology(String technology) {
        this.technology = technology;
    }

    public String getAnalyticalApproach() {
        return analyticalApproach;
    }

    public void setAnalyticalApproach(String analyticalApproach) {
        this.analyticalApproach = analyticalApproach;
    }

    public String getEnzyme() {
        return enzyme;
    }

    public void setEnzyme(String enzyme) {
        this.enzyme = enzyme;
    }

    public String getQuantificationBasis() {
        return quantificationBasis;
    }

    public void setQuantificationBasis(String quantificationBasis) {
        this.quantificationBasis = quantificationBasis;
    }

    public String getQuantBasisComment() {
        return quantBasisComment;
    }

    public void setQuantBasisComment(String quantBasisComment) {
        this.quantBasisComment = quantBasisComment;
    }

    public Integer getPumedID() {
        return pumedID;
    }

    public void setPumedID(Integer pumedID) {
        this.pumedID = pumedID;
    }

    public void setAdditionalcomments(String additionalComments) {
        this.additionalComments = additionalComments;
    }

    public String getAdditionalcomments() {
        return additionalComments;
    } 
    
    
    @Override
    public int compareTo(Object t) {
        if(t instanceof QuantStudyBean){
            return this.getUniqLogicId().compareTo(((QuantStudyBean)t).getUniqLogicId());
        } else {
            return -1;
        }
    }

}
