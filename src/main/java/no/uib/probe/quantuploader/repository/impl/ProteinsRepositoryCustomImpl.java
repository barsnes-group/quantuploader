
package no.uib.probe.quantuploader.repository.impl;

import com.compomics.util.waiting.WaitingHandler;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import no.uib.probe.quantuploader.beans.QuantDatasetBean;
import no.uib.probe.quantuploader.beans.QuantDatasetPeptideBean;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import no.uib.probe.quantuploader.beans.QuantProteinBean;
import no.uib.probe.quantuploader.repository.ProteinsRepositoryCustom;
import no.uib.probe.quantuploader.beans.QuantDatasetProteinBean;
import no.uib.probe.quantuploader.repository.RepoReturnInfo;
import no.uib.probe.quantuploader.repository.StudiesRepository;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author carlos.horro
 * This class defines and inherits methods to access QuantProteinBean data from the database.
 * These methods must be run into a previously existing transaction.
 */
@Transactional(propagation=Propagation.MANDATORY)
public class ProteinsRepositoryCustomImpl implements ProteinsRepositoryCustom{
    
    /*
     * @PersistenceContext gets the default persistence unit defined in the system.
     * ie: in this case, "my_persistence_unit", defined in JPAConfig.
     * One specific persistence unit may be also declared in the annotation if wanted.
    */
    @PersistenceContext
    private EntityManager entityManager;
    
    @Autowired
    private StudiesRepository studiesRepositoryImpl;
    
    private static final Logger LOGGER = Logger.getLogger(ProteinsRepositoryCustomImpl.class.getName());


    /**
     * Persists or updates QuantProteinBean objects in the system.Also assigns an identifier for every new QuantProteinBean instance.
     *
     * @param proteins
     * @param waitingHandler
     * @return 
     */
    @Override
    public synchronized RepoReturnInfo saveProteins(HashMap<String,QuantProteinBean> proteins,
            WaitingHandler waitingHandler) {
        RepoReturnInfo myReturn = new RepoReturnInfo();
        if (proteins == null) {
            LOGGER.log(Level.WARNING,"Protein collection is null.");
            return myReturn;
        }else{
            LOGGER.log(Level.FINER, "EntityManager: {0}", entityManager);
            int i = 0;
            myReturn.numProcessedEntities = proteins.size();
            for(QuantProteinBean protein : proteins.values()){
                LOGGER.log(Level.FINEST, "protein ({0}): {1}", new Object[]{i, protein.toString()});
                if(!existsProteinByLogicID(protein.getUniprotAccession())){
                    entityManager.persist(protein);
                    myReturn.savedEntities.add(protein);
                    if (waitingHandler != null)
                        waitingHandler.setPrimaryProgressCounter(i+1);
                }else{
                    myReturn.unsavedExistingEntities.add(protein);
                    LOGGER.log(Level.FINEST, "protein ({0}): {1} already exists in DB, not saved", new Object[]{i, protein.toString()});
                }
                if ((i % 10000) == 0) {
                    entityManager.flush();
                    entityManager.clear();
                }
                i++;
            }
            entityManager.flush();
            entityManager.clear();
            LOGGER.log(Level.FINER, "{0} proteins saved",  myReturn.savedEntities.size());
            return myReturn;
        }

    }
    
    /**
     * Persists or updates QuantDatasetProteinBean objects in the system.As the QuantDatasetProteinBean objects have reference to their 
 QuantProteinBean and QuantDatasetBean objects, references
 in the database should be properly kept.
     *
     * @param datasetProteins
     * @return 
     */
    @Override
    public synchronized RepoReturnInfo saveDatasetProteins(HashMap<String,QuantDatasetProteinBean> datasetProteins) {
        return ProteinsRepositoryCustomImpl.this.saveDatasetProteins(datasetProteins, null, null, null);
    }
        
