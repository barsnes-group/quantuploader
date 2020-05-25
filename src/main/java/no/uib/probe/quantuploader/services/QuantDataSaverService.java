package no.uib.probe.quantuploader.services;

import no.uib.probe.quantuploader.beans.QuantDatasetBean;
import no.uib.probe.quantuploader.beans.QuantStudyBean;
import no.uib.probe.quantuploader.beans.QuantProteinBean;
import no.uib.probe.quantuploader.beans.QuantDatasetPeptideBean;
import no.uib.probe.quantuploader.beans.QuantDatasetProteinBean;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerCLIImpl;
import com.compomics.util.waiting.WaitingHandler;
import java.io.Serializable;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import no.uib.probe.quantuploader.repository.ProteinsRepository;
import no.uib.probe.quantuploader.repository.RepoReturnInfo;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import no.uib.probe.quantuploader.repository.StudiesRepository;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;


@Service("quantDataSaverService")
public class QuantDataSaverService implements Serializable {
  
    
	private static final Logger LOGGER = Logger.getLogger(QuantDataSaverService.class.getName());
        
        @Autowired
        private StudiesRepository studyRepositoryImpl;
        
        @Autowired
        private ProteinsRepository proteinRepositoryImpl;
        
        @Autowired
        private TransactionManager transactionManager;
       
        
        private WaitingHandler globalWaitingHandler = null;  
        private WaitingHandler studiesWaitingHandler = null;
        private WaitingHandler proteinsWaitingHandler = null;
        private WaitingHandler datasetsWaitingHandler = null;
        private WaitingHandler datasetProteinsWaitingHandler = null;
        private WaitingHandler datasetPeptidesWaitingHandler = null;
        
        private String lastRunningError;

	public QuantDataSaverService() {  
            initialiseDataStructures();
	}
        
        public void initialiseDataStructures(){
            globalWaitingHandler = null;  
            studiesWaitingHandler = null;
            proteinsWaitingHandler = null;
            datasetsWaitingHandler = null;
            datasetProteinsWaitingHandler = null;
            datasetPeptidesWaitingHandler = null;
            lastRunningError = "";
        }
        
        public WaitingHandler getRefreshedWaitingHandler(){
            int accumulated = 0;
            if (studiesWaitingHandler != null){
                accumulated += studiesWaitingHandler.getPrimaryProgressCounter();
            }
            if (proteinsWaitingHandler != null){
                accumulated += proteinsWaitingHandler.getPrimaryProgressCounter();
            }
            if (datasetsWaitingHandler != null){
                accumulated += datasetsWaitingHandler.getPrimaryProgressCounter();
            }
            if (datasetProteinsWaitingHandler != null){
                accumulated += datasetProteinsWaitingHandler.getPrimaryProgressCounter();          
            }
            if (datasetPeptidesWaitingHandler != null){
                accumulated += datasetPeptidesWaitingHandler.getPrimaryProgressCounter();          
            }
            this.globalWaitingHandler.setPrimaryProgressCounter(accumulated);
            return this.globalWaitingHandler;
        }
        
