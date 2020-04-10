
package no.uib.probe.quantuploader.repository;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author carlos.horro
 */
public class RepoReturnInfo {
    
    public Set savedEntities;
    public Set unsavedExistingEntities;
    public int numProcessedEntities;
    
    public RepoReturnInfo(){
        savedEntities = new HashSet();
        unsavedExistingEntities = new HashSet();
        numProcessedEntities = 0;
    }
}