    /**
     * Persists or updates QuantDatasetProteinBean objects in the system.As the QuantDatasetProteinBean objects
 has reference to their QuantProteinBean and QuantDatasetBean objects, references
 in the database should be properly kept.
     *
     * @param datasetProteins
     * @return 
     */
    @Override
    public synchronized RepoReturnInfo saveDatasetProteins(HashMap<String,QuantDatasetProteinBean> datasetProteins, 
            RepoReturnInfo datasetsReturnInfo /*tocheck!*/, RepoReturnInfo proteinsReturnInfo,
            WaitingHandler waitingHandler) {
        RepoReturnInfo myReturn = new RepoReturnInfo();
        if (datasetProteins == null) {
            LOGGER.log(Level.WARNING,"DatasetProtein collection is null.");
            return myReturn;
        }else{
            LOGGER.log(Level.FINER, "EntityManager: {0}", entityManager);
            int i = 0;
            myReturn.numProcessedEntities = datasetProteins.size();

            for(QuantDatasetProteinBean datasetProtein : datasetProteins.values()){
                LOGGER.log(Level.FINEST, "datasetProtein ({0}): {1}", new Object[]{i, datasetProtein.toString()});
                
                QuantDatasetBean parentDataset = datasetProtein.getQuantDatasetBean();
                if(!existsDatasetProteinByLogicId(
                        parentDataset.getQuantStudyBean().getStudyKey(),
                        parentDataset.getSampleType(),
                        parentDataset.getSampleMatching(),
                        parentDataset.getNormalizationStrategy(),
                        parentDataset.getPatientsGroup1(),
                        parentDataset.getPatientsGroup2(),
                        parentDataset.getPatientsSubGroup1(),
                        parentDataset.getPatientsSubGroup2(),
                        datasetProtein.getQuantProteinBean().getUniprotAccession())){
                    
                    // If the parent dataset was not saved because it already existed in the db,
                    // we need to try to reassociate this datasetProtein with the dataset
                    // currently existing in the db and dissassociate it from the not saved dataset 
                    if (datasetsReturnInfo != null && 
                            datasetsReturnInfo.unsavedExistingEntities.contains(
                                    datasetProtein.getQuantDatasetBean())){
                        
                        QuantDatasetBean preexistentDataset = studiesRepositoryImpl.getDatasetByLogicId(
                                parentDataset.getQuantStudyBean().getStudyKey(),
                                parentDataset.getSampleType(),
                                parentDataset.getSampleMatching(),
                                parentDataset.getNormalizationStrategy(),
                                parentDataset.getPatientsGroup1(),
                                parentDataset.getPatientsGroup2(),
                                parentDataset.getPatientsSubGroup1(),
                                parentDataset.getPatientsSubGroup2());
                       
                        datasetProtein.setQuantDatasetBean(preexistentDataset);
                    }
                    
                    // the same for the parent protein
                    if (proteinsReturnInfo != null && 
                            proteinsReturnInfo.unsavedExistingEntities.contains(datasetProtein.getQuantProteinBean())){
                        
                        QuantProteinBean preexistentProtein = getProteinByLogicID(
                                datasetProtein.getQuantProteinBean().getUniprotAccession());
                       
                        datasetProtein.setQuantProteinBean(preexistentProtein);
                    }
                    
                    entityManager.persist(datasetProtein);
                    myReturn.savedEntities.add(datasetProtein);
                    if (waitingHandler != null)
                        waitingHandler.setPrimaryProgressCounter(i+1);
                    if ((i % 10000) == 0) {
                        entityManager.flush();
                        entityManager.clear();
                    }
                }else{
                    LOGGER.log(Level.FINEST, "DatasetProteins ({0}): {1} already exists in DB, not saved", new Object[]{i, datasetProteins.toString()});
                    myReturn.unsavedExistingEntities.add(datasetProtein);
                }
                
                
                i++;
            }
            entityManager.flush();
            entityManager.clear();
            
            LOGGER.log(Level.FINER, "{0} datasetProteins saved", myReturn.savedEntities.size());
            return myReturn;
        }

    }
    
    
    