        /**
         * Main functionality method of this service. It receives HashMaps with loaded 
         * data and try to persist them all into the database in a proper way.
         * @param studies
         * @param proteins
         * @param datasets
         * @param datasetProteins
         * @param datasetPeptides 
         */
        @Transactional(propagation=Propagation.REQUIRES_NEW,isolation=Isolation.SERIALIZABLE,rollbackFor=Exception.class)
        public void saveModel(HashMap<String,QuantStudyBean> studies, HashMap<String,QuantProteinBean> proteins,
                HashMap<String,QuantDatasetBean> datasets, HashMap<String,QuantDatasetProteinBean> datasetProteins,
                HashMap<String,QuantDatasetPeptideBean> datasetPeptides){
            
            LOGGER.log(Level.FINE, "QuantDataSaverService saveModel");

            lastRunningError = "";
            globalWaitingHandler = new WaitingHandlerCLIImpl();
            studiesWaitingHandler = null;
            proteinsWaitingHandler = null;
            datasetsWaitingHandler = null;
            datasetProteinsWaitingHandler = null;
            datasetPeptidesWaitingHandler = null;

            if (studies == null || datasets == null || proteins == null || datasetProteins == null
                    || datasetPeptides == null){
                globalWaitingHandler.setRunCanceled();
                LOGGER.log(Level.INFO, "Some key model info is null. studies:{0}; proteins:{1};datasets:{2};datasetProteins:{3};datasetPeptides:{4}", new Object[]{studies, proteins, datasets,datasetProteins,datasetPeptides});
                return;
            }
            
            globalWaitingHandler.setMaxPrimaryProgressCounter(
                    studies.size()+datasets.size()+datasetPeptides.size()+proteins.size()+datasetProteins.size());
            
            try {
                studiesWaitingHandler = new WaitingHandlerCLIImpl();
                RepoReturnInfo studiesReturnInfo = saveStudies(studies, studiesWaitingHandler);
                
                proteinsWaitingHandler = new WaitingHandlerCLIImpl();
                RepoReturnInfo proteinsReturnInfo = saveProteins(proteins, proteinsWaitingHandler);

                datasetsWaitingHandler = new WaitingHandlerCLIImpl();
                RepoReturnInfo datasetsReturnInfo = saveDatasets(datasets, 
                        studiesReturnInfo, datasetsWaitingHandler);
                
                datasetProteinsWaitingHandler = new WaitingHandlerCLIImpl();
                datasetProteinsWaitingHandler.setMaxPrimaryProgressCounter(datasetProteins.size());
                RepoReturnInfo datasetProteinsReturnInfo = saveDatasetProteins(
                        datasetProteins,studiesReturnInfo, proteinsReturnInfo, datasetProteinsWaitingHandler);
                
                datasetPeptidesWaitingHandler = new WaitingHandlerCLIImpl();
                RepoReturnInfo datasetPeptidesReturnInfo = saveDatasetPeptides(
                        datasetPeptides, datasetsReturnInfo, datasetProteinsReturnInfo, 
                        datasetPeptidesWaitingHandler);

            }catch(Exception e){
                // It would be better to let the exception just raising and the transaction
                // would be rolled back automatically. But it does not allow to add
                // any extra bussiness logic.
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                lastRunningError = e.getLocalizedMessage();
                LOGGER.log(Level.WARNING, "QuantDataSaverService error: ", e);

            }
            LOGGER.log(Level.FINE, "QuantDataSaverService finishing, isRollbackOnly: {0}", TransactionAspectSupport.currentTransactionStatus().isRollbackOnly());
            if (TransactionAspectSupport.currentTransactionStatus().isRollbackOnly()){
                globalWaitingHandler.setRunCanceled();
            }else{
                globalWaitingHandler.setRunFinished();
            }
            
            
        }
        
        
        private RepoReturnInfo saveStudies(HashMap<String,QuantStudyBean> studies,
                WaitingHandler waitingHandler){
            LOGGER.log(Level.FINE, "QuantDataSaverService saveStudies ", studies);
            RepoReturnInfo returnInfo = studyRepositoryImpl.saveStudies(studies, waitingHandler);
            LOGGER.log(Level.FINE, "QuantDataSaverService saveStudies numSaved: {0}", 
                    returnInfo.savedEntities.size());
            return returnInfo;
        }
        
        private RepoReturnInfo saveDatasets(HashMap<String,QuantDatasetBean> datasets, 
                RepoReturnInfo studiesReturnInfo,WaitingHandler waitingHandler){
            LOGGER.log(Level.FINE, "QuantDataSaverService saveDatasets ", datasets);
            RepoReturnInfo returnInfo = studyRepositoryImpl.saveDatasets(
                    datasets, studiesReturnInfo, waitingHandler);
            LOGGER.log(Level.FINE, "QuantDataSaverService saveDatasets numSaved: {0}", 
                    returnInfo.savedEntities.size());
            return returnInfo;
        }
        
        private RepoReturnInfo saveProteins(HashMap<String,QuantProteinBean> proteins,
                WaitingHandler waitingHandler){
            LOGGER.log(Level.FINE, "QuantDataSaverService saveProteins ", proteins);
            RepoReturnInfo returnInfo = proteinRepositoryImpl.saveProteins(proteins,waitingHandler);
            LOGGER.log(Level.FINE, "QuantDataSaverService saveProteins numSaved: {0}", 
                    returnInfo.savedEntities.size());
            return returnInfo;
        }
        
        private RepoReturnInfo saveDatasetProteins(HashMap<String,QuantDatasetProteinBean> datasetProteins, 
                RepoReturnInfo datasetsReturnInfo, RepoReturnInfo proteinsReturnInfo,
                WaitingHandler waitingHandler){
            LOGGER.log(Level.FINE, "QuantDataSaverService saveDatasetProteins", datasetProteins);
            RepoReturnInfo returnInfo = proteinRepositoryImpl.saveDatasetProteins(
                    datasetProteins, datasetsReturnInfo, proteinsReturnInfo, waitingHandler);
            LOGGER.log(Level.FINE, "QuantDataSaverService saveDatasetProteins numSaved: {0}", 
                    returnInfo.savedEntities.size());
            return returnInfo;
        }
        
        private RepoReturnInfo saveDatasetPeptides(HashMap<String,QuantDatasetPeptideBean> datasetPeptides,
                RepoReturnInfo datasetsReturnInfo, RepoReturnInfo datasetProteinsReturnInfo,
                WaitingHandler waitingHandler){
            LOGGER.log(Level.FINE, "QuantDataSaverService saveDatasetPeptides", datasetPeptides);
            RepoReturnInfo returnInfo = proteinRepositoryImpl.saveDatasetPeptides(
                    datasetPeptides, datasetsReturnInfo, datasetProteinsReturnInfo, waitingHandler);
            LOGGER.log(Level.FINE, "QuantDataSaverService saveDatasetPeptides numSaved: {0}", returnInfo.savedEntities.size());
            return returnInfo;
        }


        public String getLastRunningError(){
            return lastRunningError;
        }



}
