
package no.uib.probe.quantuploader.repository;
import no.uib.probe.quantuploader.beans.QuantProteinBean;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author carlos.horro
 * Other types: CrudRepository, JpaRepository
 */
public interface ProteinsRepository extends /*Repository*/ JpaRepository<QuantProteinBean, Long>/*, CrudRepository<QuantProteinBean, Long>*/, ProteinsRepositoryCustom {
    
}