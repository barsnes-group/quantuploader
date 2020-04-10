package no.uib.probe.quantuploader.components;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.FailedEvent;
import com.vaadin.flow.component.upload.FileRejectedEvent;
import com.vaadin.flow.component.upload.SucceededEvent;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import java.util.logging.Logger;
import no.uib.probe.quantuploader.enums.ComponentStatus;

/**
 *
 * @author carlos.horro
 */
public class UploadCustomComponent extends VerticalLayout {
        
    private static final Logger LOGGER = Logger.getLogger(UploadCustomComponent.class.getName());

    private UploadView parentView;    
    
    private Upload uploader;
    
    private Paragraph title;
    
    private Div outputInfo;    
    
    private ComponentStatus componentStatus;
        
    public UploadCustomComponent(UploadView parentView){    
        this.parentView = parentView;

        setMargin(true);
        
        title = new Paragraph(new HtmlComponent(Tag.P));
        outputInfo = new Div();
        
        // uploader predefined-component settings
        MemoryBuffer buffer = new MemoryBuffer(); // FileBuffer if there is any memory problem uploading things?
        //FileBuffer buffer = new FileBuffer(); // FileBuffer seems to have an internal problem in vaadin and its inputStream always returns null
        uploader = new Upload(buffer);
        uploader.setMaxFiles(1);
        uploader.setAcceptedFileTypes("application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        uploader.addSucceededListener(getFileSucceededEventListener());
        uploader.addFileRejectedListener(getFileRejectedListener());
        uploader.addFailedListener(getFileFailedEventListener());
        
        //@formatter:on
        uploader.setId("test-upload");
        outputInfo.setId("test-output");
        title.getElement().setText("1. Upload");
        
        // adding subcomponents to the layout
        add(title);
        add(uploader);
        add(outputInfo);
            
        setVisualStatus(ComponentStatus.NOT_INITIALIZED);
        System.out.println("end LoadView");

    }
    
    /**
     * Initializes all component structures.
     */
    private void initializeSubcomponents(){
        uploader.setDropLabel(new Label("Upload a file in excel format"));
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
            case NOT_INITIALIZED:
                initializeSubcomponents();
                break;
            case RUNNING:                
                break;                
            case FINISHED:                
                break;             
            case PREVIOUS_ERROR:
                fullMessage = "There was an error before loading the component"+((extraMessage != null)?": "+extraMessage:"");
                refreshOutputInfo(fullMessage);
            break;          
            case RUNNING_ERROR:
                fullMessage = "There was an error trying to upload the file"+((extraMessage != null)?": "+extraMessage:"");
                refreshOutputInfo(fullMessage);
            break;
            case CANCELLED:
                fullMessage = "Importing process was cancelled"+((extraMessage != null)?": "+extraMessage:"");
                refreshOutputInfo(fullMessage);
            break;
                
                
        }
        this.componentStatus = componentStatus;
    }

    
    private void refreshOutputInfo(String text) {
        parentView.getMainUI().access(() -> {
            outputInfo.removeAll();
            HtmlComponent htmlParagraph = new HtmlComponent(Tag.P);
            htmlParagraph.getElement().setText(text);
            outputInfo.add(htmlParagraph);
        });
    }
    
    
    // EVENTS AND LISTENERS
    
    /**
     * This event is fired just when the file to be uploaded doesn't fit any
     * necessary requirement to do so.
     * @return Listener for the event
     */
    private ComponentEventListener<FileRejectedEvent> getFileRejectedListener(){
        ComponentEventListener<FileRejectedEvent> myEvent = event -> {
            //refreshOutputInfo(event.getErrorMessage());
            setVisualStatus(ComponentStatus.RUNNING_ERROR,event.getErrorMessage());
        };
        return myEvent;
    }
    

    /**
     * This event is fired just when the excel file has been successfully uploaded
     * @return Listener for the event
     */
    private ComponentEventListener<SucceededEvent> getFileSucceededEventListener(){
        ComponentEventListener<SucceededEvent> myEvent = event -> {
            // Once the uploading process has properly finished, we can start the 
            // thread for loading the data into memory.
            //Paragraph component = new Paragraph();
            setVisualStatus(ComponentStatus.FINISHED);
            // We notify to the main view that the importing procedure should start
            this.parentView.startImport(((MemoryBuffer)uploader.getReceiver()).getInputStream(), event.getFileName());
            
        };
        return myEvent;
    }
    
    /**
     * This event is fired just when the excel file has had some problem trying 
     * to be uploaded
     * @return Listener for the event
     */
    private ComponentEventListener<FailedEvent> getFileFailedEventListener(){
        ComponentEventListener<FailedEvent> myEvent = event -> {
            setVisualStatus(ComponentStatus.RUNNING_ERROR);
            // We notify to the main view that the importing procedure should start            
        };
        return myEvent;
    }
    
    
    /**
     * Finish any pending task if the main window is closed.
     */
    public void windowClosed() {
        
    }
    

}
