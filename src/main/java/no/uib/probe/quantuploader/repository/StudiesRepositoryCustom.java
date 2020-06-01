package no.uib.probe.quantuploader.repository;

import com.compomics.util.waiting.WaitingHandler;
import java.util.HashMap;
import java.util.List;
import no.uib.probe.quantuploader.beans.QuantStudyBean;
import no.uib.probe.quantuploader.beans.QuantDatasetBean;
import no.uib.probe.quantuploader.beans.QuantDatasetPeptideBean;

/**
 *
 * @author carlos.horro
 */
//This is not an intermediate repository interface, so we don't have to use : @NoRepositoryBean
//Not necessary anymore in modern spring versions: @Repository
public interface StudiesRepositoryCustom  {
    
    // Methods for saving memory-loaded model
    
    RepoReturnInfo saveStudies(HashMap<String,QuantStudyBean> studies, WaitingHandler waitingHandler);
    
    RepoReturnInfo saveDatasets(HashMap<String,QuantDatasetBean> datasets);
    
    public RepoReturnInfo saveDatasets(HashMap<String,QuantDatasetBean> datasets, 
            RepoReturnInfo studiesReturnInfo, WaitingHandler waitingHandler);
    
    
    // -- Utility methods
    
    // Studies
    
    boolean existsStudyByLogicId(String studyKey);
    
    Long getStudyDBIdByLogicId(String studyKey);
    
    QuantStudyBean getStudyByLogicId(String studyKey);
    
    // Datasets
    
    boolean existsDatasetByLogicId(String studyKey, String sampleType, String sampleMatching,
            String normalizationStrategy, String patientsGroup1, String patientsGroup2,
            String patientsSubgroup1, String patientsSubgroup2);

    Long getDatasetDBIdByLogicId(String studyKey, String sampleType, String sampleMatching,
            String normalizationStrategy, String patientsGroup1, String patientsGroup2,
            String patientsSubgroup1, String patientsSubgroup2);
    
    QuantDatasetBean getDatasetByLogicId(String studyKey, String sampleType, String sampleMatching,
            String normalizationStrategy, String patientsGroup1, String patientsGroup2,
            String patientsSubgroup1, String patientsSubgroup2);
    
    
    List<Long> getDatasetDBIdsByDiseases(String diseaseGroupAcronym, String diseaseAcronym1, String diseaseAcronym2);
}