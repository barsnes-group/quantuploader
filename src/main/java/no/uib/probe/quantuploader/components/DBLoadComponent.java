package no.uib.probe.quantuploader.components;

//import ch.carnet.kasparscherrer.VerticalScrollLayout;
import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.progressbar.ProgressBarVariant;
import com.vaadin.flow.component.textfield.TextArea;

import java.util.logging.Level;
import java.util.logging.Logger;
import no.uib.probe.quantuploader.enums.ComponentStatus;

//import org.vaadin.addons.collapsiblepanel.CollapsiblePanel;


/**
 *
 * @author carlos.horro
 */
public class DBLoadComponent extends VerticalLayout  {
        
    private static final Logger LOGGER = Logger.getLogger(DBLoadComponent.class.getName());
    
    private UploadView parentView;
    
    private ComponentStatus componentStatus;

    // Contains everything but the title
    private VerticalLayout mainBodyContainer;

    Paragraph title;
    private Div outputInfo;        
    private ProgressBar processProgressBar;
    private TextArea errorDetailsPanel;
    
    public DBLoadComponent(UploadView parentView) {    
        this.parentView = parentView;
        
        setMargin(true);
        
        // The mainBodyContainer will contain everything but the title and the errorDetailsPanel
        mainBodyContainer = new VerticalLayout();
        
        // TODO: to improve styling getting styleclassnames from uploader
        // don't work: Template.get, Component.get, getElementById("fuel-name-1");, test-upload.
        
        //mainBodyContainer.getStyle().set( "border-style" , "solid" ) ; 
        //mainBodyContainer.getStyle().set( "border" , "1px solid black" ) ; 
        mainBodyContainer.getStyle().set( "border" , "1px dashed black" ) ; 
        
        outputInfo = new Div();        
        outputInfo.setId("test-output");

        title = new Paragraph(new HtmlComponent(Tag.P));
        title.getElement().setText("3. DB load");
        
        processProgressBar = new ProgressBar();
        processProgressBar.setIndeterminate(false);
        processProgressBar.setMin(0);
       
        errorDetailsPanel = new TextArea();
        errorDetailsPanel.setWidthFull();
        //errorDetailsPanel.setHeight("400px");
        errorDetailsPanel.setHeightFull();
        errorDetailsPanel.setReadOnly(true);
        errorDetailsPanel.getStyle().set( "border" , "1px dotted orange" ) ; 
        errorDetailsPanel.setVisible(false);
        
        // adding subcomponents to the layout
        add(title);
        add(mainBodyContainer);
        mainBodyContainer.add(outputInfo);
        mainBodyContainer.add(processProgressBar);
        add(errorDetailsPanel);
        
        
        initializeSubcomponents();
        LOGGER.log(Level.FINE, "end DBLoaderComponent");

    }
    
    /**
     * Initializes all component structures.
     */
    private void initializeSubcomponents(){
        setVisualStatus(ComponentStatus.NOT_INITIALIZED,"","");
        // presenter may still be null if we are just starting the app.
        //if (this.parentView.getPresenter() != null)
            //this.parentView.getPresenter().finishDBLoadingPendingTasks();
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
        setVisualStatus(componentStatus, extraMessage, null);
    }
    
    /**
     * Method that adapts component visual status to the working status of its
     * procedures.
     * @param componentStatus
     * @param extraMessage 
     * @param errorDetails 
     */
    private void setVisualStatus(ComponentStatus componentStatus, String extraMessage, String errorDetails){
        String fullMessage = "";
        switch(componentStatus){
            case NOT_INITIALIZED:
                fullMessage = "Waiting for importing..."+((extraMessage != null)?" "+extraMessage:"");
                parentView.getMainUI().access(() ->  {
                    processProgressBar.setMax(1);
                    processProgressBar.setValue(0);
                    processProgressBar.removeThemeVariants(ProgressBarVariant.LUMO_SUCCESS,ProgressBarVariant.LUMO_ERROR);
                });
                refreshOutputInfo(fullMessage);
                refreshErrorDetails(errorDetails);
                break;
            case RUNNING:
                fullMessage = "Loading data into the DB..."+((extraMessage != null)?" "+extraMessage:"");
                refreshOutputInfo(fullMessage);
                refreshErrorDetails(errorDetails);

                parentView.getMainUI().access(() ->  {
                    getUI().get().push();
                });
                break;
            case FINISHED:
                fullMessage = "Data properly loaded into the DB"+((extraMessage != null)?": "+extraMessage:"");
                refreshOutputInfo(fullMessage);
                refreshErrorDetails(errorDetails);
                parentView.getMainUI().access(() ->  {
                    processProgressBar.addThemeVariants(ProgressBarVariant.LUMO_SUCCESS);
                });
                break;
            case PREVIOUS_ERROR:
                fullMessage = "There was an error importing the data "+((extraMessage != null)?": "+extraMessage:"");
                parentView.getMainUI().access(() ->  {
                    processProgressBar.addThemeVariants(ProgressBarVariant.LUMO_ERROR);
                });
                refreshOutputInfo(fullMessage);
                refreshErrorDetails(errorDetails);

            break;          
            case RUNNING_ERROR:
                fullMessage = "There was an error persisting the data"+((extraMessage != null)?": "+extraMessage:"");
                refreshOutputInfo(fullMessage);
                refreshErrorDetails(errorDetails);
                parentView.getMainUI().access(() ->  {
                    processProgressBar.addThemeVariants(ProgressBarVariant.LUMO_ERROR);
                });
            break;
            case CANCELLED:
                fullMessage = "Loading process was cancelled"+((extraMessage != null)?": "+extraMessage:"");
                refreshOutputInfo(fullMessage);
                refreshErrorDetails(errorDetails);
                parentView.getMainUI().access(() ->  {
                    processProgressBar.addThemeVariants(ProgressBarVariant.LUMO_ERROR);
                });
            break;
                
        }
        this.componentStatus = componentStatus;
    }
    
