
package no.uib.probe.quantuploader.repository.impl;

import com.compomics.util.waiting.WaitingHandler;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import no.uib.probe.quantuploader.beans.QuantStudyBean;
import org.springframework.transaction.annotation.Transactional;

import no.uib.probe.quantuploader.beans.QuantDatasetBean;
import no.uib.probe.quantuploader.beans.QuantDatasetPeptideBean;
import no.uib.probe.quantuploader.repository.StudiesRepositoryCustom;
import org.springframework.transaction.annotation.Propagation;

import javax.annotation.PostConstruct;
import no.uib.probe.quantuploader.beans.DiseaseBean;
import no.uib.probe.quantuploader.repository.RepoReturnInfo;
import no.uib.probe.quantuploader.repository.StudiesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;


/**
 *
 * @author carlos.horro
 */
// note: StudyRepositoryImpl name follows a convention name. There is another
// convention that says that the name should be StudyRepositoryCustomImpl, but
// with this last one I had many exceptions like "No property saveStudies found for type QuantStudyBean".
// These methods but createStudyViewsAfterDeployment must be run into a previously existing transaction.
@Transactional(propagation=Propagation.MANDATORY)
public class StudiesRepositoryCustomImpl implements StudiesRepositoryCustom{
    
    @PersistenceContext(name="jdbc/quantPersistence", unitName="my_persistence_unit") //name should match to the jndi
    private EntityManager entityManager;
    
    @Autowired
    private PlatformTransactionManager transactionManager;
    
   
    private static final Logger LOGGER = Logger.getLogger(StudiesRepositoryCustomImpl.class.getName());

