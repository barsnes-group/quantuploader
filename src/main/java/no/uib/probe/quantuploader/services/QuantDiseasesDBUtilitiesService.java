
package no.uib.probe.quantuploader.services;

import com.compomics.util.waiting.WaitingHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import no.uib.probe.quantuploader.beans.DiseaseBean;
import no.uib.probe.quantuploader.beans.DiseaseGroupAliasBean;
import no.uib.probe.quantuploader.beans.DiseaseGroupBean;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.stereotype.Service;

/**
 *
 * @author carlos.horro
 * This class defines and inherits methods to get pre-loaded diseases data from the database.
 */
@Transactional(propagation=Propagation.REQUIRES_NEW)
@Service("quantDiseasesDBUtilitiesService")
public class QuantDiseasesDBUtilitiesService{
    
    /*
     * @PersistenceContext gets the default persistence unit defined in the system.
     * ie: in this case, "my_persistence_unit", defined in JPAConfig.
     * One specific persistence unit may be also declared in the annotation if wanted.
    */
    @PersistenceContext
    private EntityManager entityManager;

    
    private HashMap<Integer,DiseaseGroupBean> cachedDiseaseGroupsBeansById = null;
    private HashMap<String,DiseaseGroupBean> cachedDiseaseGroupsBeansByAlias = null;
    private HashMap<Integer,DiseaseBean> cachedDiseaseBeansById = null;
    private HashMap<String,Set<DiseaseBean>> cachedDiseaseBeansByAcronym = null;
    private HashMap<Integer,DiseaseGroupAliasBean> cachedDiseaseGroupsAliasesBeansById = null;
        
    
    private static final Logger LOGGER = Logger.getLogger(QuantDiseasesDBUtilitiesService.class.getName());

    
    /**
     * Returns DiseaseBean objects into a HashMap indexed by their id.
     * Objects are returned using cached data previously loaded from the db.
     * If there is no cached data, data is loaded directly from the db itself and cached.
     * @return 
     */
    public HashMap<Integer,DiseaseGroupBean> getAllDiseaseGroupsById(){
        if (cachedDiseaseGroupsBeansById == null 
                || cachedDiseaseBeansById == null
                || cachedDiseaseBeansByAcronym == null
                || cachedDiseaseGroupsAliasesBeansById == null
                || cachedDiseaseGroupsBeansByAlias == null){
            loadDiseaseModel();
        }
        
        return cachedDiseaseGroupsBeansById;
    }
    
    /**
     * Returns DiseaseBean objects into a HashMap indexed by their id.
     * Objects are returned using cached data previously loaded from the db.
     * If there is no cached data, data is loaded directly from the db itself and cached.
     * @return 
     */
    public HashMap<String,DiseaseGroupBean> getAllDiseaseGroupsByAlias(){
        if (cachedDiseaseGroupsBeansById == null 
                || cachedDiseaseBeansById == null
                || cachedDiseaseBeansByAcronym == null
                || cachedDiseaseGroupsAliasesBeansById == null
                || cachedDiseaseGroupsBeansByAlias == null){
            loadDiseaseModel();
        }
        
        return cachedDiseaseGroupsBeansByAlias;
    }
    
    /**
     * Returns DiseaseBean objects into a HashMap indexed by their id.
     * Objects are returned using cached data previously loaded from the db.
     * If there is no cached data, data is loaded directly from the db itself and cached.
     * @return 
     */
    public HashMap<Integer,DiseaseBean> getAllDiseasesById(){
        if (cachedDiseaseGroupsBeansById == null 
                || cachedDiseaseBeansById == null
                || cachedDiseaseBeansByAcronym == null
                || cachedDiseaseGroupsAliasesBeansById == null
                || cachedDiseaseGroupsBeansByAlias == null){
            loadDiseaseModel();
        }
        
        return cachedDiseaseBeansById;
    }
    
    /**
     * Returns DiseaseBean objects into a HashMap indexed by their acronym.
     * Objects are returned using cached data previously loaded from the db.
     * If there is no cached data, data is loaded directly from the db itself and cached.
     * @return 
     */
    public HashMap<String,Set<DiseaseBean>> getAllDiseasesByAcronym(){
        if (cachedDiseaseGroupsBeansById == null 
                || cachedDiseaseBeansById == null
                || cachedDiseaseBeansByAcronym == null
                || cachedDiseaseGroupsAliasesBeansById == null){
            loadDiseaseModel();
        }
        
        return cachedDiseaseBeansByAcronym;
    }
    