    /**
     * Receives a notification about the loading of the data into the db must start.
     * @param inputStream reference to the input stream to read the data from
     * @param fileName name of the source file
     */
    public void notifyStartDBLoading(){
        setVisualStatus(ComponentStatus.RUNNING);
        this.parentView.getPresenter().startDBLoading(this);
    }
    
    /**
     * Receives a notification about an internal error loading data into the DB
     * @param errorMessage 
     * @param errorDetails
     */
    public void notifyRunningError(String errorMessage, String errorDetails){
        setVisualStatus(ComponentStatus.RUNNING_ERROR, errorMessage, errorDetails);
        this.parentView.getPresenter().finishDBLoadingPendingTasks();
    }
    
    /**
     * Receives a notification about the user has canceled the db loading.
     * @param errorMessage 
     */
    public void notifyCancellation(){
        setVisualStatus(ComponentStatus.CANCELLED);   
        this.parentView.getPresenter().finishDBLoadingPendingTasks();
    }
    
    /**
     * Receives a notification about the import has finished.
     * @param endingMessage 
     */
    public void notifyEnding(String endingMessage){
        setVisualStatus(ComponentStatus.FINISHED,endingMessage);   
        this.parentView.getPresenter().finishDBLoadingPendingTasks();
    }
    
    /**
     * Receives a notification about there was some error in the previous
     * excel file upload.
     * @param previousMessage 
     * @param detailedError
     */
    public void notifyUploadingError(String previousMessage, String detailedError){
        setVisualStatus(ComponentStatus.PREVIOUS_ERROR, previousMessage, detailedError);
    }
    
    /**
     * Receives an update about the progress of an ongoing import.
     * @param currentProgress
     */
    public void notifyUploadingProgress(int currentProgress){
        notifyUploadingProgress(currentProgress, null);
    }
    
    /**
     * Receives an update about the progress of an ongoing import.
     * @param currentProgress
     * @param maxProgress It may be optional if it was previously defined
     */
    public void notifyUploadingProgress(int currentProgress, Integer maxProgress){
        // For accessing the user interface we need to get access to the main thread
        // As we cannot be sure from what thread we are being called, it is better to use it by default.
        if (maxProgress != null){
            LOGGER.log(Level.FINEST, "notifyUploadingProgress currentProgress:"+currentProgress+"; maxProgress:"+maxProgress);
            //System.out.println("notifyUploadingProgress currentProgress:"+currentProgress+"; maxProgress:"+maxProgress);
            setVisualStatus(ComponentStatus.RUNNING, currentProgress+" / "+maxProgress);
        }else{
            LOGGER.log(Level.FINEST, "notifyUploadingProgress currentProgress:"+currentProgress+"; maxProgress not defined, using preestablished:"+processProgressBar.getMax());
            //System.out.println("notifyUploadingProgress currentProgress:"+currentProgress+"; maxProgress not defined, using preestablished:"+processProgressBar.getMax());
            setVisualStatus(ComponentStatus.RUNNING, currentProgress+" / "+processProgressBar.getMax());
        }
        
        parentView.getMainUI().access(() ->  {
        //getUI().get().access(() ->  {
            if (maxProgress!=null)
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
            if (text != null){
                outputInfo.removeAll();
                HtmlComponent htmlParagraph = new HtmlComponent(Tag.P);
                htmlParagraph.getElement().setText(text);
                outputInfo.add(htmlParagraph);
            }
        });
        
    }
    
    /**
     * Private method that updates the details of an internal error.
     * @param details 
     */
    private void refreshErrorDetails(String details){
        parentView.getMainUI().access(() -> {
            if (details != null){
                if (details.trim().equals("")){
                    errorDetailsPanel.setVisible(false);
                }else
                    errorDetailsPanel.setVisible(true);
                errorDetailsPanel.setValue(details);

            }
        });
    }
    
    /**
     * Finish any pending task if the main window is closed.
     */
    public void windowClosed() {
        this.parentView.getPresenter().finishDBLoadingPendingTasks();
    }
    

}
