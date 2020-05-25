
package no.uib.probe.quantuploader.services;


import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerCLIImpl;
import com.compomics.util.waiting.WaitingHandler;
import no.uib.probe.quantuploader.beans.QuantProteinBean;
import no.uib.probe.quantuploader.beans.QuantStudyBean;
import no.uib.probe.quantuploader.beans.QuantDatasetBean;
import no.uib.probe.quantuploader.beans.QuantDatasetPeptideBean;
import no.uib.probe.quantuploader.beans.QuantDatasetProteinBean;
import no.uib.probe.quantuploader.enums.Column;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import no.uib.probe.quantuploader.enums.DiseaseCategory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 *
 * @author carlos.horro
 */
@Service("quantDataExcelLoaderService")
public class QuantDataExcelLoaderService {
    
    private static final LogManager logManager = LogManager.getLogManager();
    static{
        try {
            File file = ResourceUtils.getFile("classpath:logger.properties");
            logManager.readConfiguration(new FileInputStream(file));
        } catch (IOException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }
    
    private static final Logger LOGGER = Logger.getLogger(QuantDataSaverService.class.getName());

    private HashMap<String,QuantStudyBean> studies;
    private HashMap<String,QuantDatasetBean> datasets;
    private HashMap<String,QuantProteinBean> proteins;
    private HashMap<String,QuantDatasetProteinBean> datasetProteins;
    private HashMap<String,QuantDatasetPeptideBean> datasetPeptides;


    private WaitingHandler waitingHandler;     

    
    public QuantDataExcelLoaderService() {
        waitingHandler = new WaitingHandlerCLIImpl();
        initialiseDataStructures();
    }
    
    public void initialiseDataStructures(){
        studies = new HashMap<String,QuantStudyBean>();
        datasets = new HashMap<String,QuantDatasetBean>();
        proteins = new HashMap<String,QuantProteinBean>();
        datasetProteins = new HashMap<String,QuantDatasetProteinBean>();
        datasetPeptides = new HashMap<String,QuantDatasetPeptideBean>();
    }

    
    // Public functions to return imported data
    
    public HashMap<String,QuantStudyBean> getStudies(){
        return studies;
    } 
    public HashMap<String,QuantDatasetBean> getDatasets(){
        return datasets;
    }
    public HashMap<String,QuantProteinBean> getProteins(){
        return proteins;
    }
    public HashMap<String,QuantDatasetProteinBean> getDatasetProteins(){
        return datasetProteins;
    }
    public HashMap<String,QuantDatasetPeptideBean> getDatasetPeptides(){
        return datasetPeptides;
    }
    
    
    public WaitingHandler getWaitingHandler(){
        return this.waitingHandler;
    }


    /**
     * Loads information from an excel file into our quantification data representation.
     *
     * @param excelFilePath String path to the source excel file.
     * @return 
     */
    @Async // run in a separate thread
    public synchronized HashMap<String,QuantStudyBean> importModelFromExcelFile(String excelFilePath) {
        LOGGER.log(Level.INFO, ">> LOADING QUANTDATAEXCELLOADERSERVICE SERVICE CALL BY FILEPATH: {0}", excelFilePath);

        if (excelFilePath == null) {
            LOGGER.log(Level.WARNING,"No excel file path.");
            return null;
        }
        
        try (InputStream inputStream = new FileInputStream(excelFilePath)){
            return importModelFromExcelFile(inputStream, excelFilePath);
        }catch (IOException ioe) {
            waitingHandler.setRunCanceled();
            LOGGER.log(Level.SEVERE, "Error creating input stream from {0}: {1}", new Object[]{excelFilePath, ioe});
        }
        return null;
    }   
        
    /**
     * Main entry point of the loader service.Loads information from an excel file 
     * into our quantification data representation.
     * @param excelInputStream
     * @param excelFileName String name of the source excel file. It may contain the entire path of the file.
     * @return 
     */
    public synchronized HashMap<String,QuantStudyBean> importModelFromExcelFile(InputStream excelInputStream, String excelFileName) {
        LOGGER.log(Level.INFO, ">> LOADING QUANTDATAEXCELLOADERSERVICE CALL BY INPUTSTREAM: {0}", excelFileName);

        if (excelInputStream == null) {
            LOGGER.log(Level.WARNING,"Null excel stream received.");
            waitingHandler.setRunCanceled();
            return null;
        }
        
        Workbook workbook = null;
        try{
            workbook = getWorkbookByExcelType(excelInputStream, excelFileName);
        }catch (IllegalArgumentException iae) {
            LOGGER.log(Level.SEVERE, "It looks like {0} is not an excel file", excelFileName);
            LOGGER.log(Level.SEVERE, "{0}", iae);
            waitingHandler.setRunCanceled();
            return null;
        }catch (IOException ioe) {
            LOGGER.log(Level.SEVERE, "Error managing input stream from {0}", excelFileName);
            LOGGER.log(Level.SEVERE, "{0}", ioe);
            waitingHandler.setRunCanceled();
            return null;
        }
        if (workbook == null){
            LOGGER.log(Level.SEVERE, "Workbook from {0} could not be obtained", excelFileName);
            waitingHandler.setRunCanceled();
            return null;
        }
        
            
        Iterator<Row> rowIterator = workbook.getSheetAt(0).iterator();  // iterator over the first sheet found in the workbook

        QuantStudyBean studyBean = null;
        QuantProteinBean proteinBean = null;
        QuantDatasetBean datasetBean = null;
        QuantDatasetProteinBean datasetProteinBean = null;
        QuantDatasetPeptideBean datasetPeptideBean = null;
        EnumMap<Column, Integer> columnsPositions = null;

        if (rowIterator.hasNext())
            columnsPositions = getColumnsPositions(rowIterator.next());

        Function<Column, String>  getStringValue =  null;
        Function<Column, Integer>  getIntegerValue = null;

        Row currentRow = null;

        int numRows = workbook.getSheetAt(0).getLastRowNum();
        waitingHandler.setMaxPrimaryProgressCounter(numRows);
        
        LOGGER.log(Level.INFO, ">> QUANTDATAEXCELLOADERSERVICE STARTING IMPORTING PROCESS ", excelFileName);

        
        while (rowIterator.hasNext()) {    
            currentRow = rowIterator.next();

            // We prepare utility functions to get values from current row in the easiest possible way.
            getStringValue = getFunctionColumnStringValue(currentRow, columnsPositions);
            getIntegerValue = getFunctionColumnIntegerValue(currentRow, columnsPositions);

            waitingHandler.setPrimaryProgressCounter(currentRow.getRowNum());

            LOGGER.log(Level.FINER, "> RowNumber: {0} / {1}", new Object[]{currentRow.getRowNum(),numRows});

            /***** STUDY STUFF    *****/
            studyBean = loadStudyBean(getStringValue,getIntegerValue);
            if (studyBean == null){
                LOGGER.log(Level.WARNING, "studyBean is null, skipping all remaining row information");
                continue;
            }

            /***** PROTEIN STUFF    *****/

            proteinBean = loadProteinBean(getStringValue,getIntegerValue);
            if (proteinBean == null){
                LOGGER.log(Level.WARNING, "proteinBean is null, skipping all remaining row information");
                continue;
            }

            /***** DATASET STUFF    *****/

            datasetBean = loadDatasetBean(studyBean, getStringValue,getIntegerValue);
            if (datasetBean == null){
                LOGGER.log(Level.WARNING, "datasetBean is null, skipping all remaining row information");
                continue;
            }
            /***** DATASET-PROTEIN STUFF    *****/

            datasetProteinBean = loadDatasetProteinBean(datasetBean, proteinBean, getStringValue,getIntegerValue);
            if (datasetProteinBean == null){
                LOGGER.log(Level.WARNING, "datasetProteinBean is null, skipping all remaining row information");
                continue;
            }

            /***** PEPTIDE STUFF    *****/
            String proteinPeptideString = getStringValue.apply(Column.PROTEIN_PEPTIDE);    // TODO: NOT LOADED (YET) INTO ANY OBJECT             
            if (proteinPeptideString != null && proteinPeptideString.trim().equalsIgnoreCase("Peptide") ){
                datasetPeptideBean = loadDatasetPeptideBean(datasetBean, datasetProteinBean, getStringValue,getIntegerValue);
            }               
            if (datasetPeptideBean == null){
                LOGGER.log(Level.WARNING, "datasetPeptideBean is null, skipping all remaining row information");
                continue;
            }


            LOGGER.log(Level.FINER, "END ROW ; Study: {0}; Protein: {1}; DatasetProtein: {2}; Dataset: {3}; DatasetPeptide: {4}", new Object[]{studyBean.getUniqLogicId(), proteinBean.getUniqLogicId(), datasetProteinBean.getUniqLogicId(), datasetBean.getUniqLogicId(), datasetPeptideBean.getUniqLogicId()});

            if (waitingHandler.isRunCanceled()){
                LOGGER.log(Level.FINE, "Execution cancelled: {0} / {1} ", new Object[]{currentRow.getRowNum(),numRows} );
                break;
            }

        } // end of row iterator

        
        showFinalReport();
        waitingHandler.setRunFinished();

        return studies;
    }
    
    /**
     * Loads into a QuantStudyBean object all information found in one excel row related to studies.
     * @param getStringValue Function that returns a String value from one specified cell of a previously established row
     * @param getIntegerValue Function that returns a Integer value from one specified cell of a previously established row
     * @return QuantStudyBean object
     */
    private QuantStudyBean loadStudyBean(Function<Column, String> getStringValue, Function<Column, Integer> getIntegerValue){
        String currentStudyKey = getUniqueIdStringByBeanClassAndRow(QuantStudyBean.class,getStringValue);

        QuantStudyBean quantStudyBean = studies.get(currentStudyKey);

        // we fill the object if it is new
        if (quantStudyBean == null ){
            LOGGER.log(Level.FINER, "loadStudyBean: creating new quantStudyBean, did not find id {0} ", new Object[]{currentStudyKey});

            try{
                quantStudyBean = new QuantStudyBean();

                quantStudyBean.setPumedID((getIntegerValue.apply(Column.PUBMED_ID)));

                quantStudyBean.setStudyKey(currentStudyKey);                                      

                quantStudyBean.setAuthor((String)getStringValue.apply(Column.AUTHOR));

                quantStudyBean.setYear(getIntegerValue.apply(Column.YEAR));              

                quantStudyBean.setQuantifiedProteinsNumber(getIntegerValue.apply(Column.QUANT_PROTEINS_NUM));  
                
                quantStudyBean.setTypeOfStudy(getStringValue.apply(Column.STUDY_TYPE));                 

                quantStudyBean.setTechnology(getStringValue.apply(Column.TECHNOLOGY));    

                quantStudyBean.setAnalyticalMethod(getStringValue.apply(Column.ANALYTHICAL_METHOD));  
                
                quantStudyBean.setAnalyticalApproach(getStringValue.apply(Column.ANALYTHICAL_APPROACH));    

                quantStudyBean.setShotgunTargeted(getStringValue.apply(Column.QUANT_TYPE));    

                quantStudyBean.setEnzyme(getStringValue.apply(Column.ENZYME));    

                quantStudyBean.setQuantificationBasis(getStringValue.apply(Column.QUANT_BASIS));    

                quantStudyBean.setQuantBasisComment(getStringValue.apply(Column.QUANT_BASIS_COMMENT));    
                
                quantStudyBean.setAdditionalcomments(getStringValue.apply(Column.ADDITIONAL_COMMENTS));    

                // This code should replace well Yehia's code
                quantStudyBean.setDiseaseCategory(DiseaseCategory.getDiseaseGroupByInput(getStringValue.apply(Column.DISEASE_GROUP_CAT)));
                /*
                if (updatedRowArr[index] == null || updatedRowArr[index].trim().equalsIgnoreCase("")) {
                    qProt.setDiseaseCategory(" ");
                    index++;
                } else {
                    String diseaseCat = updatedRowArr[index++];
                    if (diseaseCat.trim().equalsIgnoreCase("MS")) {
                        diseaseCat = "Multiple Sclerosis";
                    } else if (diseaseCat.trim().equalsIgnoreCase("AD")) {
                        diseaseCat = "Alzheimer's";
                    } else if (diseaseCat.trim().equalsIgnoreCase("PD") || diseaseCat.trim().equalsIgnoreCase("Parkinson")) {
                        diseaseCat = "Parkinson's";
                    } else if (diseaseCat.trim().equalsIgnoreCase("ALS")) {
                        diseaseCat = "Amyotrophic Lateral Sclerosis";
                    }

                    quantStudyBean.setDiseaseCategory(diseaseCat);
                }*/
                
                String stringPooledSamples = getStringValue.apply(Column.POOLED_SAMPLES);
                if (stringPooledSamples != null){
                    if (stringPooledSamples.equalsIgnoreCase("Yes"))                    
                        quantStudyBean.setPooledSample(true);
                    else if (stringPooledSamples.equalsIgnoreCase("No"))     
                        quantStudyBean.setPooledSample(false);
                }
                
                
                
                studies.put(quantStudyBean.getUniqLogicId(), quantStudyBean);
            }catch(Exception e){
                System.out.println("loadStudyBean exception:"+e);
                quantStudyBean = null;
            }
        }else{
             LOGGER.log(Level.FINER, "loadStudyBean: reusing quantStudyBean {0}", quantStudyBean.getUniqLogicId());
        }           
        return quantStudyBean;            
    }
    
    
    /**
     * Loads into a QuantDatasetBean object all information found in one excel row related to an study dataset.
     * @param parentStudyBean reference to the QuantStudyBean parent entity of the datasetBean which is being loaded.
     * @param getStringValue Function that returns a String value from one specified cell of a previously established row
     * @param getIntegerValue Function that returns a Integer value from one specified cell of a previously established row
     * @return QuantDatasetBean object
     */
    private QuantDatasetBean loadDatasetBean(QuantStudyBean parentStudyBean, Function<Column, String> getStringValue, Function<Column, Integer> getIntegerValue){
        if (parentStudyBean == null){
            LOGGER.log(Level.WARNING, "loadDatasetBean: parentStudyBean is null");
            return null;
        }
        
        String currentDatasetKey = getUniqueIdStringByBeanClassAndRow(QuantDatasetBean.class,getStringValue);
        QuantDatasetBean quantDatasetBean = datasets.get(currentDatasetKey);

        // we fill the object if it is new
        if (quantDatasetBean == null ){
            LOGGER.log(Level.FINER, "loadDatasetBean: creating new quantDatasetBean, did not find id {0} ; parentStudyBean : {1}", new Object[]{currentDatasetKey,parentStudyBean.getUniqLogicId()});

            quantDatasetBean = new QuantDatasetBean();  
            
            quantDatasetBean.setSampleType(getStringValue.apply(Column.SAMPLE_TYPE));     
            
            quantDatasetBean.setSampleMatching(getStringValue.apply(Column.SAMPLE_MATCHING));    

            quantDatasetBean.setNormalizationStrategy(getStringValue.apply(Column.NORM_STRATEGY)); 

            quantDatasetBean.setPatientsGroup1Number(getIntegerValue.apply(Column.PG1_NUMBER));              

            quantDatasetBean.setPatientsGroup1(getStringValue.apply(Column.PG1));              

            quantDatasetBean.setPatientsSubGroup1(getStringValue.apply(Column.PSG1));              

            quantDatasetBean.setPatientsGroup1Comm(getStringValue.apply(Column.PG1_COMMENT));   

            quantDatasetBean.setPatientsGroup2Number(getIntegerValue.apply(Column.PG2_NUMBER));              

            quantDatasetBean.setPatientsGroup2(getStringValue.apply(Column.PG2));              

            quantDatasetBean.setPatientsSubGroup2(getStringValue.apply(Column.PSG2));              

            quantDatasetBean.setPatientsGroup2Comm(getStringValue.apply(Column.PG2_COMMENT));    

            
            if (quantDatasetBean.getPatientsGroup1() != null){
                //quantStudyBean.setDiseaseCategory(excelFilePath);
                // TODO: WHAT IS  qProt.setPatientGroupII(dieseaseGroup.replace("Controls", "Control")); ??
                // CODE FROM YEHIA'S CODE: If we have patients in group 1 but not in subgroup1, we add (Undefined) by the end of the PatientsGroup1 string
                if (quantDatasetBean.getPatientsSubGroup1() == null || quantDatasetBean.getPatientsSubGroup1().equalsIgnoreCase("")){
                    quantDatasetBean.setPatientsGroup1(quantDatasetBean.getPatientsGroup1()+ " (Undefined)");
                }
            }
            if (quantDatasetBean.getPatientsSubGroup1()!= null){
                //?? quantStudyBean.setDiseaseSubcategory(excelFilePath); ???
            }
            
            if (quantDatasetBean.getPatientsGroup2() != null){
                // CODE FROM YEHIA'S CODE
                if (quantDatasetBean.getPatientsSubGroup2() == null || quantDatasetBean.getPatientsSubGroup2().equalsIgnoreCase("")){
                    quantDatasetBean.setPatientsGroup2(quantDatasetBean.getPatientsGroup2()+ " (Undefined)");
                }
            }
            
            
            quantDatasetBean.setQuantStudyBean(parentStudyBean);
            parentStudyBean.addDataset(quantDatasetBean); 
            datasets.put(quantDatasetBean.getUniqLogicId(), quantDatasetBean);
        }else {
            LOGGER.log(Level.FINER, "loadDatasetBean: reusing quantDatasetBean {0}", quantDatasetBean.getUniqLogicId());
        }
        
        return quantDatasetBean;            
    }
    
    
    /**
     * Loads into a QuantProteinBean object all information found in one excel row related to a protein.
     * @param getStringValue Function that returns a String value from one specified cell of a previously established row
     * @param getIntegerValue Function that returns a Integer value from one specified cell of a previously established row
     * @return QuantProteinBean object
     */
    private QuantProteinBean loadProteinBean(Function<Column, String> getStringValue, Function<Column, Integer> getIntegerValue){
        String currentProteinKey = getUniqueIdStringByBeanClassAndRow(QuantProteinBean.class,getStringValue);

        QuantProteinBean quantProteinBean = proteins.get(currentProteinKey);

        // we fill the object if it is new; if we already found one with the same key, we just return it
        if (quantProteinBean == null ){
            LOGGER.log(Level.FINER, "loadProteinBean: creating new quantProteinBean, did not find id {0}", currentProteinKey);

            quantProteinBean = new QuantProteinBean();
        
            quantProteinBean.setUniprotAccession(getStringValue.apply(Column.ACCESSION_PROT));
                
            quantProteinBean.setUniprotProteinName(getStringValue.apply(Column.PROTEIN_NAME_UNIPROT));


            
            proteins.put(quantProteinBean.getUniqLogicId(), quantProteinBean);
        }else {
            LOGGER.log(Level.FINER, "loadProteinBean: reusing quantProteinBean {0}", quantProteinBean.getUniqLogicId());
        }
        
        return quantProteinBean;            
    }
    
    
    
    /**
     * Loads into a QuantDatasetProteinBean object all information found in one excel row 
     * related to proteins of one dataset publication.
     * @param parentDatasetBean reference to the QuantDatasetBean parent entity of the datasetProteinBean which is being loaded.
     * @param parentProteinBean reference to the QuantProteinBean parent entity of the datasetProteinBean which is being loaded.
     * @param getStringValue Function that returns a String value from one specified cell of a previously established row
     * @param getIntegerValue Function that returns a Integer value from one specified cell of a previously established row
     * @return QuantDatasetProteinBean object
     */
    private QuantDatasetProteinBean loadDatasetProteinBean(
            QuantDatasetBean parentDatasetBean, QuantProteinBean parentProteinBean, 
            Function<Column, String> getStringValue, Function<Column, Integer> getIntegerValue){
        
        if (parentDatasetBean == null){
            LOGGER.log(Level.WARNING, "loadDatasetProteinBean: parentDatasetBean is null");
            return null;
        }
        
        if (parentProteinBean == null){   
            LOGGER.log(Level.WARNING, "loadDatasetProteinBean: parentProteinBean is null");
            return null;
        }
        
        String currentDatasetProteinKey = getUniqueIdStringByBeanClassAndRow(QuantDatasetProteinBean.class,getStringValue);

        //String currentDatasetProteinKey = getStringValue.apply(Column.STUDY_KEY)+"_"+getStringValue.apply(Column.ACCESSION_PROT); // TODO: REVIEW ID!!
        QuantDatasetProteinBean datasetProteinBean = datasetProteins.get(currentDatasetProteinKey);
        
        if (datasetProteinBean == null){
            LOGGER.log(Level.FINER, "loadDatasetProteinBean: creating new datasetProteinBean, did not find id: {0}; parentDatasetBean: {1}; parentProteinBean:{2}", new Object[]{currentDatasetProteinKey, parentDatasetBean.getUniqLogicId(), parentProteinBean.getUniqLogicId()});

            datasetProteinBean = new QuantDatasetProteinBean();
            
            datasetProteinBean.setPublicationAccNumber(getStringValue.apply(Column.ACCESSION_PUB));

            datasetProteinBean.setPublicationProteinName(getStringValue.apply(Column.PROTEIN_NAME_PUB));

            String rawDataAvailableString = getStringValue.apply(Column.RAW_DATA);
            boolean rawDataAvailable = (rawDataAvailableString != null && !rawDataAvailableString.trim().equalsIgnoreCase("") && 
                    rawDataAvailableString.trim().equalsIgnoreCase("No"))?true:false;

            datasetProteinBean.setRawDataAvailable(rawDataAvailable);

            datasetProteinBean.setPeptideIdNumb(getIntegerValue.apply(Column.PEPTIDES_ID_NUMBER));                

            // TODO: IS THIS ITS APPROPIATE PLACE, OR WOULD IT BE BETTER INTO STUDYBEAN?
            datasetProteinBean.setQuantifiedPeptidesNumber(getIntegerValue.apply(Column.QUANT_PEPTIDES_NUMBER));                

            
            String stringDoubleFCRatioPG1PG2 = getStringValue.apply(Column.FC_PG1_PG2);    
            if (stringDoubleFCRatioPG1PG2 != null & !stringDoubleFCRatioPG1PG2.trim().equalsIgnoreCase("")) {
                try {
                    datasetProteinBean.setFcPatientGroupIonPatientGroupII(Double.valueOf(stringDoubleFCRatioPG1PG2.replace(",", ".").replace("?", "-")));
                } catch (NumberFormatException exp) {
                    datasetProteinBean.setFcPatientGroupIonPatientGroupII(-1000000000.0); // TODO: TO CHECK IF THESE VARIABLES ARE NECESSARY
                    datasetProteinBean.setStringFCValue(stringDoubleFCRatioPG1PG2);       // TODO: TO CHECK IF THESE VARIABLES ARE NECESSARY
                }
            } else {
                datasetProteinBean.setFcPatientGroupIonPatientGroupII(-1000000000.0);
                datasetProteinBean.setStringFCValue("Not Provided");
            }
            // TODO : LOG2FC_PG1_PG2 seems to be a NUMERIC column, so could its value be assigned directly? are all these checkups necessary to assign it?
            String stringDoubleLogFCRatioPG1PG2 = getStringValue.apply(Column.LOG2FC_PG1_PG2);    
            if (stringDoubleLogFCRatioPG1PG2 != null && !stringDoubleLogFCRatioPG1PG2.trim().equalsIgnoreCase("")) {
                try {
                    datasetProteinBean.setLogFC(Double.valueOf(stringDoubleLogFCRatioPG1PG2.replace(",", ".").replace("?", "-")));
                    if (datasetProteinBean.getLogFC() > 0) {
                        datasetProteinBean.setStringFCValue("Increased");
                    } else {
                        datasetProteinBean.setStringFCValue("Decreased");
                    }
                } catch (NumberFormatException exp) {
                    datasetProteinBean.setLogFC(-1000000000.0);
                    if (!stringDoubleLogFCRatioPG1PG2.trim().isEmpty()) {
                        datasetProteinBean.setStringFCValue(stringDoubleLogFCRatioPG1PG2.trim());

                    } else {
                        if (datasetProteinBean.getStringFCValue() == null) {      // TODO: TO CHECK IF THESE VARIABLES ARE NECESSARY
                            datasetProteinBean.setStringFCValue("Not Provided");  // TODO: TO CHECK IF THESE VARIABLES ARE NECESSARY
                        }
                    }
                }
            } else {
                datasetProteinBean.setLogFC(-1000000000.0);
                if (datasetProteinBean.getStringFCValue() == null) {
                    datasetProteinBean.setStringFCValue("Not Provided");
                }
            }
                        

            // TODO: CALCULUS EXTRACTED FROM YEHIA'S CODE. TO CHECK IF THERE ARE STILL NECESSARY OR NOT.
            String stringTempPvalue = getStringValue.apply(Column.PVALUE);
            if (stringTempPvalue != null && !stringTempPvalue.trim().equalsIgnoreCase("")) {          
                datasetProteinBean = definePValueQuantDatasetProtein(datasetProteinBean, stringTempPvalue, getStringValue.apply(Column.SIGNIFICANCE_TH), getStringValue.apply(Column.PVALUE_COMMENT));
            } else {
                datasetProteinBean.setpValue(-1000000000.0);
                datasetProteinBean.setStringPValue(" ");
                datasetProteinBean.setPvalueComment(" ");
                datasetProteinBean.setSignificanceThreshold(" ");
            }
            
            // TODO: CALCULUS EXTRACTED FROM YEHIA'S CODE. TO CHECK IF THERE ARE STILL NECESSARY OR NOT.
            String stringRocAuc = getStringValue.apply(Column.ROC_AUC);
            if (!stringRocAuc.trim().equalsIgnoreCase("")) {
                datasetProteinBean.setRocAuc(Double.valueOf(stringRocAuc.replace(",", ".").replace("?", "-")));
            } else {
                datasetProteinBean.setRocAuc(-1000000000.0);
            }
            
            
            
            datasetProteinBean.setQuantDatasetBean(parentDatasetBean);
            datasetProteinBean.setQuantProteinBean(parentProteinBean);
            parentProteinBean.addQuantDatasetProt(datasetProteinBean);
            parentDatasetBean.addQuantDatasetProt(datasetProteinBean);
            datasetProteins.put(datasetProteinBean.getUniqLogicId(),datasetProteinBean);
        }else{
            LOGGER.log(Level.FINER, "loadDatasetProteinBean: reusing datasetProteinBean "+datasetProteinBean.getUniqLogicId()+", parentDatasetBean: "+parentDatasetBean.getUniqLogicId()+"; parentProteinBean:"+parentProteinBean.getUniqLogicId());
        }
                        
        return datasetProteinBean;            
    }
    
    /**
     * Loads into a QuantDatasetPeptideBean object all information found in one excel 
     * row related to one peptide.
     * @param parentDatasetBean reference to the QuantDatasetBean parent entity of 
     * the datasetPeptideBean which is being loaded.    
     * @param parentDatasetProteinBean reference to the QuantDatasetProteinBean 
     * parent entity of the datasetPeptideBean which is being loaded.
     * @param getStringValue Function that returns a String value from one specified 
     * cell of a previously established row
     * @param getIntegerValue Function that returns a Integer value from one specified 
     * cell of a previously established row
     * @return QuantDatasetPeptideBean object
     */
    private QuantDatasetPeptideBean loadDatasetPeptideBean(
            QuantDatasetBean parentDatasetBean, QuantDatasetProteinBean parentDatasetProteinBean, 
            Function<Column, String> getStringValue, Function<Column, Integer> getIntegerValue){
        if (parentDatasetBean == null){
            LOGGER.log(Level.WARNING, "loadDatasetPeptideBean: parentDatasetBean is null");
            return null;    
        }
        if (parentDatasetProteinBean == null){
            LOGGER.log(Level.WARNING, "loadDatasetPeptideBean: parenDatasetProteinBean is null");
            return null;    
        }
        
        String currentDatasetPeptideKey = getUniqueIdStringByBeanClassAndRow(QuantDatasetPeptideBean.class,getStringValue);
        //String currentDatasetPeptideKey = getStringValue.apply(Column.STUDY_KEY)+"_"+getStringValue.apply(Column.ACCESSION_PROT); // TODO: REVIEW ID!!
        QuantDatasetPeptideBean quantDatasetPeptideBean = datasetPeptides.get(currentDatasetPeptideKey);
        
        if (quantDatasetPeptideBean == null){
            LOGGER.log(Level.FINER, "loadDatasetPeptideBean: creating new quantDatasetPeptideBean, did not find id {0}; parentDatasetBean: {1}; parentDatasetProteinBean:{2}", new Object[]{currentDatasetPeptideKey,parentDatasetBean.getUniqLogicId(), parentDatasetProteinBean.getUniqLogicId()});

            quantDatasetPeptideBean = new QuantDatasetPeptideBean();

            String rawDataAvailableString = getStringValue.apply(Column.RAW_DATA);
            boolean rawDataAvailable = (rawDataAvailableString != null && !rawDataAvailableString.trim().equalsIgnoreCase("") && 
                                        rawDataAvailableString.trim().equalsIgnoreCase("No"))?true:false;

            quantDatasetPeptideBean.setRawDataAvailable(rawDataAvailable);

            quantDatasetPeptideBean.setPeptideCharge(getIntegerValue.apply(Column.PEPTIDE_CHARGE));

            quantDatasetPeptideBean.setPeptideSequance(getStringValue.apply(Column.PEPTIDE_SEQUENCE));

            quantDatasetPeptideBean.setPeptideSequenceAnnotated(getStringValue.apply(Column.PEPTIDE_SEQUENCE_ANNOTATED));

            quantDatasetPeptideBean.setPeptideModification(getStringValue.apply(Column.PEPTIDE_MODS));

            quantDatasetPeptideBean.setModificationComment(getStringValue.apply(Column.PEPTIDE_COMMENT_MOD));
        
            
            // FUNCTIONALITY DUPLICATED FROM DATASETPROTEINBEAN
            String stringDoubleFCRatioPG1PG2 = getStringValue.apply(Column.FC_PG1_PG2);    
            if (stringDoubleFCRatioPG1PG2 != null & !stringDoubleFCRatioPG1PG2.trim().equalsIgnoreCase("")) {
                try {
                    quantDatasetPeptideBean.setFcPatientGroupIonPatientGroupII(Double.valueOf(stringDoubleFCRatioPG1PG2.replace(",", ".").replace("?", "-")));
                } catch (NumberFormatException exp) {
                    quantDatasetPeptideBean.setFcPatientGroupIonPatientGroupII(-1000000000.0); // TODO: TO CHECK IF THESE VARIABLES ARE NECESSARY
                    quantDatasetPeptideBean.setStringFCValue(stringDoubleFCRatioPG1PG2);       // TODO: TO CHECK IF THESE VARIABLES ARE NECESSARY
                }
            } else {
                quantDatasetPeptideBean.setFcPatientGroupIonPatientGroupII(-1000000000.0);
                quantDatasetPeptideBean.setStringFCValue("Not Provided");
            }
            // TODO : LOG2FC_PG1_PG2 seems to be a NUMERIC column, so could its value be assigned directly? are all these checkups necessary to assign it?
            String stringDoubleLogFCRatioPG1PG2 = getStringValue.apply(Column.LOG2FC_PG1_PG2);    
            if (stringDoubleLogFCRatioPG1PG2 != null && !stringDoubleLogFCRatioPG1PG2.trim().equalsIgnoreCase("")) {
                try {
                    quantDatasetPeptideBean.setLogFC(Double.valueOf(stringDoubleLogFCRatioPG1PG2.replace(",", ".").replace("?", "-")));
                    if (quantDatasetPeptideBean.getLogFC() > 0) {
                        quantDatasetPeptideBean.setStringFCValue("Increased");
                    } else {
                        quantDatasetPeptideBean.setStringFCValue("Decreased");
                    }
                } catch (NumberFormatException exp) {
                    quantDatasetPeptideBean.setLogFC(-1000000000.0);
                    if (!stringDoubleLogFCRatioPG1PG2.trim().isEmpty()) {
                        quantDatasetPeptideBean.setStringFCValue(stringDoubleLogFCRatioPG1PG2.trim());

                    } else {
                        if (quantDatasetPeptideBean.getStringFCValue() == null) {      // TODO: TO CHECK IF THESE VARIABLES ARE NECESSARY
                            quantDatasetPeptideBean.setStringFCValue("Not Provided");  // TODO: TO CHECK IF THESE VARIABLES ARE NECESSARY
                        }
                    }
                }
            } else {
                quantDatasetPeptideBean.setLogFC(-1000000000.0);
                if (quantDatasetPeptideBean.getStringFCValue() == null) {
                    quantDatasetPeptideBean.setStringFCValue("Not Provided");
                }
            }
                        
            
            
            // TODO: CALCULUS EXTRACTED FROM YEHIA'S CODE. TO CHECK IF THERE ARE STILL NECESSARY OR NOT.
            // Functionality duplicated from loadDatasetProteinBean
            String stringTempPvalue = getStringValue.apply(Column.PVALUE);
            if (stringTempPvalue != null && !stringTempPvalue.trim().equalsIgnoreCase("")) {          
                quantDatasetPeptideBean = definePValueQuantDatasetPeptide(quantDatasetPeptideBean, stringTempPvalue, getStringValue.apply(Column.SIGNIFICANCE_TH), getStringValue.apply(Column.PVALUE_COMMENT));
            } else {
                quantDatasetPeptideBean.setpValue(-1000000000.0);
                quantDatasetPeptideBean.setStringPValue(" ");
                quantDatasetPeptideBean.setPvalueComment(" ");
                quantDatasetPeptideBean.setSignificanceThreshold(" ");
            }
            
            // TODO: CALCULUS EXTRACTED FROM YEHIA'S CODE. TO CHECK IF THERE ARE STILL NECESSARY OR NOT.
            // TODO: WHAT IS ROC AUC???
            // DUPLICATED FROM QUANTDATASETPROTEINBEAN
            String stringRocAuc = getStringValue.apply(Column.ROC_AUC);
            if (!stringRocAuc.trim().equalsIgnoreCase("")) {
                quantDatasetPeptideBean.setRocAuc(Double.valueOf(stringRocAuc.replace(",", ".").replace("?", "-")));
            } else {
                quantDatasetPeptideBean.setRocAuc(-1000000000.0);
            }
            
            
            
            quantDatasetPeptideBean.setQuantDatasetBean(parentDatasetBean);
            quantDatasetPeptideBean.setQuantDatasetProteinBean(parentDatasetProteinBean);
            parentDatasetProteinBean.addQuantDatasetPeptide(quantDatasetPeptideBean);
            parentDatasetBean.addQuantDatasetPeptide(quantDatasetPeptideBean);
            datasetPeptides.put(quantDatasetPeptideBean.getUniqLogicId(),quantDatasetPeptideBean);
        }else{
            LOGGER.log(Level.FINER, "loadDatasetPeptideBean: reusing quantDatasetPeptideBean {0}; parentDatasetBean: {1}; parentDatasetProteinBean:{2}", new Object[]{quantDatasetPeptideBean.getUniqLogicId(), parentDatasetBean.getUniqLogicId(), parentDatasetProteinBean.getUniqLogicId()});
        }
       
        return quantDatasetPeptideBean;            
    }
    
    
    // SOME OTHER FUNCTIONALITY METHODS
    
    /**
     * Imported functionality from old Yehia's code to define QuantDatasetProtein fields: 
     * pValue, significanceThreshold, StringPValue and PValueComment
     * @param prot QuantDatasetProteinBean object whose p-value related fields we are going to fill.
     * @param pValue raw string with the pValue just extracted from the excel file.
     * @param significanceThreshold raw string with the significanceThreshold just extracted from the excel file.
     * @param pvalueComment raw string with the pvalueComment just extracted from the excel file.
     * @return The QuantDatasetProteinBean object properly filled
     */
    private QuantDatasetProteinBean definePValueQuantDatasetProtein(QuantDatasetProteinBean prot, String pValue, String significanceThreshold, String pvalueComment) {       
        String operator = "op";
        try {
            pValue = pValue.replace(",", ".").replace("?", "-");

            double signThresholdValue = -1;
            if (significanceThreshold == null || significanceThreshold.trim().equalsIgnoreCase("")) {
                if (pValue.contains(">")) {
                    significanceThreshold = pValue.replace(">", "<");
                    operator = "<";
                    signThresholdValue = Double.valueOf(significanceThreshold.replace("<", "").replace("�", "").replace("�", "").replace("�", "").trim());
                } else if (pValue.contains("<")) {
                    significanceThreshold = pValue;
                    operator = "<";
                    signThresholdValue = Double.valueOf(significanceThreshold.replace("<", "").replace("�", "").replace("�", "").replace("�", "").trim());

                } else {
                    significanceThreshold = "defined by CSF-Pr at 0.05";
                    signThresholdValue = 0.05;
                    operator = "<";
                }

            } else if (significanceThreshold.contains("<=")) {
                significanceThreshold = significanceThreshold.replace(",", ".").replace("?", "-");
                signThresholdValue = Double.valueOf(significanceThreshold.replace("<=", ",").replace("�", "").replace("�", "").split(",")[1].replace("�", "").trim());
                operator = "<=";
            } else if (significanceThreshold.contains("<")) {
                significanceThreshold = significanceThreshold.replace(",", ".").replace("?", "-");
                signThresholdValue = Double.valueOf(significanceThreshold.split("<")[1].trim());
                operator = "<";
            } else {
                significanceThreshold = significanceThreshold.replace(",", ".").replace("?", "-");
                signThresholdValue = Double.valueOf(significanceThreshold.trim());
                operator = "<";

            }

            if (pValue.contains(">")) {
                prot.setStringPValue("Not Significant");
                prot.setSignificanceThreshold(significanceThreshold);
                prot.setpValue(-1000000000.0);
            } else if (pValue.contains("<")) {
                prot.setStringPValue("Significant");
                prot.setSignificanceThreshold(significanceThreshold);
                prot.setpValue(-1000000000.0);
            } else if (pValue.trim().equalsIgnoreCase("Significant")) {
                prot.setStringPValue("Significant");
                prot.setSignificanceThreshold(significanceThreshold);
                prot.setpValue(-1000000000.0);

            } else if (pValue.trim().equalsIgnoreCase("Not Significant")) {
                prot.setStringPValue("Not Significant");
                prot.setSignificanceThreshold(significanceThreshold);
                prot.setpValue(-1000000000.0);

            } else if (operator.equalsIgnoreCase("<=")) {
                if (Double.valueOf(pValue.trim()) <= signThresholdValue) {
                    prot.setStringPValue("Significant");
                    prot.setpValue(Double.valueOf(pValue.trim()));
                    prot.setSignificanceThreshold(significanceThreshold);
                } else if (Double.valueOf(pValue.trim()) >= signThresholdValue) {
                    prot.setStringPValue("Not Significant");
                    prot.setpValue(Double.valueOf(pValue.trim()));
                    prot.setSignificanceThreshold(significanceThreshold);
                }

            } else if (operator.equalsIgnoreCase("<")) {
                if (Double.valueOf(pValue.trim()) < signThresholdValue) {
                    prot.setStringPValue("Significant");
                    prot.setpValue(Double.valueOf(pValue.trim()));
                    prot.setSignificanceThreshold(significanceThreshold);
                } else if (Double.valueOf(pValue.trim()) >= signThresholdValue) {
                    prot.setStringPValue("Not Significant");
                    prot.setpValue(Double.valueOf(pValue.trim()));
                    prot.setSignificanceThreshold(significanceThreshold);
                }

            }

        } catch (NumberFormatException exp) {
            prot.setpValue(-1000000000.0);
            prot.setStringPValue(" ");
            LOGGER.log(Level.FINE, "definePValueQuantDatasetProtein error:{0}", new Object[]{exp});

        }
        prot.setPvalueComment(pvalueComment);

//        if(prot.getStringPValue()== null || prot.getStringPValue().trim().equalsIgnoreCase(""))
//        System.out.println("operator "+operator+"  "+significanceThreshold+"    "+pValue + "    String p Value "+prot.getUniprotAccession()+"  "+"  "+ prot.getStudyKey()+"   "+ prot.getPatientSubGroupI());
        return prot;
    }
    
    // Functionality duplicated from definePValueQuantDatasetProtein.
    private QuantDatasetPeptideBean definePValueQuantDatasetPeptide(QuantDatasetPeptideBean peptideStudyBean, String pValue, String significanceThreshold, String pvalueComment) {       
        String operator = "op";
        try {
            pValue = pValue.replace(",", ".").replace("?", "-");

            double signThresholdValue = -1;
            if (significanceThreshold == null || significanceThreshold.trim().equalsIgnoreCase("")) {
                if (pValue.contains(">")) {
                    significanceThreshold = pValue.replace(">", "<");
                    operator = "<";
                    signThresholdValue = Double.valueOf(significanceThreshold.replace("<", "").replace("�", "").replace("�", "").replace("�", "").trim());
                } else if (pValue.contains("<")) {
                    significanceThreshold = pValue;
                    operator = "<";
                    signThresholdValue = Double.valueOf(significanceThreshold.replace("<", "").replace("�", "").replace("�", "").replace("�", "").trim());

                } else {
                    significanceThreshold = "defined by CSF-Pr at 0.05";
                    signThresholdValue = 0.05;
                    operator = "<";
                }

            } else if (significanceThreshold.contains("<=")) {
                significanceThreshold = significanceThreshold.replace(",", ".").replace("?", "-");
                signThresholdValue = Double.valueOf(significanceThreshold.replace("<=", ",").replace("�", "").replace("�", "").split(",")[1].replace("�", "").trim());
                operator = "<=";
            } else if (significanceThreshold.contains("<")) {
                significanceThreshold = significanceThreshold.replace(",", ".").replace("?", "-");
                signThresholdValue = Double.valueOf(significanceThreshold.split("<")[1].trim());
                operator = "<";
            } else {
                significanceThreshold = significanceThreshold.replace(",", ".").replace("?", "-");
                signThresholdValue = Double.valueOf(significanceThreshold.trim());
                operator = "<";

            }

            if (pValue.contains(">")) {
                peptideStudyBean.setStringPValue("Not Significant");
                peptideStudyBean.setSignificanceThreshold(significanceThreshold);
                peptideStudyBean.setpValue(-1000000000.0);
            } else if (pValue.contains("<")) {
                peptideStudyBean.setStringPValue("Significant");
                peptideStudyBean.setSignificanceThreshold(significanceThreshold);
                peptideStudyBean.setpValue(-1000000000.0);
            } else if (pValue.trim().equalsIgnoreCase("Significant")) {
                peptideStudyBean.setStringPValue("Significant");
                peptideStudyBean.setSignificanceThreshold(significanceThreshold);
                peptideStudyBean.setpValue(-1000000000.0);

            } else if (pValue.trim().equalsIgnoreCase("Not Significant")) {
                peptideStudyBean.setStringPValue("Not Significant");
                peptideStudyBean.setSignificanceThreshold(significanceThreshold);
                peptideStudyBean.setpValue(-1000000000.0);

            } else if (operator.equalsIgnoreCase("<=")) {
                if (Double.valueOf(pValue.trim()) <= signThresholdValue) {
                    peptideStudyBean.setStringPValue("Significant");
                    peptideStudyBean.setpValue(Double.valueOf(pValue.trim()));
                    peptideStudyBean.setSignificanceThreshold(significanceThreshold);
                } else if (Double.valueOf(pValue.trim()) >= signThresholdValue) {
                    peptideStudyBean.setStringPValue("Not Significant");
                    peptideStudyBean.setpValue(Double.valueOf(pValue.trim()));
                    peptideStudyBean.setSignificanceThreshold(significanceThreshold);
                }

            } else if (operator.equalsIgnoreCase("<")) {
                if (Double.valueOf(pValue.trim()) < signThresholdValue) {
                    peptideStudyBean.setStringPValue("Significant");
                    peptideStudyBean.setpValue(Double.valueOf(pValue.trim()));
                    peptideStudyBean.setSignificanceThreshold(significanceThreshold);
                } else if (Double.valueOf(pValue.trim()) >= signThresholdValue) {
                    peptideStudyBean.setStringPValue("Not Significant");
                    peptideStudyBean.setpValue(Double.valueOf(pValue.trim()));
                    peptideStudyBean.setSignificanceThreshold(significanceThreshold);
                }

            }

        } catch (NumberFormatException exp) {
            peptideStudyBean.setpValue(-1000000000.0);
            peptideStudyBean.setStringPValue(" ");
            LOGGER.log(Level.FINE, "definePValueQuantDatasetPeptide error:{0}", new Object[]{exp});
        }
        peptideStudyBean.setPvalueComment(pvalueComment);

//        if(prot.getStringPValue()== null || prot.getStringPValue().trim().equalsIgnoreCase(""))
//        System.out.println("operator "+operator+"  "+significanceThreshold+"    "+pValue + "    String p Value "+prot.getUniprotAccession()+"  "+"  "+ prot.getStudyKey()+"   "+ prot.getPatientSubGroupI());
        return peptideStudyBean;
    }
    
        

        
    // UTILITY METHODS FOR MANAGING EXCEL STUFF
    
    
    /**
     * Private method that returns a XSSFWorkbook or HSSFWorkbook object depending on 
     * the excel file type (xlsx or xls).
     * @param inputStream
     * @param excelFileName
     * @return XSSFWorkbook or HSSFWorkbook, both implementing Workbook interface
     * @throws IOException 
     */
    private Workbook getWorkbookByExcelType(InputStream inputStream, String excelFileName)
        throws IOException, IllegalArgumentException {
        Workbook workbook = null;

        if (excelFileName.endsWith("xlsx")) {
            workbook = new XSSFWorkbook(inputStream);
            // workbook = new SXSSFWorkbook(inputStream);  // Designed for very long tables, but seems to have problems when the window loaded is emptied
        } else if (excelFileName.endsWith("xls")) {
            workbook = new HSSFWorkbook(inputStream);
        } else {
            throw new IllegalArgumentException("The specified file is not Excel file");
        }

        return workbook;
    }
    
    // TODO: CHECK ALL POSSIBLE STATUSES: _NONE, NUMERIC, STRING, FORMULA, BLANK, BOOLEAN, ERROR;
    /**
     * Returns the value of the cell according to its type
     * @param cell
     * @return 
     */
    private Object getCellValue(Cell cell) {
        if (cell != null){
            switch (cell.getCellType()) {
                case STRING:
                    String stringValue = cell.getStringCellValue();
                    if (stringValue != null)
                        stringValue = stringValue.trim();
                    LOGGER.log(Level.FINEST, "getCellValue:{0}; cellColumnIndex:{1}; rowIndex:{2}; CellType(): +{3}", new Object[]{stringValue, cell.getColumnIndex(), cell.getRowIndex(), cell.getCellType()});
                    return stringValue;
                case BOOLEAN:
                    LOGGER.log(Level.FINEST, "getCellValue:{0}; cellColumnIndex:{1}; rowIndex:{2}; CellType(): +{3}", new Object[]{cell.getBooleanCellValue(), cell.getColumnIndex(), cell.getRowIndex(), cell.getCellType()});                    
                    return cell.getBooleanCellValue();
                case NUMERIC:
                    LOGGER.log(Level.FINEST, "getCellValue:{0}; cellColumnIndex:{1}; rowIndex:{2}; CellType(): +{3}", new Object[]{cell.getNumericCellValue(), cell.getColumnIndex(), cell.getRowIndex(), cell.getCellType()});                    
                    return cell.getNumericCellValue();

                /*case FORMULA:
                    System.out.println("Cell Formula="+cell.getCellFormula());
                    System.out.println("Cell Formula Result Type="+cell.getCachedFormulaResultType());
                    if(cell.getCachedFormulaResultType() == Cell.NUMERIC){
                            System.out.println("Formula Value="+cell.getNumericCellValue());
                    }
                */ 

            }
        }else{
            LOGGER.log(Level.FINEST, "getCellStringValue: cell is null");
        }

        return null;
    }
    
    /**
     * Returns the String value of the cell 
     * @param cell
     * @return 
     */
    private String getCellStringValue(Cell cell) {
        String string = "";
        if (cell != null){
            switch (cell.getCellType()) {
                case STRING:
                    string = cell.getStringCellValue();
                    break;
                case BOOLEAN:
                    string = Boolean.toString(cell.getBooleanCellValue());
                    break;
                case NUMERIC:
                    string =  Double.toString(cell.getNumericCellValue());
                    break;
                case BLANK:
                    string = "";
                    break;
                default: 
                    string =  "";
            }
        }else{
            LOGGER.log(Level.FINEST, "getCellStringValue: cell is null");
        }
        if (cell != null){
            string = string.trim();
            LOGGER.log(Level.FINEST, "getCellStringValue:{0}; cellColumnIndex:{1}; rowIndex:{2}; CellType(): +{3}", new Object[]{string, cell.getColumnIndex(), cell.getRowIndex(), cell.getCellType()});
        }
        return string; 
    }
    
    /**
     * Returns the Double value of the cell
     * @param cell
     * @return 
     */
    private Double getCellDoubleValue(Cell cell) {
        Double doubleReturn = null;
        if (cell != null) {        
            switch (cell.getCellType()) {
                case STRING:
                    doubleReturn = null;
                    break;
                case BOOLEAN:
                    doubleReturn = null;
                    break;
                case NUMERIC:
                    doubleReturn = cell.getNumericCellValue();
                    break;
                case BLANK:
                    doubleReturn = null;
                    break;
                default: 
                    doubleReturn = null;
            }
        }else{
            LOGGER.log(Level.FINEST, "getCellDoubleValue: cell is null");
        }
        if (cell != null)
            LOGGER.log(Level.FINEST, "getCellDoubleValue:{0}; cellColumnIndex:{1}; rowIndex:{2}; CellType(): +{3}", new Object[]{doubleReturn, cell.getColumnIndex(), cell.getRowIndex(), cell.getCellType()});
        
        return doubleReturn;
    }
    
    /**
     * Returns the Integer value of the cell
     * @param cell
     * @return 
     */
    private Integer getCellIntegerValue(Cell cell) {
        Integer integerReturn = null;
        if (cell != null) {
            switch (cell.getCellType()) {
                case STRING:
                    integerReturn = null;
                    break;
                case BOOLEAN:
                    integerReturn = null;
                    break;
                case NUMERIC:
                    integerReturn = (int)cell.getNumericCellValue();
                    break;
                case BLANK:
                    integerReturn = null;
                    break;
                default: 
                    integerReturn = null;
            }
        }else{
            LOGGER.log(Level.FINEST, "getCellIntegerValue: cell is null");
        }
        if (cell != null)
            LOGGER.log(Level.FINEST, "getCellIntegerValue:{0}; cellColumnIndex:{1}; rowIndex:{2}; CellType(): +{3}", new Object[]{integerReturn, cell.getColumnIndex(), cell.getRowIndex(), cell.getCellType()});
        return integerReturn;    
    }
    
    /**
     * Returns the boolean value of the cell 
     * @param cell
     * @return 
     */
    private Boolean getCellBooleanValue(Cell cell) {
        Boolean booleanReturn = null;
        if (cell != null){ 
            switch (cell.getCellType()) {
                case STRING:
                    booleanReturn = null;
                    break;
                case BOOLEAN:
                    booleanReturn = cell.getBooleanCellValue();
                    break;
                case NUMERIC:
                    booleanReturn = null;
                    break;
                case BLANK:
                    booleanReturn = null;
                    break;
                default: 
                    booleanReturn = null;
            }
        }else{
            LOGGER.log(Level.FINEST, "getCellBooleanValue: cell is null");
        }
        if (cell != null)
            LOGGER.log(Level.FINEST, "getCellBooleanValue:{0}; cellColumnIndex:{1}; rowIndex:{2}; CellType(): +{3}", new Object[]{booleanReturn, cell.getColumnIndex(), cell.getRowIndex(), cell.getCellType()});
        return booleanReturn;
    }
    
    
    /**
     * Returns an EnumMap with the columns available and their position into the 
     * first row of the excel file.
     * @param firstRow
     * @return EnumMap<Column, Integer> enum map with Column objects as keys and 
     * their numeric column position in the first row
     */
    private EnumMap getColumnsPositions(Row firstRow){
        EnumMap<Column, Integer> enumMap = new EnumMap<Column, Integer>(Column.class);
        
        int columnPosition = -1;
        for(Column column : Column.values()){
            columnPosition = getColumnPosition(firstRow, column.getColumnName());
            LOGGER.log(Level.FINE, "Column {0} : \t\t {1}", new Object[]{column, columnPosition});
            enumMap.put(column,columnPosition);
        }
        
        // use example
        //int levelPosition = enumMap.get(Level.HIGH);
        return enumMap;
    } 
    
    /**
     * Method that receives a row and a column name and returns the column number
     * into the row where the column name is found.
     * @param firstRow
     * @param columnName
     * @return -1 if the column name is not found
     */
    private int getColumnPosition(Row firstRow, String columnName){
        int columnPosition = -1;
        int i = 0;
        Cell tempCell = null;
        while (columnPosition == -1 && i < firstRow.getLastCellNum()){
            tempCell = firstRow.getCell(i);
            if (tempCell.getStringCellValue().indexOf(columnName) != -1){
                columnPosition = i;
            }
            i++;
        }
        return columnPosition;
    } 
    
    /**
     * Utility functions created for making simpler to retrieve columns'value avoiding repetitive
     * callings to row or columnsPositions
     * into the row where the column name is found.
     * @param row any row from the excel file
     * @param columnsPositions calculated positions of all columns in current excel file 
     * @return Function which receives a column and retrieves its value as an Object
     */
    private Function<Column, String> getFunctionColumnStringValue(Row row, EnumMap<Column, Integer> columnsPositions){      
        Function<Column, String> myFunction = (Column column) -> {
            String returnString = getCellStringValue(row.getCell(columnsPositions.get(column)));
            LOGGER.log(Level.FINER, "getFunctionColumnStringValue: column:{0}; rowIndex:{1}; return:{2}", new Object[]{column, row.getRowNum(), returnString});
            return returnString;
        };   
        return myFunction;
    }
    
    private Function<Column, Double> getFunctionColumnDoubleValue(Row row, EnumMap<Column, Integer> columnsPositions){
        Function<Column, Double> myFunction = (Column column) -> {
            Double returnDouble = getCellDoubleValue(row.getCell(columnsPositions.get(column)));
            LOGGER.log(Level.FINER, "getFunctionColumnDoubleValue: column:{0}; rowIndex:{1}; return:{2}", new Object[]{column, row.getRowNum(), returnDouble});
            return returnDouble;
        }; 
        return myFunction;
    }
    
    private Function<Column, Integer> getFunctionColumnIntegerValue(Row row, EnumMap<Column, Integer> columnsPositions){
        Function<Column, Integer> myFunction = (Column column) -> {
            Integer returnInteger = getCellIntegerValue(row.getCell(columnsPositions.get(column)));
            LOGGER.log(Level.FINER, "getCellIntegerValue: column:{0}; rowIndex:{1}; return:{2}", new Object[]{column, row.getRowNum(), returnInteger});
            return returnInteger;
        }; 
        return myFunction;
    }

    /**
     * Method that returns the columns able to uniquely identify one specific bean type
     * It has to be in correspondence with the way ids are being generated by every bean by itself based on its own properties.
     * @param myBeanClass
     * @return List of Columns able to uniquely identify the bean type
     */
    public List<Column> getBeanClassUniqIdColumns(Class beanClass){
        switch (beanClass.getSimpleName()){
            case "QuantProteinBean":
                // Derived unique id, from its uniprot accession column
                return new ArrayList<>(Arrays.asList(Column.ACCESSION_PROT));
            
            case "QuantStudyBean":
                // Derived unique id, from its studykey column
                return new ArrayList<>(Arrays.asList(Column.STUDY_KEY));
            
            case "QuantDatasetBean":
                //Derived unique id, with columns from its study and study dataset related objects
                return new ArrayList<>(Arrays.asList(Column.STUDY_KEY,
                        Column.SAMPLE_TYPE,Column.SAMPLE_MATCHING,
                        Column.NORM_STRATEGY,Column.PG1,Column.PG2,
                        Column.PSG1, Column.PSG2));
                
            case "QuantDatasetProteinBean":
                //Derived unique id, with columns from its dataset and protein-related objects
                return new ArrayList<>(Arrays.asList(Column.STUDY_KEY,
                        Column.SAMPLE_TYPE,Column.SAMPLE_MATCHING,
                        Column.NORM_STRATEGY,Column.PG1,Column.PG2,
                        Column.PSG1, Column.PSG2,                   // until here we have dataset id
                        Column.ACCESSION_PROT));
                
            case "QuantDatasetPeptideBean":
                // Derived unique id, from its dataset and peptide-related columns               
                return new ArrayList<>(Arrays.asList(Column.STUDY_KEY,
                        Column.SAMPLE_TYPE,Column.SAMPLE_MATCHING,
                        Column.NORM_STRATEGY,Column.PG1,Column.PG2,
                        Column.PSG1, Column.PSG2,                   // until here we have dataset id
                        Column.ACCESSION_PROT,Column.PEPTIDE_MODS,
                        Column.PEPTIDE_SEQUENCE)); // TODO: TO INCLUDE COLUMN.PEPTIDE_SEQUENCE??    
                
            default:
                LOGGER.log(Level.WARNING, "getBeanClassUniqIdColumns found an unknown class: {0}",beanClass.getSimpleName() );
                return null;                
        }        
    }
    
    /**
     * Method that, given a type of bean and a row reader, returns the String identifier adapted to the bean class.
     * @param myBeanClass
     * @param getStringValue
     * @return 
     */
    public String getUniqueIdStringByBeanClassAndRow(Class myBeanClass, Function<Column, String>  getStringValue){
        List<Column> idColumns = getBeanClassUniqIdColumns(myBeanClass);
        Iterator<Column> iterator = idColumns.listIterator();
        String id = "";
        while(iterator.hasNext()){
            id = id + getStringValue.apply(iterator.next());
            if (iterator.hasNext())
                id = id + "_";
        }
        LOGGER.log(Level.FINEST, "myBeanClass: "+myBeanClass.getSimpleName()+"; ID: "+id);

        return id;
    }
    
    private void showFinalReport(){
        showReportEntity("studies",studies);
        showReportEntity("datasets",datasets);
        showReportEntity("proteins",proteins);
        showReportEntity("datasetProteins",datasetProteins);
        showReportEntity("datasetPeptides",datasetPeptides);
    }
    
    private void showReportEntity(String entitiesName, HashMap entityHashMap){
        LOGGER.log(Level.INFO, "Entities : {0}: {1}", new Object[]{entitiesName, entityHashMap.keySet().size()});
    }
     
    
    // THREAD RELATED METHODS
    
    private void sleep() {
        try {
            int lower = 500;
            int upper = 3000;
            //Thread.sleep((long) ((Math.random() * (upper - lower)) + lower));
            Thread.sleep(4);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    public String getHour() {
        return DateTimeFormatter
                .ofPattern("hh:mm:ss a")
                .withLocale(Locale.ENGLISH)
                .withZone(ZoneId.systemDefault())
                .format(Instant.now());
    }

}