    /**
     * This is a special method executed just immediately after the creation of this bean.
     * It creates in the DB all study-related views required.
     * It uses its own transaction.
     */
    @PostConstruct
    private void createStudyViewsAfterDeployment(){
        LOGGER.log(Level.FINER,"createViewsAfterDeployment");
        try {
            TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    javax.persistence.Query q = entityManager.createNativeQuery("CREATE OR REPLACE VIEW my_view AS select id FROM quantstudybean");
                    q.executeUpdate();
                }
            });
            
        } catch(Exception e) {
            LOGGER.log(Level.WARNING, "Error creating studies-related views: {0}", e);
        }
    }

    /**
     * Persists or updates QuantStudyBean objects in the system.
     * Also assigns an identifier for every new QuantStudyBean instance.
     *
     * @param studies
     * @param waitingHandler
     * @return 
     */
    @Override
    public synchronized RepoReturnInfo saveStudies(HashMap<String,QuantStudyBean> studies,
            WaitingHandler waitingHandler) {
        RepoReturnInfo myReturn = new RepoReturnInfo();
        if (studies == null) {
            LOGGER.log(Level.WARNING,"Study collection is null.");
            return myReturn;
        }else{
             //  System.out.println("em.getTransaction(): "+entityManager.getTransaction());
           // entityManager.getTransaction().begin();
            LOGGER.log(Level.FINER,"EntityManager: "+entityManager);
            myReturn.numProcessedEntities = studies.size();
            int i = 0;
            for(QuantStudyBean study : studies.values()){
                LOGGER.log(Level.FINEST, "study ({0}): {1}", new Object[]{i, study.toString()});
                if(!existsStudyByLogicId(study.getStudyKey())){
                    entityManager.persist(study);
                    myReturn.savedEntities.add(study);
                    if (waitingHandler != null)
                        waitingHandler.setPrimaryProgressCounter(i+1);
                }else{
                    myReturn.unsavedExistingEntities.add(study);
                    LOGGER.log(Level.FINEST, "study ({0}): {1} already exists in DB, not saved", new Object[]{i, study.toString()});
                }
                if ((i % 10000) == 0) {
                    entityManager.flush();
                    entityManager.clear();
                }
                i++;
            }
            entityManager.flush();
            entityManager.clear();
            LOGGER.log(Level.FINER, "{0} studies saved", myReturn.savedEntities.size());
            return myReturn;
        }

    }
    
    /**
     * Persists or updates QuantDatasetBean objects in the system.As the 
     * QuantDatasetBean objects have reference to their QuantStudyBean object, 
     * references in the database should be properly kept.
     *
     * @param studyDatasets
     * @return Saved studyDatasets (previously existing entities will not be re-saved)
     */
    @Override
    public synchronized RepoReturnInfo saveDatasets(HashMap<String,QuantDatasetBean> studyDatasets) {
        return saveDatasets(studyDatasets, null, null);
    }
        
    @Override
    public synchronized RepoReturnInfo saveDatasets(HashMap<String,QuantDatasetBean> datasets, 
            RepoReturnInfo studiesReturnInfo, WaitingHandler waitingHandler) {
        RepoReturnInfo myReturn = new RepoReturnInfo();
        if (datasets == null) {
            LOGGER.log(Level.WARNING,"Datasets collection is null.");
            return myReturn;
        }else{
            LOGGER.log(Level.FINER, "EntityManager: {0}", entityManager);
            myReturn.numProcessedEntities = datasets.size();
            int i = 0;
            for(QuantDatasetBean dataset : datasets.values()){
                LOGGER.log(Level.FINEST, "dataset ({0}): {1}", new Object[]{i, dataset.toString()}); 
                
                if (!existsDatasetByLogicId(dataset.getQuantStudyBean().getStudyKey(),dataset.getSampleType(),
                        dataset.getSampleMatching(),dataset.getNormalizationStrategy(),
                        dataset.getPatientsGroup1(),dataset.getPatientsGroup2(),
                        dataset.getPatientsSubGroup1(),dataset.getPatientsSubGroup2())){
                    
                    // If the parent study was not saved because it already existed in the db,
                    // we need to try to reassociate this dataset with the study
                    // currently existing in the db and dissassociate it from the not saved study 
                    if (studiesReturnInfo != null && 
                            studiesReturnInfo.unsavedExistingEntities.contains(dataset.getQuantStudyBean())){
                        
                        QuantStudyBean preexistentStudy = getStudyByLogicId(dataset.getQuantStudyBean().getStudyKey());
                        // this is not necessary as we only persists on db the relation from the dataset:
                        //preexistentStudy.addDataset(dataset);
                        dataset.setQuantStudyBean(preexistentStudy);
                    }
                       
                    entityManager.persist(dataset);
                    myReturn.savedEntities.add(dataset);
                    if (waitingHandler != null)
                         waitingHandler.setPrimaryProgressCounter(i+1);
                        
                    if ((i % 10000) == 0) {
                        entityManager.flush();
                        entityManager.clear();
                    }
                }else{
                    LOGGER.log(Level.FINEST, "dataset ({0}): {1} already exists in DB, not saved", new Object[]{i, dataset.toString()});
                    myReturn.unsavedExistingEntities.add(dataset);
                }
                
                i++;
            }
            entityManager.flush();
            entityManager.clear();
            LOGGER.log(Level.FINER, "{0} datasets saved", myReturn.savedEntities.size());
            return myReturn;
        }
        
    }
    
    /**
     * Utility method that returns if a given study exists, using its logic id (study key).
     * @param studyKey
     * @return 
     */
    @Override
    public boolean existsStudyByLogicId(String studyKey) {
        return getStudyDBIdByLogicId(studyKey)!=null;
    }
    
    /**
     * Utility method that returns a given QuantStudyBean object if the study exists, using its logic id (study key).
     * @param studyKey
     * @return 
     */
    @Override
    public QuantStudyBean getStudyByLogicId(String studyKey) {
        List returnedList = entityManager
                .createQuery("SELECT s FROM " + QuantStudyBean.class.getName()+" s WHERE s.studyKey = :studykey")
                .setParameter("studykey", studyKey)
                .getResultList();
        if (returnedList.size()>0){
            return (QuantStudyBean)returnedList.get(0);
        }else 
            return null;
    }
    
    /**
     * Utility method that returns a given study id if the study exists, using its logic id (study key).
     * @param studyKey
     * @return 
     */
    @Override
    public Long getStudyDBIdByLogicId(String studyKey) {
        List returnedList = entityManager
                .createQuery("SELECT s.id FROM " + QuantStudyBean.class.getName()+" s WHERE s.studyKey = :studykey")
                .setParameter("studykey", studyKey)
                .getResultList();
        if (returnedList.size()>0){
            return (Long)returnedList.get(0);
        }else 
            return null;
    }
    
    /**
     * Utility method that returns if a given study dataset exists, using its logic id 
     * (study key, sample type, sample matching, normalization strategy, patients group 1,
     * patients group 2, patients subgroup1, patients subgroup2 ).
     * @param studyKey
     * @param sampleType
     * @param sampleMatching
     * @param normalizationStrategy
     * @param patientsGroup1
     * @param patientsGroup2
     * @param patientsSubgroup1
     * @param patientsSubgroup2
     * @return 
     */
    @Override
    public boolean existsDatasetByLogicId(String studyKey, String sampleType, String sampleMatching,
            String normalizationStrategy, String patientsGroup1, String patientsGroup2,
            String patientsSubgroup1, String patientsSubgroup2) {
        return getDatasetDBIdByLogicId(studyKey, sampleType, sampleMatching,
            normalizationStrategy, patientsGroup1, patientsGroup2,
            patientsSubgroup1, patientsSubgroup2)!=null;
    }
    
    
    /**
     * Utility method that returns a given study dataset id if it exists, using its logic id 
     * (study key, sample type, sample matching, normalization strategy, patients group 1,
     * patients group 2, patients subgroup1, patients subgroup2 ).
     * @param studyKey
     * @param sampleType
     * @param sampleMatching
     * @param normalizationStrategy
     * @param patientsGroup1
     * @param patientsGroup2
     * @param patientsSubgroup1
     * @param patientsSubgroup2
     * @return 
     */
    @Override
    public Long getDatasetDBIdByLogicId(String studyKey, String sampleType, String sampleMatching,
            String normalizationStrategy, String patientsGroup1, String patientsGroup2,
            String patientsSubgroup1, String patientsSubgroup2) {
        String query = 
                "SELECT d.id FROM " + QuantDatasetBean.class.getName()+" d LEFT JOIN d.quantStudyBean s "+
                " WHERE s.studyKey = :studykey"+
                " AND d.sampleType = :sampleType"+
                " AND d.sampleMatching = :sampleMatching"+
                " AND d.normalizationStrategy = :normalizationStrategy"+
                " AND d.patientsGroup1 = :patientsGroup1"+
                " AND d.patientsGroup2 = :patientsGroup2"+
                " AND d.patientsSubGroup1 = :patientsSubgroup1"+
                " AND d.patientsSubGroup2 = :patientsSubgroup2";
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
                .getResultList();
        if (returnedList.size()>0) 
            return (Long)returnedList.get(0);
        else
            return null;
    }
    
    
    
    /**
     * Utility method that returns a given dataset if it exists in the db, using its logic id 
     * (study key, sample type, sample matching, normalization strategy, patients group 1,
     * patients group 2, patients subgroup1, patients subgroup2 ).
     * @param studyKey
     * @param sampleType
     * @param sampleMatching
     * @param normalizationStrategy
     * @param patientsGroup1
     * @param patientsGroup2
     * @param patientsSubgroup1
     * @param patientsSubgroup2
     * @return 
     */
    @Override
    public QuantDatasetBean getDatasetByLogicId(String studyKey, String sampleType, String sampleMatching,
            String normalizationStrategy, String patientsGroup1, String patientsGroup2,
            String patientsSubgroup1, String patientsSubgroup2) {
        // Exactly the same query than the previous method but it returns the entire bean
        String query = 
                "SELECT d FROM " + QuantDatasetBean.class.getName()+" d LEFT JOIN d.quantStudyBean s "+
                " WHERE s.studyKey = :studykey"+
                " AND d.sampleType = :sampleType"+
                " AND d.sampleMatching = :sampleMatching"+
                " AND d.normalizationStrategy = :normalizationStrategy"+
                " AND d.patientsGroup1 = :patientsGroup1"+
                " AND d.patientsGroup2 = :patientsGroup2"+
                " AND d.patientsSubGroup1 = :patientsSubgroup1"+
                " AND d.patientsSubGroup2 = :patientsSubgroup2";
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
                .getResultList();
        if (returnedList.size()>0) 
            return (QuantDatasetBean)returnedList.get(0);
        else
            return null;
    }
    
    
    
    /**
     * Utility method that returns an array of dataset ids if they exist, using .
     * @param diseaseGroupAcronym
     * @param diseaseAcronym1
     * @param diseaseAcronym2
     * @return 
     */
    @Override
    public List<Long> getDatasetDBIdsByDiseases(
            String diseaseGroupAcronym, 
            String diseaseAcronym1, 
            String diseaseAcronym2) {
        
        if (diseaseGroupAcronym == null || diseaseAcronym1 == null || diseaseAcronym2 == null)
            return null;
        
        /*
        As there may be different disease groups when having one disease acronym
        (ie: "health" disease), we must always include the disease group acronym
        into the query.
        */
        String stringQuery = 
                    "SELECT d.id FROM " + QuantDatasetBean.class.getName()+" d LEFT JOIN d.quantStudyBean s "+
                    " WHERE s.diseaseGroupAcronym = :diseaseGroupAcronym ";
        
        stringQuery = stringQuery +
                " AND d.patientsSubGroup1 = :patientsSubgroup1"+
                " AND d.patientsSubGroup2 = :patientsSubgroup2";

            
        javax.persistence.Query query = entityManager
                .createQuery(stringQuery)
                .setParameter("patientsSubgroup1", diseaseAcronym1)
                .setParameter("patientsSubgroup2", diseaseAcronym2);
        query.setParameter("diseaseGroupAcronym", diseaseGroupAcronym);
                
                
        return (List<Long>)query.getResultList();
        
    }
    

    // Example of a custom query
    /*
    public List<QuantStudyBean> findAllCustom() {
        return entityManager
          .createQuery("from " + Foo.class.getName()).getResultList();
    }*/
    
    /*
    public createQueryCustom2(){
        Query q = entityManager.createNativeQuery("CREATE VIEW result_set AS 
                                           select record FROM my_data");
        q.executeUpdate();
    }*/
    
    // Another possibility
    /*
    @Query("<JPQ statement here>")
    List<Account> findByCustomer(Customer customer);
    */

    //@Query(
    //    value = "SELECT id FROM Users ORDER BY id", 
    //    nativeQuery = true)
    
    // they were supposed to work, but they didn't from this service itself
    /*
    boolean existsQuantStudyBeanByStudyKey(String studyKey);
    
    @Query("SELECT count(s)>0 FROM QuantStudyBean s WHERE s.studykey = ?1")
    boolean existsStudyByStudyKey(String studyKey);
    */


}
