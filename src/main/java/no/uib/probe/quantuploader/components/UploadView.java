package no.uib.probe.quantuploader.components;

import no.uib.probe.quantuploader.components.UploadImportSavePresenter;
import no.uib.probe.quantuploader.components.UploadCustomComponent;
import no.uib.probe.quantuploader.components.ImportComponent;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import java.io.InputStream;

import java.util.logging.Level;
import java.util.logging.Logger;
import no.uib.probe.quantuploader.enums.ComponentStatus;
import org.springframework.beans.factory.annotation.Autowired;



/**
 * A sample Vaadin view class.
 * <p>
 * To implement a Vaadin view just extend any Vaadin component and
 * use @Route annotation to announce it in a URL as a Spring managed
 * bean.
 * Use the @PWA annotation make the application installable on phones,
 * tablets and some desktop browsers.
 * <p>
 * A new instance of this class is created for every new user and every
 * browser tab/window.
 */

@Route("")
@PWA(name = "Uploader application",shortName = "Uploader App")
@Push
//@CssImport("./styles/shared-styles.css")
//@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
public class UploadView extends HorizontalLayout implements View  {
        
    private static final Logger LOGGER = Logger.getLogger(UploadView.class.getName());
    
    private UI ui;
    
    @Autowired
    private UploadImportSavePresenter uploadPresenter;
       
    private UploadCustomComponent myUploaderComponent;
    private ImportComponent myImportComponent;
    private DBLoadComponent myDBLoaderComponent;
    

    
    public UploadView() {
        
        ui = UI.getCurrent();
        
        myUploaderComponent = new UploadCustomComponent(this);
        this.add(myUploaderComponent);
        
        myImportComponent = new ImportComponent(this);
        this.add(myImportComponent);
        
        myDBLoaderComponent = new DBLoadComponent(this);
        this.add(myDBLoaderComponent);
        
        
        System.out.println("end SingleUploaderView");
        
        // client management when the user close the window
        UI.getCurrent().getPage().executeJs("function closeListener() { $0.$server.windowClosed(); } " +
        "window.addEventListener('beforeunload', closeListener); " +
        "window.addEventListener('unload', closeListener);",getElement());

    }
    
    public UploadImportSavePresenter getPresenter() {
            return uploadPresenter;
    }
    
    public UI getMainUI(){
        return ui;
    }
    
    /**
     * Initialises import component
     */
    public void initialiseImportComponent(){
        myImportComponent.notifyInitialiseImportComponent();
    }
    
    
    /**
     * Coordinates the required actions to start the excel data import.
     * @param inputStream
     * @param fileName 
     */
    public void startImport(InputStream inputStream, String fileName){
        myImportComponent.notifyStartImport(inputStream, fileName);
    }
    
    /**
     * Initialises db loading component
     */
    public void initialiseDBLoadingComponent(){
        myDBLoaderComponent.notifyInitialiseDBLoadingComponent();
    }
    
    /**
     * Coordinates the required actions to start the DB data loading
     */
    public void startDBLoading(){
        myDBLoaderComponent.notifyStartDBLoading();
    }
    
    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    @ClientCallable
    public void windowClosed() {
        myUploaderComponent.windowClosed();
        myImportComponent.windowClosed();
        LOGGER.log(Level.FINE, "Window closed");
        //System.out.println("Window closed");
    }

}
