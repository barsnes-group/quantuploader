package no.uib.probe.quantuploader.components;

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.progressbar.ProgressBarVariant;
import java.io.InputStream;

import java.util.logging.Logger;
import no.uib.probe.quantuploader.enums.ComponentStatus;

/**
 *
 * @author carlos.horro
 */
public class ImportComponent /* HorizontalLayout */ extends VerticalLayout  {
        
    private static final Logger LOGGER = Logger.getLogger(ImportComponent.class.getName());
    
    private UploadView parentView;
    
    // Contains everything but the title
    private VerticalLayout mainBodyContainer;

    Paragraph title;
    private Div outputInfo;        
    private ProgressBar processProgressBar;
    
    private ComponentStatus componentStatus;
    
    public ImportComponent(UploadView parentView) {    
        this.parentView = parentView;
        
        // The mainBodyContainer will contain everything but the title
        mainBodyContainer = new VerticalLayout();
        
        // to improve styling getting styleclassnames from uploader
        // don't work: Template.get, Component.get, getElementById("fuel-name-1");, test-upload.
        
        //mainBodyContainer.getStyle().set( "border-style" , "solid" ) ; 
        mainBodyContainer.getStyle().set( "border" , "1px dashed grey" ) ; 

        setMargin(true);
        
        outputInfo = new Div();        
        outputInfo.setId("test-output");

        title = new Paragraph(new HtmlComponent(Tag.P));
        title.getElement().setText("2. Import");
        
        processProgressBar = new ProgressBar();
        processProgressBar.setIndeterminate(false);
        processProgressBar.setMin(0);
        //processProgressBar.setWidth(getWidth());
       
        // adding subcomponents to the layout
        add(title);
        add(mainBodyContainer);
        mainBodyContainer.add(outputInfo);
        mainBodyContainer.add(processProgressBar);
        
        System.out.println("end ImportComponent");
        notifyInitialiseImportComponent();
    }
    
    
    /**
     * Method that adapts component visual status to the working status of its
     * procedures.
     * @param componentStatus
     */
    private void setVisualStatus(ComponentStatus componentStatus){
        setVisualStatus(componentStatus,null);
    }
    
    /**
     * Method that adapts component visual status to the working status of its
     * procedures.
     * @param componentStatus
     * @param extraMessage 
     */
    private void setVisualStatus(ComponentStatus componentStatus, String extraMessage){
        String fullMessage = "";
        switch(componentStatus){
            case NOT_STARTED:
                fullMessage = "Waiting for uploading..."+((extraMessage != null)?" "+extraMessage:"");
                parentView.getMainUI().access(() ->  {
                    processProgressBar.setMax(1);
                    processProgressBar.setValue(0);
                    processProgressBar.removeThemeVariants(ProgressBarVariant.LUMO_SUCCESS,ProgressBarVariant.LUMO_ERROR);
                });
                refreshOutputInfo(fullMessage);
                break;
            case RUNNING:
                fullMessage = "Uploading of excel file successful, importing it..."+((extraMessage != null)?" "+extraMessage:"");
                refreshOutputInfo(fullMessage);
                parentView.getMainUI().access(() ->  {
                    getUI().get().push();
                });
                break;
            case FINISHED:
                fullMessage = "Importing finished"+((extraMessage != null)?": "+extraMessage:"");
                refreshOutputInfo(fullMessage);
                parentView.getMainUI().access(() ->  {
                    processProgressBar.addThemeVariants(ProgressBarVariant.LUMO_SUCCESS);
                });
                break;
            case PREVIOUS_ERROR:
                fullMessage = "There was an error uploading the file into the server"+((extraMessage != null)?": "+extraMessage:"");
                parentView.getMainUI().access(() ->  {
                    processProgressBar.addThemeVariants(ProgressBarVariant.LUMO_ERROR);
                });
                refreshOutputInfo(fullMessage);
            break;          
            case RUNNING_ERROR:
                fullMessage = "There was an error managing the data"+((extraMessage != null)?": "+extraMessage:"");
                refreshOutputInfo(fullMessage);
                parentView.getMainUI().access(() ->  {
                    processProgressBar.addThemeVariants(ProgressBarVariant.LUMO_ERROR);
                });
            break;
            case CANCELLED:
                fullMessage = "Importing process was cancelled"+((extraMessage != null)?": "+extraMessage:"");
                refreshOutputInfo(fullMessage);
            break;
                
        }
        this.componentStatus = componentStatus;
    }
    
    
    /**
     * Initializes all component structures.
     */
    public void notifyInitialiseImportComponent(){
        setVisualStatus(ComponentStatus.NOT_STARTED);
        // presenter may still be null if we are just starting the app.
        if (this.parentView.getPresenter() != null){
            this.parentView.getPresenter().finishImportingPendingTasks();
            //this.parentView.getPresenter().initialiseDBLoadingData();
            this.parentView.initialiseDBLoadingComponent();
        }
    }
    
    
    /**
     * Receives a notification about the import of an excel file must start.
     * @param inputStream reference to the input stream to read the data from
     * @param fileName name of the source file
     */
    public void notifyStartImport(InputStream inputStream, String fileName){
        setVisualStatus(ComponentStatus.RUNNING);
        this.parentView.getPresenter().startImport(inputStream, fileName, this);
    }
    
    /**
     * Receives a notification about an internal error managing the imported data
     * @param errorMessage 
     */
    public void notifyRunningError(String errorMessage){
        setVisualStatus(ComponentStatus.RUNNING_ERROR, errorMessage);
        this.parentView.getPresenter().finishImportingPendingTasks();
    }
    
    /**
     * Receives a notification about the user has canceled the import.
     * @param errorMessage 
     */
    public void notifyCancellation(){
        setVisualStatus(ComponentStatus.CANCELLED);   
        this.parentView.getPresenter().finishImportingPendingTasks();
    }
    
    /**
     * Receives a notification about the import has finished.
     * @param endingMessage 
     */
    public void notifyEnding(String endingMessage){
        setVisualStatus(ComponentStatus.FINISHED,endingMessage);   
        this.parentView.getPresenter().finishImportingPendingTasks();    
        this.parentView.startDBLoading();
    }
    
    /**
     * Receives a notification about there was some error in the previous
     * excel file upload.
     * @param previousMessage 
     */
    public void notifyUploadingError(String previousMessage){
        setVisualStatus(ComponentStatus.PREVIOUS_ERROR, previousMessage);
    }
    
    
    /**
     * Receives an update about the progress of an ongoing import.
     * @param currentProgress
     * @param maxProgress 
     */
    public void notifyUploadingProgress(int currentProgress, int maxProgress){
        // For accessing the user interface we need to get access to the main thread
        // As we cannot be sure from what thread we are being called, it is better to use it by default.
        setVisualStatus(ComponentStatus.RUNNING, currentProgress+" / "+maxProgress);
        parentView.getMainUI().access(() ->  {
            processProgressBar.setMax(maxProgress);
            processProgressBar.setValue(currentProgress);

            getUI().get().push();
        });
    }
    
    /**
     * Private method that updates the status information container with the
     * received input.
     * @param text 
     */
    private void refreshOutputInfo(String text) {
        
        parentView.getMainUI().access(() -> {
            outputInfo.removeAll();
            HtmlComponent htmlParagraph = new HtmlComponent(Tag.P);
            htmlParagraph.getElement().setText(text);
            outputInfo.add(htmlParagraph);
        });
        
    }
    
    /**
     * Finish any pending task if the main window is closed.
     */
    public void windowClosed() {
        this.parentView.getPresenter().finishImportingPendingTasks();
    }
    

}