    /**
     * Persists or updates QuantDatasetPeptideBean objects in the system. As the QuantDatasetPeptideBean objects
     * have reference to their QuantStudyBean object, references
     * in the database should be properly kept.
     *
     * @param datasetPeptides
     */
    @Override
    public synchronized RepoReturnInfo saveDatasetPeptides(HashMap<String,QuantDatasetPeptideBean> datasetPeptides){
        return saveDatasetPeptides(datasetPeptides, null, null, null);
    }
    
    
    /**
     * Persists or updates QuantDatasetPeptideBean objects in the system.As the QuantDatasetPeptideBean objects have reference to their 
 QuantStudyBean object, references in the database should be properly kept.
     *
     * @param datasetPeptides
     * @param datasetsReturnInfo
     * @param datasetProteinsReturnInfo
     * @param waitingHandler
     * @return 
     */
    @Override
    public synchronized RepoReturnInfo saveDatasetPeptides(HashMap<String,QuantDatasetPeptideBean> datasetPeptides,
            RepoReturnInfo datasetsReturnInfo, RepoReturnInfo datasetProteinsReturnInfo,
            WaitingHandler waitingHandler) {
        RepoReturnInfo myReturn = new RepoReturnInfo();
        if (datasetPeptides == null) {
            LOGGER.log(Level.WARNING,"DatasetPeptides collection is null.");
            return myReturn;
        }else{
            LOGGER.log(Level.FINER, "EntityManager: {0}", entityManager);
            int i = 0;
            myReturn.numProcessedEntities = datasetPeptides.size();

            for(QuantDatasetPeptideBean datasetPeptide : datasetPeptides.values()){
                LOGGER.log(Level.FINEST, "datasetPeptide ({0}): {1}", new Object[]{i, datasetPeptide.toString()});
                
                QuantDatasetBean parentDataset = datasetPeptide.getQuantDatasetBean();
                if (!existsDatasetPeptideByLogicId(
                        parentDataset.getQuantStudyBean().getStudyKey(),parentDataset.getSampleType(),
                        parentDataset.getSampleMatching(),parentDataset.getNormalizationStrategy(),
                        parentDataset.getPatientsGroup1(),parentDataset.getPatientsGroup2(),
                        parentDataset.getPatientsSubGroup1(),parentDataset.getPatientsSubGroup2(),
                        datasetPeptide.getQuantDatasetProteinBean().getQuantProteinBean().getUniprotAccession(),
                        datasetPeptide.getPeptideModification(),
                        datasetPeptide.getPeptideSequance())){
                    
                    // If the parent dataset was not saved because it already existed in the db,
                    // we need to try to reassociate this datasetPeptide with the dataset
                    // currently existing in the db and dissassociate it from the not saved dataset 
                    if (datasetsReturnInfo != null && 
                            datasetsReturnInfo.unsavedExistingEntities.contains(parentDataset)){
                        
                        QuantDatasetBean preexistentDataset = studiesRepositoryImpl.getDatasetByLogicId(
                                parentDataset.getQuantStudyBean().getStudyKey(),
                                parentDataset.getSampleType(),
                                parentDataset.getSampleMatching(),
                                parentDataset.getNormalizationStrategy(),
                                parentDataset.getPatientsGroup1(),
                                parentDataset.getPatientsGroup2(),
                                parentDataset.getPatientsSubGroup1(),
                                parentDataset.getPatientsSubGroup2());
                        
                        datasetPeptide.setQuantDatasetBean(preexistentDataset);
                    }
                    
                    // the same for datasetProteins
                    if (datasetProteinsReturnInfo != null && 
                            datasetProteinsReturnInfo.unsavedExistingEntities.contains(
                                    datasetPeptide.getQuantDatasetProteinBean())){
                        
                        QuantDatasetProteinBean preexistentDatasetProtein = getDatasetProteinByLogicId(
                                parentDataset.getQuantStudyBean().getStudyKey(),
                                parentDataset.getSampleType(),
                                parentDataset.getSampleMatching(),
                                parentDataset.getNormalizationStrategy(),
                                parentDataset.getPatientsGroup1(),
                                parentDataset.getPatientsGroup2(),
                                parentDataset.getPatientsSubGroup1(),
                                parentDataset.getPatientsSubGroup2(),
                                datasetPeptide.getQuantDatasetProteinBean().getQuantProteinBean().getUniprotAccession());
                        
                        datasetPeptide.setQuantDatasetProteinBean(preexistentDatasetProtein);
                    }
                    
                    
                    entityManager.persist(datasetPeptide);
                    myReturn.savedEntities.add(datasetPeptide);
                    if (waitingHandler != null)
                        waitingHandler.setPrimaryProgressCounter(i+1);
                    
                    if ((i % 10000) == 0) {
                        entityManager.flush();
                        entityManager.clear();
                    }
                    
                }else{
                    LOGGER.log(Level.FINEST, "datasetPeptide ({0}): {1} already exists in DB, not saved", new Object[]{i, datasetPeptide.toString()});
                    myReturn.unsavedExistingEntities.add(datasetPeptide);
                }
                i++;
            }
            entityManager.flush();
            entityManager.clear();
            LOGGER.log(Level.FINER, "{0} DatasetPeptides saved", myReturn.savedEntities.size());
            return myReturn;
        }
        
    }
    
    
    
    
    // -- Utility methods
    
