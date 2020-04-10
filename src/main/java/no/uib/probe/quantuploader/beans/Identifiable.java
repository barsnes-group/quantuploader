
package no.uib.probe.quantuploader.beans;

/**
 *
 * @author carlos.horro
 */
public interface Identifiable {
    
    /**
     * Returns the UniqLogicId of the entity. This String uniquely identifies any
     * bean loaded in the system, and it's just used during the loading procedure.
     * After data is loaded into the database, used id will be the ID column 
     * automatically generated.
     * @return 
     */
    public String getUniqLogicId();
        

}
