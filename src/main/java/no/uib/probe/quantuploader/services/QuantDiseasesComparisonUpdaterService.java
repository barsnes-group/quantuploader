/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package no.uib.probe.quantuploader.services;

import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerCLIImpl;
import com.compomics.util.waiting.WaitingHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import no.uib.probe.quantuploader.beans.DiseaseBean;
import no.uib.probe.quantuploader.beans.DiseaseGroupAliasBean;
import no.uib.probe.quantuploader.beans.DiseaseGroupBean;
import no.uib.probe.quantuploader.beans.QuantStudyBean;
import no.uib.probe.quantuploader.repository.StudiesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author carlos.horro
 */
@Service("quantDiseasesComparisonUpdaterService")
public class QuantDiseasesComparisonUpdaterService {
    private static final Logger LOGGER = Logger.getLogger(QuantDiseasesComparisonUpdaterService.class.getName());
        
    private WaitingHandler waitingHandler;   
    
    @Autowired
    private StudiesRepository studyRepositoryImpl;
    @Autowired
    private QuantDiseasesDBUtilitiesService diseasesUtilitiesService;
    
    public QuantDiseasesComparisonUpdaterService() {
        waitingHandler = new WaitingHandlerCLIImpl();
    }
    
    /**
    * Function that rebuilds the comparison tables based on the data currently loaded
    * into the system and the pre-loaded disease data available.
    */ 
    @Async
    @Transactional(propagation=Propagation.REQUIRES_NEW,
            isolation=Isolation.SERIALIZABLE,rollbackFor=Exception.class)    
    public boolean updateComparisonsView(){
        boolean result = false;
        
        System.out.println("QuantDiseasesComparisonUpdaterService updateComparisonsView");
        LOGGER.log(Level.FINE, "QuantDiseasesComparisonUpdaterService updateComparisonsView");
        
        HashMap<Integer,DiseaseBean> diseases = diseasesUtilitiesService.getAllDiseasesById();
        
        
        Map<DiseaseBean, Map<DiseaseBean, List>> resultsComparisonsMap = 
                new HashMap<DiseaseBean, Map<DiseaseBean, List>>();

        
        Object[] diseasesArray = diseases.values().toArray();
        DiseaseBean firstDisease = null;
        DiseaseBean secondDisease = null;
        
        for(int i=0;i<diseasesArray.length;i++){
            firstDisease = (DiseaseBean)diseasesArray[i];
            HashMap<DiseaseBean,List> secondDiseaseComparison = new HashMap<>();
            
            for(int j=i+1;j<diseasesArray.length;j++){
                secondDisease = (DiseaseBean)diseasesArray[j];
                
                List<Long> resultList = new ArrayList();
                /* get datasets by the first and second disease they cover
                * The disease group will be ALMOST always the same for them, except
                * when we have some particular dataset which compares diseases 
                * from different groups. Yehia created codes
                * like PD/AD for this.
                */
                if (firstDisease.getDiseaseGroupBean().equals(firstDisease.getDiseaseGroupBean())){           
                    resultList = studyRepositoryImpl.getDatasetDBIdsByDiseases(
                            firstDisease.getDiseaseGroupBean().getAcronym(), 
                            firstDisease.getAcronym(),
                            secondDisease.getAcronym());
                }else{
                    
                    resultList = studyRepositoryImpl.getDatasetDBIdsByDiseases(
                            firstDisease.getDiseaseGroupBean().getAcronym()+"/"+secondDisease.getDiseaseGroupBean().getAcronym(), 
                            firstDisease.getAcronym(),
                            secondDisease.getAcronym());
                    // TODO: We make the same query with the 2 different possibilities for 
                    // disease group acronym. ie: PD/AD , and AD/PD
                    /*
                    resultList. studyRepositoryImpl.getDatasetDBIdsByDiseases(
                            secondDisease.getDiseaseGroupBean().getAcronym()+"/"+firstDisease.getDiseaseGroupBean().getAcronym(), 
                            firstDisease.getAcronym(),
                            secondDisease.getAcronym());
                    */
                    
                    
                }
                
                
                System.out.println("updateComparisons, firstDisease:"+firstDisease.getAcronym()+", secondDisease: "+secondDisease.getAcronym()+":"+resultList.size());
                LOGGER.log(Level.FINE, "updateComparisons, firstDisease:"+firstDisease.getAcronym()+"secondDisease: "+secondDisease.getAcronym()+":"+resultList.size());
                
                secondDiseaseComparison.put(secondDisease, resultList);
                
            }
            
            resultsComparisonsMap.put(firstDisease, secondDiseaseComparison);
            
        }
        
        reCreateComparisons(resultsComparisonsMap);
        
        return result;
    }
    
    /**
     * Function that writes into the DB the comparisons between two diseases and
     * the datasets which cover them.
     * It firstly clean the tables before including the new data.
     * @param comparisonsMap 
     */
    private void reCreateComparisons(Map<DiseaseBean, Map<DiseaseBean, List>> comparisonsMap){
        
        System.out.println("reCreateComparisons, comparisonsMap:"+comparisonsMap);
        LOGGER.log(Level.FINE, "reCreateComparisons, comparisonsMap:"+comparisonsMap);

        
        if (comparisonsMap == null)
            return;
        
        diseasesUtilitiesService.createDiseaseComparisonsTables();
        diseasesUtilitiesService.cleanDiseaseComparisonsTables();
        
        Iterator firstDiseaseIterator = comparisonsMap.keySet().iterator();
        while(firstDiseaseIterator.hasNext()){
            DiseaseBean firstDisease = (DiseaseBean)firstDiseaseIterator.next();
            
            
            HashMap<DiseaseBean,List> secondDiseaseHashMap = (HashMap<DiseaseBean,List>)comparisonsMap.get(firstDisease);
            Iterator secondDiseaseIterator = secondDiseaseHashMap.keySet().iterator();
            
            while(secondDiseaseIterator.hasNext()){
                DiseaseBean secondDisease = (DiseaseBean)secondDiseaseIterator.next();
                
                List<Long> datasetIds = secondDiseaseHashMap.get(secondDisease);
                if (datasetIds.size()>0)
                    diseasesUtilitiesService.writeDiseaseBeansComparison(firstDisease, secondDisease, datasetIds);

            }
        
        }
        
    }
    

}