    /**
     * Utility method that returns if a given protein exists, using its logic id (uniprot accession).
     * @param uniprotAccession
     * @return 
     */
    @Override
    public boolean existsProteinByLogicID(String uniprotAccession) {
        return getProteinDBIdByLogicID(uniprotAccession) != null;
    }
    
    
    /**
     * Utility method that returns a given protein if it exists in the DB, 
     * using its logic id (uniprot accession).
     * @param uniprotAccession
     * @return 
     */
    @Override
    public QuantProteinBean getProteinByLogicID(String uniprotAccession){
        List returnedList = entityManager
                .createQuery("SELECT p FROM " + QuantProteinBean.class.getName()+" p WHERE p.uniprotAccession = :uniprot")
                .setParameter("uniprot", uniprotAccession)
                .getResultList();
        if (returnedList.size()>0) 
            return (QuantProteinBean)returnedList.get(0);
        else
            return null;
    }
    
    /**
     * Utility method that returns a given protein DB id if it exists in the DB, 
     * using its logic id (uniprot accession).
     * @param uniprotAccession
     * @return 
     */
    @Override
    public Long getProteinDBIdByLogicID(String uniprotAccession){
        List returnedList = entityManager
                .createQuery("SELECT p.id FROM " + QuantProteinBean.class.getName()+" p WHERE p.uniprotAccession = :uniprot")
                .setParameter("uniprot", uniprotAccession)
                .getResultList();
        if (returnedList.size()>0) 
            return (Long)returnedList.get(0);
        else
            return null;
    }
    
    // DatasetProteins
    
    
    public boolean existsDatasetProteinByLogicId(String studyKey, String sampleType, String sampleMatching,
            String normalizationStrategy, String patientsGroup1, String patientsGroup2,
            String patientsSubgroup1, String patientsSubgroup2, String uniprotAccession) {
        
        return getDatasetProteinDBIdByLogicId(studyKey, sampleType, sampleMatching,
            normalizationStrategy, patientsGroup1, patientsGroup2,
            patientsSubgroup1, patientsSubgroup2, uniprotAccession)!=null;
    }


