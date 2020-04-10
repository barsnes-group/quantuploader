/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package no.uib.probe.quantuploader.repository;

import com.compomics.util.waiting.WaitingHandler;
import java.util.HashMap;
import no.uib.probe.quantuploader.beans.QuantDatasetPeptideBean;
import no.uib.probe.quantuploader.beans.QuantProteinBean;
import no.uib.probe.quantuploader.beans.QuantDatasetProteinBean;

/**
 *
 * @author carlos.horro
 */
public interface ProteinsRepositoryCustom  {
    
    // Methods for saving memory-loaded model

    RepoReturnInfo saveProteins(HashMap<String,QuantProteinBean> proteins,
            WaitingHandler waitingHandler);
    
        
    RepoReturnInfo saveDatasetProteins(HashMap<String,QuantDatasetProteinBean> datasetProteins);
    
    RepoReturnInfo saveDatasetProteins(HashMap<String,QuantDatasetProteinBean> datasetProteins, 
            RepoReturnInfo datasetsReturnInfo, RepoReturnInfo proteinsReturnInfo, 
            WaitingHandler waitingHandler);
    
    
    RepoReturnInfo saveDatasetPeptides(HashMap<String,QuantDatasetPeptideBean> datasetPeptides);
    
    RepoReturnInfo saveDatasetPeptides(HashMap<String,QuantDatasetPeptideBean> datasetPeptides,
            RepoReturnInfo datasetsReturnInfo, RepoReturnInfo datasetProteinsReturnInfo,
            WaitingHandler waitingHandler);


    // -- Utility methods
    
    // Proteins
    boolean existsProteinByLogicID(String uniprotAccession);
    
    QuantProteinBean getProteinByLogicID(String uniprotAccession);
    
    Long getProteinDBIdByLogicID(String uniprotAccession);
    
    // DatasetProteins
    
    Long getDatasetProteinDBIdByLogicId(String studyKey, String sampleType, String sampleMatching,
            String normalizationStrategy, String patientsGroup1, String patientsGroup2,
            String patientsSubgroup1, String patientsSubgroup2, String uniprotAccession);
    
    QuantDatasetProteinBean getDatasetProteinByLogicId(String studyKey, String sampleType, String sampleMatching,
            String normalizationStrategy, String patientsGroup1, String patientsGroup2,
            String patientsSubgroup1, String patientsSubgroup2, String uniprotAccession);

    // DatasetPeptides
    
    boolean existsDatasetPeptideByLogicId(String studyKey, String sampleType, String sampleMatching,
            String normalizationStrategy, String patientsGroup1, String patientsGroup2,
            String patientsSubgroup1, String patientsSubgroup2, String uniprotAccession,
            String peptideModification, String peptideSequence);
    
    QuantDatasetPeptideBean getDatasetPeptideByLogicId(String studyKey, String sampleType, String sampleMatching,
            String normalizationStrategy, String patientsGroup1, String patientsGroup2,
            String patientsSubgroup1, String patientsSubgroup2, String uniprotAccession,
            String peptideModification, String peptideSequence);
    
    Long getDatasetPeptideDBIdByLogicId(String studyKey, String sampleType, String sampleMatching,
            String normalizationStrategy, String patientsGroup1, String patientsGroup2,
            String patientsSubgroup1, String patientsSubgroup2, String uniprotAccession,
            String peptideModification, String peptideSequence);
}