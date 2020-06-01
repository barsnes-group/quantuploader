/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package no.uib.probe.quantuploader.components;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import no.uib.probe.quantuploader.services.QuantDataExcelLoaderService;
import no.uib.probe.quantuploader.services.QuantDataSaverService;
import no.uib.probe.quantuploader.services.QuantDiseasesComparisonUpdaterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextStoppedEvent;

/**
 *
 * @author carlos.horro
 */
@SpringComponent
@UIScope
public class UploadImportSavePresenter
{
    
    private static final Logger LOGGER = Logger.getLogger(UploadImportSavePresenter.class.getName());

    
    @Autowired
    private QuantDataExcelLoaderService excelLoaderService;
    @Autowired
    private QuantDataSaverService dataSaverService;
    @Autowired
    private QuantDiseasesComparisonUpdaterService diseasesComparisonUpdaterService;
   
    
    private Timer importingTimerMonitoring;
    
    private Timer dbLoadingTimerMonitoring;

    // MAIN METHODS, MAIN LISTENERS, MAIN EVENTS
   
    public UploadImportSavePresenter(){
    }
    
    
    /**
     * Listener for the ContextStoppedEvent (fired when the application is stopped,
     * undeployed, etc.). 
     * It removes and pending thread running in the system.
     * @param event 
     */
    public void onApplicationEvent(ContextStoppedEvent event) {
        excelLoaderService.getWaitingHandler().setRunCanceled();
        //excelDataService.getRefreshedWaitingHandler().setRunCanceled();
        //backgroundServices.close();
        //executor.shutdownNow();
        finishImportingPendingTasks();
        finishDBLoadingPendingTasks();
        /*if (importingTimerMonitoring != null){
            removeImportingTimerMonitoring();
        }*/
     }
    
    
    
    @ClientCallable
    public void windowClosed() {
        finishImportingPendingTasks();
        finishDBLoadingPendingTasks();
    }
    
    
    // IMPORTING-RELATED METHODS
    
    /**
     * Resets the Import Component to its initial status
     */
    public void initialiseImportData(){
        excelLoaderService.initialiseDataStructures();
    }
    
    public void startImport(InputStream inputStream, String fileName, ImportComponent importComponent){
        UploadImportSavePresenter.ImportDataThread thread = new UploadImportSavePresenter.ImportDataThread(inputStream,fileName, importComponent);
        thread.start();
    }
    
    
    
    /**
     * After an excel file has been successfully uploaded and the SucceededEvent
     * has been fired, one object of this class is created and run.
     * It shows a progress bar and executes the loading process of the excel data.
     */
    public class ImportDataThread extends Thread {
	private InputStream inputStream;
        private String fileName;
        private ImportComponent importComponent;
	private boolean isDone = false;
	
	public ImportDataThread(InputStream inputStream, String fileName, ImportComponent importComponent) {
            this.inputStream = inputStream;
            this.fileName = fileName;
            this.importComponent = importComponent;
            setDaemon(true);
	}
	
	public void started() {
	}

	public boolean isDone() {
            return isDone;
	}
	