    /**
     * Returns DiseaseGroupAliasBean objects into a HashMap indexed by their id.
     * Objects are returned using cached data previously loaded from the db.
     * If there is no cached data, data is loaded directly from the db itself and cached.
     * @return 
     */
    public HashMap<Integer,DiseaseGroupAliasBean> getAllDiseaseGroupsAliasesById(){
        if (cachedDiseaseGroupsBeansById == null 
                || cachedDiseaseBeansById == null
                || cachedDiseaseGroupsAliasesBeansById == null){
            loadDiseaseModel();
        }
        
        return cachedDiseaseGroupsAliasesBeansById;
    }
    
    /**
     * Generates DiseaseBean and DiseaseGroupBean objects loading their related
     * info from the database, and caches them. Previous cached information is lost.
     */
    private void loadDiseaseModel(){

        cachedDiseaseGroupsBeansById = new HashMap<>();
        cachedDiseaseGroupsBeansByAlias = new HashMap<>();
        cachedDiseaseBeansById = new HashMap<>();
        cachedDiseaseBeansByAcronym = new HashMap<>();
        cachedDiseaseGroupsAliasesBeansById = new HashMap<>();

        List<Object[]> diseases = getAllDiseaseRowsFromDB();
        List<Object[]> diseaseGroupsAliases = getAllDiseaseGroupsAliasesRowsFromDB();
        List<Object[]> diseaseGroups = getAllDiseaseGroupRowsFromDB();

        // We load DiseaseGroupBeans
        DiseaseGroupBean diseaseGroupBean;
        Iterator<Object[]> iteratorDiseaseGroups = diseaseGroups.iterator();
        while(iteratorDiseaseGroups.hasNext()){
            Object[] row = iteratorDiseaseGroups.next();
            diseaseGroupBean = new DiseaseGroupBean();
            diseaseGroupBean.setId((Integer)row[0]);
            diseaseGroupBean.setAcronym((String)row[1]);
            diseaseGroupBean.setDescription((String)row[2]);
            cachedDiseaseGroupsBeansById.put(diseaseGroupBean.getId(),diseaseGroupBean);
        }
        // We load DiseaseBeans
        DiseaseBean diseaseBean;
        Iterator<Object[]> iteratorDiseases = diseases.iterator();
        while(iteratorDiseases.hasNext()){
            Object[] row = iteratorDiseases.next();
            diseaseBean = new DiseaseBean();
            diseaseBean.setId((Integer)row[0]);
            diseaseBean.setAcronym((String)row[1]);
            diseaseBean.setDescription((String)row[2]);
            
            (cachedDiseaseGroupsBeansById.get((Integer)row[3])).addDiseaseBean(diseaseBean);
            cachedDiseaseBeansById.put(diseaseBean.getId(),diseaseBean);
            
            Set<DiseaseBean> diseaseBeansWithSameAcronym = cachedDiseaseBeansByAcronym.get(diseaseBean.getAcronym());
            if (diseaseBeansWithSameAcronym == null){
                diseaseBeansWithSameAcronym = new HashSet<>();
            }
            diseaseBeansWithSameAcronym.add(diseaseBean);
            cachedDiseaseBeansByAcronym.put(diseaseBean.getAcronym(),diseaseBeansWithSameAcronym);

        }
        
        // We load DiseaseGroupAliasBeans
        DiseaseGroupAliasBean diseaseGroupAliasBean;
        Iterator<Object[]> iteratorDiseaseGroupsAliases = diseaseGroupsAliases.iterator();
        while(iteratorDiseaseGroupsAliases.hasNext()){
            Object[] row = iteratorDiseaseGroupsAliases.next();
            diseaseGroupAliasBean = new DiseaseGroupAliasBean();
            diseaseGroupAliasBean.setId((Integer)row[0]);
            diseaseGroupAliasBean.setAlias((String)row[1]);
            
            (cachedDiseaseGroupsBeansById.get((Integer)row[2])).addDiseaseGroupAliasBean(diseaseGroupAliasBean);
            cachedDiseaseGroupsAliasesBeansById.put(diseaseGroupAliasBean.getId(),diseaseGroupAliasBean);
            cachedDiseaseGroupsBeansByAlias.put(diseaseGroupAliasBean.getAlias(), cachedDiseaseGroupsBeansById.get((Integer)row[2]));
        }
    }
    
