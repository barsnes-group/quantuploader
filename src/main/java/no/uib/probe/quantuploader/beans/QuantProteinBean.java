/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package no.uib.probe.quantuploader.beans;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import javax.persistence.*;


/**
 *
 * @author Carlos Horro
 * @author Yehia Farag
 */
@Entity
public class QuantProteinBean implements Comparable, Serializable, Identifiable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    
    private String uniprotAccession, uniprotProteinName;		
    //private boolean peptideObject;

    private double rocAuc;
    
    private String sequance;

    @Transient
    private final HashMap<String,QuantDatasetProteinBean> quantDatasetProtMap = new HashMap<String,QuantDatasetProteinBean>();

    
    /**
     * Adds a QuantDatasetProteinBean, unless it already exists!
     * @param qdp 
     */
    public void addQuantDatasetProt(QuantDatasetProteinBean qdp){
        if (qdp != null && quantDatasetProtMap.get(qdp.getUniqLogicId()) == null){
            qdp.setQuantProteinBean(this);
            quantDatasetProtMap.put(qdp.getUniqLogicId(), qdp);
        } 
    }
    public List<QuantDatasetProteinBean> getQuantDatasetProtList(){
        return (List<QuantDatasetProteinBean>) quantDatasetProtMap.values();
    }
    
    public HashMap<String,QuantDatasetProteinBean> getQuantDatasetProtMap(){
        return quantDatasetProtMap;
    }
    
    

    // TODO: I'M USING HERE UNIPROT ACCESSION AS UNIQUE IDENTIFIER FOR PROTEINS, TO CONFIRM THAT!
    /*
    Feedback by Astrid: Uniprot accession numbers are, sometimes, changed. So, they are the best
    way to identify proteins but they have to be properly updated!
    */
    /**
     * Derived unique id, in this case from its uniprot accession number
     * @return 
     */
    @Override
    public String getUniqLogicId() {
        return this.getUniprotAccession();
    } 
    
    
          
    public String getUniprotAccession() {
        return uniprotAccession;
    }

    public void setUniprotAccession(String uniprotAccession) {
        this.uniprotAccession = uniprotAccession;
    }
    
    public String getUniprotProteinName() {
        return uniprotProteinName;
    }

    public void setUniprotProteinName(String uniprotProteinName) {
        this.uniprotProteinName = uniprotProteinName;
    }
                 
    public double getRocAuc() {
        return rocAuc;
    }

    public void setRocAuc(double rocAuc) {
        this.rocAuc = rocAuc;
    }
    
/*
    public boolean isPeptideObject() {
        return peptideObject;
    }

    public void setPeptideObject(boolean peptideObject) {
        this.peptideObject = peptideObject;
    }
*/

    public String getSequance() {
        return sequance;
    }

    public void setSequance(String sequance) {
        this.sequance = sequance;
    }
    
    @Override
    public int compareTo(Object t) {
        if(t instanceof QuantDatasetProteinBean){
            return this.getUniqLogicId().compareTo(((QuantDatasetProteinBean)t).getUniqLogicId());
        } else {
            return -1;
        }
    }    


    
}
