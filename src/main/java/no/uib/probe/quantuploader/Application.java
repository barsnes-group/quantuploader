package no.uib.probe.quantuploader;

import java.util.logging.Level;
import java.util.logging.Logger;
import no.uib.probe.quantuploader.services.QuantDataExcelLoaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * The entry point of the Spring Boot application.
 */
@SpringBootApplication
public class Application extends SpringBootServletInitializer {

    private static final Logger LOGGER = Logger.getLogger(Application.class.getName());

    
    private static final String excelFilePath = "/Users/carlos.horro/GitHub/uploader/20191126_Final_Table_excel.xlsx"; // can be .xls or .xlsx
    
    @Autowired
    private QuantDataExcelLoaderService quantDataExcelLoaderService;
    
    // Injections just for testing purposes! they don't seem to work!
    //@Autowired
    //AnnotationConfigApplicationContext context;
    /*@Autowired
    private DataSource dataSource;
    @Autowired
    private StudyRepository studyRepository;
    */
    public static void main(String[] args) {
        //SpringApplication.run(Application.class, args);
        LOGGER.log(Level.FINE, ">> APPLICATION MAIN CALL");
        //System.out.println("SpringApplication.run");
        //Application app = new Application();
        //app.start();
        //QuantDataExcelLoaderService.getInstance().importModelFromExcelFile(excelFilePath);
        //quantDataExcelLoaderService.importModelFromExcelFile(excelFilePath);
    }
    
    /* TO TRY:
    public static void main(String[] args) throws Exception {
		SpringApplication.run(Application.class, args);
	}
    */
    
    
    /*
    @PostConstruct
    public void init(){
        LOGGER.log(Level.SEVERE, ">> APPLICATION RUNNING");

        try{
            testingDatasource();
            
        }catch(Exception e){
            LOGGER.log(Level.SEVERE, "Error retrieving testing data: "+e);
            return;
        }
        quantDataExcelLoaderService.importModelFromExcelFile(excelFilePath);

    }*/
    /*
    public void testingDatasource() throws Exception {
        //LOGGER.log(Level.SEVERE, "testingDatasource");

        //DataSource dataSource = (DataSource)context.getBean("dataSource");
        LOGGER.log(Level.SEVERE, "testingDatasource: {0}", dataSource);

        Iterable<QuantStudyBean> studyList =  studyRepository.findAll();
        for(QuantStudyBean study : studyList){
            LOGGER.log(Level.SEVERE, "Here is a study: {0}", study.toString());

            //System.out.println("Here is a study: " + study.toString());
        }
    }*/
    
    /*
    sometimes this works?
    @Autowired
private ApplicationContext context;

SomeClass sc = (SomeClass)context.getBean(SomeClass.class);
    */

}
