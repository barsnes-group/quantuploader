
package no.uib.probe.quantuploader.examples;



import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.function.Function;
import no.uib.probe.quantuploader.beans.QuantDatasetBean;
import no.uib.probe.quantuploader.beans.QuantStudyBean;
import no.uib.probe.quantuploader.beans.QuantProteinBean;
import no.uib.probe.quantuploader.beans.QuantDatasetPeptideBean;
import no.uib.probe.quantuploader.beans.QuantDatasetProteinBean;
import no.uib.probe.quantuploader.enums.Column;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


/**
 *
 * @author carlos.horro
 */
public class ExcelReaderExample {
    
    public static void main(String[] args) {

        String excelFilePath = "/Users/carlos.horro/GitHub/uploader/20191126_Final_Table_excel.xlsx"; // can be .xls or .xlsx

        HashMap<String,QuantStudyBean> studies = new HashMap<String,QuantStudyBean>();
         
                
        try (FileInputStream inputStream = new FileInputStream(new File(excelFilePath));){
            
            Workbook workbook = ExcelReaderExample.getWorkbook(inputStream, excelFilePath);
            Iterator<Row> rowIterator = workbook.getSheetAt(0).iterator();  // iterator over the first sheet found in the workbook
            
            QuantStudyBean studyBean = null;
            QuantDatasetBean datasetBean = null;
            QuantProteinBean proteinBean = null;
            QuantDatasetProteinBean datasetProteinBean = null;
            QuantDatasetPeptideBean datasetPeptideBean = null;
            EnumMap<Column, Integer> columnsPositions = null;
            
            if (rowIterator.hasNext())
                columnsPositions = getColumnsPositions(rowIterator.next());
            
            while (rowIterator.hasNext()) {    
                Row currentRow = rowIterator.next();
                
                Function<Column, String>  getStringValue = getFunctionColumnStringValue(currentRow, columnsPositions);
                Function<Column, Integer>  getIntegerValue = getFunctionColumnIntegerValue(currentRow, columnsPositions);

                
                // Using a cellIterator is one way of using this library; but it does not seem the most optimal one
                //Iterator<Cell> cellIterator = currentRow.cellIterator();
                //while (cellIterator.hasNext()){
                    //Get the Cell object
                //    Cell currentCell = cellIterator.next();
                //    int currentColumnIndex = currentCell.getColumnIndex();
                 
                
                /***** STUDY STUFF    *****/
                    
                // TODO: Currently using study key as the only identifier for studies. BUT IT SEEMS IT WILL NOT BE ENOUGH (TYPE OF ANALYSIS?)
                // long way to get the value of a cell
                // String currentStudyKey = (String)getCellValue(currentRow.getCell(columnsPositions.get(Column.STUDY_KEY)));
                // show way to get the value of a cell
                String currentStudyKey = getStringValue.apply(Column.STUDY_KEY);
                System.out.println(">RowNumber: "+currentRow.getRowNum()+" ; STUDY KEY: "+currentStudyKey);    
                
                // just initial condition
                if (studyBean == null ){
                    studyBean = new QuantStudyBean();
                }else{
                    // if we have a new study, we have to load if (if we already managed it in the past) or to initiate it (it we didn't)
                    if (studyBean.getStudyKey() != null && !studyBean.getStudyKey().equals(currentStudyKey)){

                        if (studies.get(currentStudyKey) != null){
                            studyBean = studies.get(currentStudyKey);
                        }else{
                            studyBean = new QuantStudyBean();
                        }
                    }
                }

                studyBean.setPumedID((getIntegerValue.apply(Column.PUBMED_ID)));
                
                studyBean.setStudyKey(currentStudyKey);                                      
                
                studyBean.setAuthor((String)getStringValue.apply(Column.AUTHOR));
                
                studyBean.setYear(getIntegerValue.apply(Column.YEAR));              

                studyBean.setQuantifiedProteinsNumber(getIntegerValue.apply(Column.QUANT_PROTEINS_NUM));              
                
                /***** PROTEIN STUFF    *****/
                
                proteinBean = new QuantProteinBean(); // TODO: REUSE!
                datasetProteinBean = new QuantDatasetProteinBean();
                
                proteinBean.setUniprotAccession(getStringValue.apply(Column.ACCESSION_PROT));
                
                System.out.println("getValue.apply(Column.PROTEIN_NAME_UNIPROT): "+getStringValue.apply(Column.PROTEIN_NAME_UNIPROT));
                proteinBean.setUniprotProteinName(getStringValue.apply(Column.PROTEIN_NAME_UNIPROT));

                /***** DATASET STUFF    *****/
                
                
                /***** DATASET-PROTEIN STUFF    *****/
                
                datasetProteinBean.setPublicationAccNumber(getStringValue.apply(Column.ACCESSION_PUB));

                datasetProteinBean.setPublicationProteinName(getStringValue.apply(Column.PROTEIN_NAME_PUB));
                
                
                /***** STABLISHING OBJECT RELATIONS    *****/

                proteinBean.addQuantDatasetProt(datasetProteinBean);
                datasetBean.addQuantDatasetProt(datasetProteinBean);
                
                System.out.println("QuantData [PubmedId= " + studyBean.getPumedID() + ", StudyKey= " + studyBean.getStudyKey() +"]" );

                    
                
            } // end of row iterator
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }


    }
    
    
    // UTILITY METHODS FOR MANAGING EXCEL STUFF
    
    
    /**
     * Private method that returns a XSSFWorkbook or HSSFWorkbook object depending on 
     * the excel file type (xlsx or xls).
     * @param inputStream
     * @param excelFilePath
     * @return XSSFWorkbook or HSSFWorkbook, both implementing Workbook interface
     * @throws IOException 
     */
    private static Workbook getWorkbook(FileInputStream inputStream, String excelFilePath)
        throws IOException {
        Workbook workbook = null;

        if (excelFilePath.endsWith("xlsx")) {
            workbook = new XSSFWorkbook(inputStream);
            // workbook = new SXSSFWorkbook(inputStream);  // Designed for very long tables, but seems to have problems when the window loaded is emptied
        } else if (excelFilePath.endsWith("xls")) {
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
    private static Object getCellValue(Cell cell) {
        System.out.println("Cell type: "+cell.getCellType());
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();

            case BOOLEAN:
                return cell.getBooleanCellValue();

            case NUMERIC:
                return cell.getNumericCellValue();
            
            /*case FORMULA:
                System.out.println("Cell Formula="+cell.getCellFormula());
                System.out.println("Cell Formula Result Type="+cell.getCachedFormulaResultType());
                if(cell.getCachedFormulaResultType() == Cell.NUMERIC){
                        System.out.println("Formula Value="+cell.getNumericCellValue());
                }
            */ 
                
        }

        return null;
    }
    
    /**
     * Returns the String value of the cell 
     * @param cell
     * @return 
     */
    private static String getCellStringValue(Cell cell) {
        System.out.println("getCellStringValue, CellType(): "+cell.getCellType());
        if (cell == null) 
            return null;
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();

            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());

            case NUMERIC:
                return Double.toString(cell.getNumericCellValue());
                
            case BLANK:
                return "";
            
            default: 
                return "";
        }
        //return cell.getStringCellValue();
    }
    
    /**
     * Returns the Double value of the cell
     * @param cell
     * @return 
     */
    private static Double getCellDoubleValue(Cell cell) {
        System.out.println("getCellDoubleValue, CellType(): "+cell.getCellType());

        if (cell == null) 
            return null;
        switch (cell.getCellType()) {
            case STRING:
                return null;

            case BOOLEAN:
                return null;

            case NUMERIC:
                return cell.getNumericCellValue();
            
            case BLANK:
                return null;
            
            default: 
                return null;
            
        }
    }
    
    /**
     * Returns the Integer value of the cell
     * @param cell
     * @return 
     */
    private static Integer getCellIntegerValue(Cell cell) {
        System.out.println("getCellIntegerValue, CellType(): "+cell.getCellType());

        if (cell == null) 
            return null;
        switch (cell.getCellType()) {
            case STRING:
                return null;

            case BOOLEAN:
                return null;

            case NUMERIC:
                return (int)cell.getNumericCellValue();
            
            case BLANK:
                return null;
            
            default: 
                return null;
                            
        }
    }
    
    /**
     * Returns the boolean value of the cell 
     * @param cell
     * @return 
     */
    private static Boolean getCellBooleanValue(Cell cell) {
        System.out.println("getCellBooleanValue, CellType(): "+cell.getCellType());

        if (cell == null) 
            return null;
        switch (cell.getCellType()) {
            case STRING:
                return null;

            case BOOLEAN:
                return cell.getBooleanCellValue();

            case NUMERIC:
                return null;
            
            case BLANK:
                return null;
            
            default: 
                return null;
                            
        }
    }
    
    
    /**
     * Returns an EnumMap with the columns available and their position into the first row of the excel file.
     * @param firstRow
     * @return 
     */
    private static EnumMap getColumnsPositions(Row firstRow){
        EnumMap<Column, Integer> enumMap = new EnumMap<Column, Integer>(Column.class);
        
        for(Column column : Column.values()){
            //System.out.println("ColumnPosition: "+column.toString()+"\t"+getColumnPosition(firstRow, column.getColumnName()));
            enumMap.put(column, getColumnPosition(firstRow, column.getColumnName()));
        }
        
        // example
        //String levelValue = enumMap.get(Level.HIGH);
        return enumMap;
    } 
    
    /**
     * Method that receives a row and a column name and returns the column number
     * into the row where the column name is found.
     * @param firstRow
     * @param columnName
     * @return -1 if the column name is not found
     */
    private static int getColumnPosition(Row firstRow, String columnName){
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
    private static Function<Column, String> getFunctionColumnStringValue(Row row, EnumMap<Column, Integer> columnsPositions){
        return (column) -> getCellStringValue(row.getCell(columnsPositions.get(column)));   
    }
    
    private static Function<Column, Double> getFunctionColumnDoubleValue(Row row, EnumMap<Column, Integer> columnsPositions){
        return (column) -> getCellDoubleValue(row.getCell(columnsPositions.get(column)));   
    }
    
    private static Function<Column, Integer> getFunctionColumnIntegerValue(Row row, EnumMap<Column, Integer> columnsPositions){
        return (column) -> getCellIntegerValue(row.getCell(columnsPositions.get(column)));   
    }
}