    /**
     * Utility method that returns a given dataset protein DB id if it exists, using its logic id 
     * (study key, sample type, sample matching, normalization strategy, patients group 1,
     * patients group 2, patients subgroup1, patients subgroup2 and uniprot accession number ).
     * @param studyKey
     * @param sampleType
     * @param sampleMatching
     * @param normalizationStrategy
     * @param patientsGroup1
     * @param patientsGroup2
     * @param patientsSubgroup1
     * @param patientsSubgroup2
     * @param uniprotAccession
     * @return 
    */
    public Long getDatasetProteinDBIdByLogicId(String studyKey, String sampleType, String sampleMatching,
            String normalizationStrategy, String patientsGroup1, String patientsGroup2,
            String patientsSubgroup1, String patientsSubgroup2, String uniprotAccession) {
            
        String query = 
                "SELECT dp.id FROM " + QuantDatasetProteinBean.class.getName()+
                " dp LEFT JOIN dp.quantDatasetBean d "+
                " LEFT JOIN d.quantStudyBean s "+
                " LEFT JOIN dp.quantProteinBean p "+
                " WHERE s.studyKey = :studykey"+
                " AND d.sampleType = :sampleType"+
                " AND d.sampleMatching = :sampleMatching"+
                " AND d.normalizationStrategy = :normalizationStrategy"+
                " AND d.patientsGroup1 = :patientsGroup1"+
                " AND d.patientsGroup2 = :patientsGroup2"+
                " AND d.patientsSubGroup1 = :patientsSubgroup1"+
                " AND d.patientsSubGroup2 = :patientsSubgroup2"+
                " AND p.uniprotAccession = :uniprotAccession";
        List returnedList = entityManager
                .createQuery(query)
                .setParameter("studykey", studyKey)
                .setParameter("sampleType", sampleType)
                .setParameter("sampleMatching", sampleMatching)
                .setParameter("normalizationStrategy", normalizationStrategy)
                .setParameter("patientsGroup1", patientsGroup1)
                .setParameter("patientsGroup2", patientsGroup2)
                .setParameter("patientsSubgroup1", patientsSubgroup1)
                .setParameter("patientsSubgroup2", patientsSubgroup2)        
                .setParameter("uniprotAccession", uniprotAccession)        
                .getResultList();
        if (returnedList.size()>0) 
            return (Long)returnedList.get(0);
        else
            return null;
    
    }
    
    
    /**
     * Utility method that returns a given dataset protein from the DB if it exists, using its logic id 
     * (study key, sample type, sample matching, normalization strategy, patients group 1,
     * patients group 2, patients subgroup1, patients subgroup2 and uniprot accession number ).
     * @param studyKey
     * @param sampleType
     * @param sampleMatching
     * @param normalizationStrategy
     * @param patientsGroup1
     * @param patientsGroup2
     * @param patientsSubgroup1
     * @param patientsSubgroup2
     * @param uniprotAccession
     * @return 
    */
    public QuantDatasetProteinBean getDatasetProteinByLogicId(String studyKey, String sampleType, String sampleMatching,
            String normalizationStrategy, String patientsGroup1, String patientsGroup2,
            String patientsSubgroup1, String patientsSubgroup2, String uniprotAccession) {
        // exactly the same query than the previous method but returns the entire bean
        String query = 
                "SELECT dp FROM " + QuantDatasetProteinBean.class.getName()+
                " dp LEFT JOIN dp.quantDatasetBean d "+
                " LEFT JOIN d.quantStudyBean s "+
                " LEFT JOIN dp.quantProteinBean p "+
                " WHERE s.studyKey = :studykey"+
                " AND d.sampleType = :sampleType"+
                " AND d.sampleMatching = :sampleMatching"+
                " AND d.normalizationStrategy = :normalizationStrategy"+
                " AND d.patientsGroup1 = :patientsGroup1"+
                " AND d.patientsGroup2 = :patientsGroup2"+
                " AND d.patientsSubGroup1 = :patientsSubgroup1"+
                " AND d.patientsSubGroup2 = :patientsSubgroup2"+
                " AND p.uniprotAccession = :uniprotAccession";
        List returnedList = entityManager
                .createQuery(query)
                .setParameter("studykey", studyKey)
                .setParameter("sampleType", sampleType)
                .setParameter("sampleMatching", sampleMatching)
                .setParameter("normalizationStrategy", normalizationStrategy)
                .setParameter("patientsGroup1", patientsGroup1)
                .setParameter("patientsGroup2", patientsGroup2)
                .setParameter("patientsSubgroup1", patientsSubgroup1)
                .setParameter("patientsSubgroup2", patientsSubgroup2)        
                .setParameter("uniprotAccession", uniprotAccession)        
                .getResultList();
        if (returnedList.size()>0) 
            return (QuantDatasetProteinBean)returnedList.get(0);
        else
            return null;
    
    }
    
    
    // DatasetPeptides
    
    
    /**
     * Utility method that returns if a given dataset peptide exists in the DB, using its logic id 
     * (study key, sample type, sample matching, normalization strategy, patients group 1,
     * patients group 2, patients subgroup1, patients subgroup2, uniprot accession number
     * peptide modification and peptide sequence fields).
     * @param studyKey
     * @param sampleType
     * @param sampleMatching
     * @param normalizationStrategy
     * @param patientsGroup1
     * @param patientsGroup2
     * @param patientsSubgroup1
     * @param patientsSubgroup2
     * @param uniprotAccession
     * @param peptideModification
     * @param peptideSequence
     * @return 
    */
    public boolean existsDatasetPeptideByLogicId(String studyKey, String sampleType, String sampleMatching,
            String normalizationStrategy, String patientsGroup1, String patientsGroup2,
            String patientsSubgroup1, String patientsSubgroup2, String uniprotAccession,
            String peptideModification, String peptideSequence) {
        
        return getDatasetPeptideDBIdByLogicId(studyKey, sampleType, sampleMatching,
            normalizationStrategy, patientsGroup1, patientsGroup2,
            patientsSubgroup1, patientsSubgroup2, uniprotAccession,
            peptideModification, peptideSequence)!=null;
    }
    
