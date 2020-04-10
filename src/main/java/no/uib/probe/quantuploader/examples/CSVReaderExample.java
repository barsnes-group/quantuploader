
package no.uib.probe.quantuploader.examples;


import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import no.uib.probe.quantuploader.beans.QuantStudyBean;
import no.uib.probe.quantuploader.beans.QuantProteinBean;
import no.uib.probe.quantuploader.beans.QuantDatasetPeptideBean;


/**
 *
 * @author carlos.horro
 */
public class CSVReaderExample {
    
    public static void main(String[] args) {

        String csvFile = "/Users/carlos.horro/GitHub/uploader/20191126_Final_Table_excel.csv";

        HashMap<String,QuantStudyBean> studies = new HashMap<String,QuantStudyBean>();
        
        CSVParser parser = new CSVParserBuilder().withSeparator(',').build();
        try (CSVReader reader = new CSVReaderBuilder(new FileReader(csvFile)).withCSVParser(parser).build();){
            
            // easy and direcy way to do it
            // reader = new CSVReader(new FileReader(csvFile));
            
            reader.skip(1); // skip titles
            String[] line;
            QuantStudyBean quantStudyBean = null;
            while ((line = reader.readNext()) != null) {                
                
                // just initial condition
                if (quantStudyBean == null ){
                    quantStudyBean = new QuantStudyBean();
                }else{
                    // if we have a new study, we have to load if (if we already managed it in the past) or to initiate it (it we didn't)
                    if (quantStudyBean.getStudyKey() != null && !quantStudyBean.getStudyKey().equals(line[1])){
                        
                        if (studies.get(line[1]) != null){
                            quantStudyBean = studies.get(line[1]);
                        }else{
                            quantStudyBean = new QuantStudyBean();
                        }
                    }
                }
                
                
                quantStudyBean.setPumedID(new Integer(line[0]));// pubmed Id                
                
                quantStudyBean.setStudyKey(line[1]);            // study key
                
                quantStudyBean.setAuthor(line[2]);              // pubmed Id                
                
                quantStudyBean.setYear(new Integer(line[3]));   // study key
                
                
                System.out.println("QuantData [PubmedId= " + quantStudyBean.getPumedID() + ", StudyKey= " + quantStudyBean.getStudyKey() +"]" );
            }
        } catch (CsvValidationException csve) {
            csve.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }


    }
}