    /**
     * Utility method that returns all disease rows from the database.
     * @return 'diseases' table rows
     */
    private List<Object[]> getAllDiseaseRowsFromDB(){
        List<Object[]> returnedList = entityManager
            .createNativeQuery("SELECT d.id, d.min, d.full, d.disease_group FROM QuantUploaderCSFPR.diseases d")
            .getResultList();
        
        System.out.println("quantDiseasesDBUtilitiesService diseases: "+returnedList.size()+" loaded");
        LOGGER.log(Level.FINE, "quantDiseasesDBUtilitiesService diseases: "+returnedList.size()+" loaded");

        
        return returnedList;
    }
    
    
    /**
     * Utility method that returns all disease group alias rows from the database.
     * @return 'disease_groups_alias' table rows
     */
    private List<Object[]> getAllDiseaseGroupsAliasesRowsFromDB(){
        List<Object[]> returnedList = entityManager
            .createNativeQuery("SELECT dga.id, dga.alias, dga.disease_group FROM QuantUploaderCSFPR.disease_groups_alias dga")
            .getResultList();
        
        System.out.println("quantDiseasesDBUtilitiesService disease groups aliases: "+returnedList.size()+" loaded");
        LOGGER.log(Level.FINE, "quantDiseasesDBUtilitiesService disease groups aliases: "+returnedList.size()+" loaded");

        
        return returnedList;
    }
    
    /**
     * Utility method that returns all disease group rows from the database.
     * @return 'disease_groups' table rows
     */
    private List<Object[]> getAllDiseaseGroupRowsFromDB(){
        List<Object[]> returnedList = entityManager
                .createNativeQuery("SELECT dg.id, dg.min, dg.full FROM QuantUploaderCSFPR.disease_groups dg ")
                .getResultList();
        
        System.out.println("quantDiseasesDBUtilitiesService getAllDiseaseGroupRowsFromDB: "+returnedList.size()+" loaded");
        LOGGER.log(Level.FINE, "quantDiseasesDBUtilitiesService getAllDiseaseGroupRowsFromDB: "+returnedList.size()+" loaded");

        return returnedList;
    }
    
    /**
     * Creates, if they don't exist, the Disease comparisons tables to be used
     * by CSF-PR.
     */
    public void createDiseaseComparisonsTables(){
        System.out.println("createDiseaseComparisonsTables");
        LOGGER.log(Level.FINER, "createDiseaseComparisonsTables");

        String queryDiseasesComparisons = "\n" +
            "CREATE TABLE IF NOT EXISTS `diseases_comparisons` (\n" +
            "  `disease_group_acronym1` varchar(100) NOT NULL ,\n" +
            "  `disease_id1` int NOT NULL,\n" +
            "  `disease_acronym1` varchar(100) NOT NULL ,\n" +
            "  `disease_id2` int NOT NULL,\n" +
            "  `disease_group_acronym2` varchar(100) NOT NULL ,\n" +
            "  `disease_acronym2` varchar(100) NOT NULL,\n" +
            "  `dataset_id` bigint NOT NULL,\n" +
            "  UNIQUE INDEX UQ_compdataset (disease_id1, disease_id2, dataset_id),\n" +
            "  FOREIGN KEY (disease_id1) REFERENCES diseases(id),\n" +
            "  FOREIGN KEY (disease_id2) REFERENCES diseases(id),\n" +
            "  FOREIGN KEY (dataset_id) REFERENCES QuantUploaderCSFPR.QUANTDATASETBEAN(id)  \n" +
            ") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;";
        
        entityManager.createNativeQuery(queryDiseasesComparisons)
            .executeUpdate();
        
        String queryDiseasesComparisonsCount = "\n" +
            "CREATE TABLE IF NOT EXISTS `diseases_comparisons_count` (\n" +
            "  `disease_group_acronym1` varchar(100) NOT NULL ,\n" +
            "  `disease_id1` int NOT NULL,\n" +
            "  `disease_acronym1` varchar(100) NOT NULL ,\n" +
            "  `disease_id2` int NOT NULL,\n" +
            "  `disease_group_acronym2` varchar(100) NOT NULL ,\n" +
            "  `disease_acronym2` varchar(100) NOT NULL ,\n" +
            "  `datasets_count` int NOT NULL,\n" +
            "  UNIQUE INDEX UQ_compdataset (disease_id1, disease_id2),\n" +
            "  FOREIGN KEY (disease_id1) REFERENCES diseases(id),\n" +
            "  FOREIGN KEY (disease_id2) REFERENCES diseases(id) \n" +
            ") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;";
        
        entityManager.createNativeQuery(queryDiseasesComparisonsCount)
            .executeUpdate();
        
    }
    