    /**
     * Utility method that returns a given dataset peptide db id from the DB if it exists, using its logic id 
     * (study key, sample type, sample matching, normalization strategy, patients group 1,
     * patients group 2, patients subgroup1, patients subgroup2, uniprot accession number, 
     * peptide modification and peptide sequence fields).
     * @param studyKey
     * @param sampleType
     * @param sampleMatching
     * @param normalizationStrategy
     * @param patientsGroup1
     * @param patientsGroup2
     * @param patientsSubgroup1
     * @param patientsSubgroup2
     * @param uniprotAccession
     * @param peptideModification
     * @param peptideSequence
     * @return 
    */
    public Long getDatasetPeptideDBIdByLogicId(String studyKey, String sampleType, String sampleMatching,
            String normalizationStrategy, String patientsGroup1, String patientsGroup2,
            String patientsSubgroup1, String patientsSubgroup2, String uniprotAccession,
            String peptideModification, String peptideSequance) {
   
        String query = 
                "SELECT dpp.id FROM " + QuantDatasetPeptideBean.class.getName()+
                " dpp LEFT JOIN dpp.quantDatasetProteinBean dp "+
                " LEFT JOIN dpp.quantDatasetBean d "+
                " LEFT JOIN d.quantStudyBean s "+
                " LEFT JOIN dp.quantProteinBean p "+
                " WHERE s.studyKey = :studykey"+
                " AND d.sampleType = :sampleType"+
                " AND d.sampleMatching = :sampleMatching"+
                " AND d.normalizationStrategy = :normalizationStrategy"+
                " AND d.patientsGroup1 = :patientsGroup1"+
                " AND d.patientsGroup2 = :patientsGroup2"+
                " AND d.patientsSubGroup1 = :patientsSubgroup1"+
                " AND d.patientsSubGroup2 = :patientsSubgroup2"+
                " AND p.uniprotAccession = :uniprotAccession"+
                " AND dpp.peptideModification = :peptideModification"+
                " AND dpp.peptideSequance = :peptideSequance";

        List returnedList = entityManager
                .createQuery(query)
                .setParameter("studykey", studyKey)
                .setParameter("sampleType", sampleType)
                .setParameter("sampleMatching", sampleMatching)
                .setParameter("normalizationStrategy", normalizationStrategy)
                .setParameter("patientsGroup1", patientsGroup1)
                .setParameter("patientsGroup2", patientsGroup2)
                .setParameter("patientsSubgroup1", patientsSubgroup1)
                .setParameter("patientsSubgroup2", patientsSubgroup2)        
                .setParameter("uniprotAccession", uniprotAccession)     
                .setParameter("peptideModification", peptideModification)     
                .setParameter("peptideSequance", peptideSequance)     
                .getResultList();
        if (returnedList.size()>0) 
            return (Long)returnedList.get(0);
        else
            return null;
    
    }
    