        @Override
	public void run() {
            try {                
                if (inputStream == null){
                    importComponent.notifyUploadingError("Data from uploaded file was not properly received");
                }else{      
                    System.out.println("ImportDataThread - initialiseDataStructures");
                    LOGGER.log(Level.FINEST, "ImportDataThread - initialiseDataStructures");
                    excelLoaderService.initialiseDataStructures();
                    System.out.println("ImportDataThread - startImportingTimerMonitoring");
                    LOGGER.log(Level.FINEST, "ImportDataThread - startImportingTimerMonitoring");
                    importingTimerMonitoring = startImportingTimerMonitoring(importComponent);
                    System.out.println("ImportDataThread - importModelFromExcelFile");
                    LOGGER.log(Level.FINEST, "ImportDataThread - importModelFromExcelFile");
                    excelLoaderService.importModelFromExcelFile(inputStream, fileName);
                }
            } finally {
                    isDone = true;
            }
	}
    }
       
    
    /**
     * Method that returns a timer which updates every second the process progress bar
     * with the records managed by the excel loader service.
     * The timer MUST be removed when the app is destroyed.
     * @return The timer (already started).
     */
    private Timer startImportingTimerMonitoring(ImportComponent importComponent){
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                int currentRecord = excelLoaderService.getWaitingHandler().getPrimaryProgressCounter();
                int maxRecords = excelLoaderService.getWaitingHandler().getMaxPrimaryProgressCounter();
                LOGGER.log(Level.FINE, "ProcessingTimer: current: : {0} / {1} ", new Object[]{currentRecord, maxRecords} );

                if (excelLoaderService.getWaitingHandler().isRunCanceled()){
                    importComponent.notifyCancellation();
                }else if (excelLoaderService.getWaitingHandler().isRunFinished()){
                    importComponent.notifyUploadingProgress(0, 0);
                    importComponent.notifyEnding("no records available to be imported");
                }else{
                    if (maxRecords > 0){
                        importComponent.notifyUploadingProgress(currentRecord, maxRecords);
                        
                        // We remove the timer when we reached all records or the importing has finished
                        if (currentRecord == maxRecords || excelLoaderService.getWaitingHandler().isRunFinished()){
                            importComponent.notifyEnding(currentRecord +" records imported");
                        }
                    }// if maxRecords == 0, it may be that the importing procedure is still starting
                }
                
                
            }
        }, 1000, 1000);
        return timer;
    }
    
    /**
     * Finishes, if they still exists, any remaining importing-related task running
     * on the system.
     */
    public void finishImportingPendingTasks(){
        removeImportingTimerMonitoring();
    }
    
    /**
     * Stops and removes from the system the processing timer monitoring.
     */
    private void removeImportingTimerMonitoring(){
        LOGGER.log(Level.FINE, "Trying to remove processing timer monitoring : {0} ", new Object[]{importingTimerMonitoring} );

        if (importingTimerMonitoring != null){
            importingTimerMonitoring.cancel();
            importingTimerMonitoring.purge();
            importingTimerMonitoring = null;
        }
    }
    
    // DB-LOADING RELATED METHODS
    
    /**
     * Resets the Import Component to its initial status
     */
    public void initialiseDBLoadingData(){
        dataSaverService.initialiseDataStructures();
    }
    
    /**
     * Starts the procedure-thread to load the model into the database. 
     * @param dbLoaderComponent 
     */
    public void startDBLoading(DBLoadComponent dbLoaderComponent){
        dataSaverService.initialiseDataStructures();
        UploadImportSavePresenter.DBLoadingDataThread thread = new UploadImportSavePresenter.DBLoadingDataThread(dbLoaderComponent);
        thread.start();
    }
    
    /**
     * Finishes, if they still exists, any remaining db loading-related task running
     * on the system.
     */
    public void finishDBLoadingPendingTasks(){
        removeDBLoadingTimerMonitoring();
    }
    
    
    /**
     * Stops and removes from the system the db loading timer monitoring.
     */
    private void removeDBLoadingTimerMonitoring(){
        LOGGER.log(Level.FINE, "Trying to remove db loading timer monitoring : {0} ", new Object[]{dbLoadingTimerMonitoring} );

        if (dbLoadingTimerMonitoring != null){
            dbLoadingTimerMonitoring.cancel();
            dbLoadingTimerMonitoring.purge();
            dbLoadingTimerMonitoring = null;
        }
    }
    
    
    
    /**
     * After an excel file has been successfully uploaded and the SucceededEvent
     * has been fired, one object of this class is created and run.
     * It shows a progress bar and executes the loading process of the excel data.
     */
    public class DBLoadingDataThread extends Thread {
        private DBLoadComponent dbLoaderComponent;
	private boolean isDone = false;
	
	public DBLoadingDataThread(DBLoadComponent dbLoaderComponent) {
            this.dbLoaderComponent = dbLoaderComponent;
            setDaemon(true);
	}
	
	public void started() {
	}

	public boolean isDone() {
            return isDone;
	}
	
        @Override
	public void run() {
            try {                
                if (excelLoaderService.getStudies() == null 
                        || excelLoaderService.getStudies().isEmpty()
                        || excelLoaderService.getProteins() == null 
                        || excelLoaderService.getProteins().isEmpty()
                        || excelLoaderService.getDatasets() == null 
                        || excelLoaderService.getDatasets().isEmpty()
                        || excelLoaderService.getDatasetProteins() == null  
                        || excelLoaderService.getDatasetProteins().isEmpty()
                        || excelLoaderService.getDatasetPeptides() == null 
                        || excelLoaderService.getDatasetPeptides().isEmpty()){
                    
                    String detailedError = "";
                    if (excelLoaderService.getStudies() == null || excelLoaderService.getStudies().isEmpty() ) 
                        detailedError = "Studies information not present.\n";
                    if (excelLoaderService.getProteins() == null || excelLoaderService.getProteins().isEmpty()) 
                        detailedError += "Proteins information not present.\n";
                    if (excelLoaderService.getDatasets() == null || excelLoaderService.getDatasets().isEmpty()) 
                        detailedError += "StudyDataset information not present.\n";
                    if (excelLoaderService.getDatasetProteins() == null || excelLoaderService.getDatasetProteins().isEmpty()) 
                        detailedError += "DatasetProteins information not present.\n";
                    if (excelLoaderService.getDatasetPeptides() == null || excelLoaderService.getDatasetPeptides().isEmpty()) 
                        detailedError += "Peptides information not present.";
                                       
                    dbLoaderComponent.notifyUploadingError("Imported data misses some key information", detailedError);
                    //diseasesComparisonUpdaterService.updateComparisonsView();

                }else{      
                    dbLoadingTimerMonitoring = startDBLoadingTimerMonitoring(dbLoaderComponent);
                    dataSaverService.saveModel(
                            excelLoaderService.getStudies(), 
                            excelLoaderService.getProteins(),
                            excelLoaderService.getDatasets(),
                            excelLoaderService.getDatasetProteins(),
                            excelLoaderService.getDatasetPeptides());
                    
                    diseasesComparisonUpdaterService.updateComparisonsView();
                    
                }
            } finally {
                    isDone = true;
            }
	}
    }
       
    
    /**
     * Method that returns a timer which updates every second the process progress bar
     * with the records persisted to the db by the excel saver service.
     * The timer MUST be removed when the app is destroyed.
     * @return The timer (already started).
     */
    private Timer startDBLoadingTimerMonitoring(DBLoadComponent dbLoaderComponent){
        
        LOGGER.log(Level.FINE, "startDBLoadingTimerMonitoring " );

        Timer timer = new Timer();
        
        timer.scheduleAtFixedRate(new TimerTask() {
            
            boolean firstIteration = true;
            
            @Override
            public void run() {
                
                // If the waiting handler is still null, we must wait for the 
                // dataSaverService to be started 
                if (dataSaverService.getRefreshedWaitingHandler() == null){
                    LOGGER.log(Level.FINER, "DBLoadingTimer: waiting for process initialization..." );
                    //System.out.println("DBLoadingTimer: waiting for process initialization...");
                    return;
                }
                    
                int currentRecord = dataSaverService.getRefreshedWaitingHandler().getPrimaryProgressCounter();
                int maxRecords = dataSaverService.getRefreshedWaitingHandler().getMaxPrimaryProgressCounter();

                LOGGER.log(Level.FINEST, "DBLoadingTimer: current: : {0} / {1} ", new Object[]{currentRecord, maxRecords} );
                //System.out.println("DBLoadingTimer: current: "+currentRecord+" max: "+ maxRecords+"; firstIteration:"+firstIteration);
                
                if (dataSaverService.getRefreshedWaitingHandler().isRunCanceled()){
                    if (dataSaverService.getLastRunningError() != null)
                        dbLoaderComponent.notifyRunningError("Internal error.", dataSaverService.getLastRunningError());
                    else
                        dbLoaderComponent.notifyCancellation();
                }else{
                    
                    if (maxRecords > 0){
                        if(firstIteration)
                            dbLoaderComponent.notifyUploadingProgress(currentRecord, maxRecords);
                        else
                            dbLoaderComponent.notifyUploadingProgress(currentRecord);
                        
                        // We remove the timer when we reached all records or the loading has finished
                        if (currentRecord == maxRecords || dataSaverService.getRefreshedWaitingHandler().isRunFinished()){
                            dbLoaderComponent.notifyEnding(currentRecord +" records loaded into the db");
                        }
                    }
                }
                
                firstIteration = false;
            }
        }, 5000, 3000);
        return timer;
    }
    
}