    /**
     * Function that removes all data from the disease comparisons tables
     */
    public void cleanDiseaseComparisonsTables(){
        
        System.out.println("cleanDiseaseComparisonsTables");
        LOGGER.log(Level.FINER, "cleanDiseaseComparisonsTables");

                
        entityManager.createNativeQuery("TRUNCATE TABLE diseases_comparisons;")
            .executeUpdate();
        
        entityManager.createNativeQuery("TRUNCATE TABLE diseases_comparisons_count;")
            .executeUpdate();
    }
    
    /**
     * Utility method that writes into the DB a comparison between 2 diseases and a reference
     * to the dataset which studied them.
     * @param diseaseBean1 first disease to be compared
     * @param diseaseBean2 second disease to be compared
     * @param datasetIds dataset comparing both diseases
     */
    public void writeDiseaseBeansComparison(
            DiseaseBean diseaseBean1, 
            DiseaseBean diseaseBean2, 
            List<Long> datasetIds){
        
        System.out.println("writeDiseaseBeansComparison: diseaseBean1:"+diseaseBean1.getAcronym()+"; diseaseBean2:"+diseaseBean2.getAcronym()+"; datasetIds size: "+datasetIds.size());
        LOGGER.log(Level.FINEST, "writeDiseaseBeansComparison: diseaseBean1:{0}; diseaseBean2:{1}; datasetIds size: {2}", new Object[]{diseaseBean1.getAcronym(), diseaseBean2.getAcronym(), datasetIds.size()});
        
        // diseases_comparisons_count table
        
        String stringQueryCount = "INSERT INTO diseases_comparisons_count "
                + "(disease_group_acronym1, disease_id1, disease_acronym1, "
                + "disease_group_acronym2, disease_id2, disease_acronym2, datasets_count) VALUES (?,?,?,?,?,?,?)";
        entityManager
                .createNativeQuery(stringQueryCount)
                .setParameter(1, diseaseBean1.getDiseaseGroupBean().getAcronym())
                .setParameter(2, diseaseBean1.getId())
                .setParameter(3, diseaseBean1.getAcronym())
                .setParameter(4, diseaseBean2.getDiseaseGroupBean().getAcronym())
                .setParameter(5, diseaseBean2.getId())
                .setParameter(6, diseaseBean2.getAcronym())
                .setParameter(7, datasetIds.size())
                .executeUpdate();

        // diseases_comparisons table
        String stringQuery = "INSERT INTO diseases_comparisons "
                + "(disease_group_acronym1, disease_id1, disease_acronym1, "
                + "disease_group_acronym2, disease_id2, disease_acronym2, dataset_id) VALUES (?,?,?,?,?,?,?)";

        Iterator<Long> datasetIdsIterator = datasetIds.iterator();
        while(datasetIdsIterator.hasNext()){
            entityManager
                    .createNativeQuery(stringQuery)
                    .setParameter(1, diseaseBean1.getDiseaseGroupBean().getAcronym())
                    .setParameter(2, diseaseBean1.getId())
                    .setParameter(3, diseaseBean1.getAcronym())
                    .setParameter(4, diseaseBean2.getDiseaseGroupBean().getAcronym())
                    .setParameter(5, diseaseBean2.getId())
                    .setParameter(6, diseaseBean2.getAcronym())
                    .setParameter(7, datasetIdsIterator.next())
                    .executeUpdate();
        }

        return;
    }
    
}