    /**
     * Utility method that returns a given QuantDatasetPeptideBean from the DB if it exists, using its logic id 
     * (study key, sample type, sample matching, normalization strategy, patients group 1,
     * patients group 2, patients subgroup1, patients subgroup2, uniprot accession number, 
     * peptide modification and peptide sequence fields).
     * @param studyKey
     * @param sampleType
     * @param sampleMatching
     * @param normalizationStrategy
     * @param patientsGroup1
     * @param patientsGroup2
     * @param patientsSubgroup1
     * @param patientsSubgroup2
     * @param uniprotAccession
     * @param peptideModification
     * @param peptideSequence
     * @return 
    */
    public QuantDatasetPeptideBean getDatasetPeptideByLogicId(String studyKey, String sampleType, String sampleMatching,
            String normalizationStrategy, String patientsGroup1, String patientsGroup2,
            String patientsSubgroup1, String patientsSubgroup2, String uniprotAccession,
            String peptideModification, String peptideSequance) {
   
        String query = 
                "SELECT dpp FROM " + QuantDatasetPeptideBean.class.getName()+
                " dpp LEFT JOIN dpp.quantDatasetProteinBean dp "+
                " LEFT JOIN dpp.quantDatasetBean d "+
                " LEFT JOIN d.quantStudyBean s "+
                " LEFT JOIN dp.quantProteinBean p "+
                " WHERE s.studyKey = :studykey"+
                " AND d.sampleType = :sampleType"+
                " AND d.sampleMatching = :sampleMatching"+
                " AND d.normalizationStrategy = :normalizationStrategy"+
                " AND d.patientsGroup1 = :patientsGroup1"+
                " AND d.patientsGroup2 = :patientsGroup2"+
                " AND d.patientsSubGroup1 = :patientsSubgroup1"+
                " AND d.patientsSubGroup2 = :patientsSubgroup2"+
                " AND p.uniprotAccession = :uniprotAccession"+
                " AND dpp.peptideModification = :peptideModification"+
                " AND dpp.peptideSequance = :peptideSequance";

        List returnedList = entityManager
                .createQuery(query)
                .setParameter("studykey", studyKey)
                .setParameter("sampleType", sampleType)
                .setParameter("sampleMatching", sampleMatching)
                .setParameter("normalizationStrategy", normalizationStrategy)
                .setParameter("patientsGroup1", patientsGroup1)
                .setParameter("patientsGroup2", patientsGroup2)
                .setParameter("patientsSubgroup1", patientsSubgroup1)
                .setParameter("patientsSubgroup2", patientsSubgroup2)        
                .setParameter("uniprotAccession", uniprotAccession)     
                .setParameter("peptideModification", peptideModification)     
                .setParameter("peptideSequance", peptideSequance)     
                .getResultList();
        if (returnedList.size()>0) 
            return (QuantDatasetPeptideBean)returnedList.get(0);
        else
            return null;
    
    }
    
}
