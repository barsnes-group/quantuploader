
package no.uib.probe.quantuploader.repository;

import no.uib.probe.quantuploader.beans.QuantStudyBean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 *
 * @author carlos.horro
 */
public interface StudiesRepository extends JpaRepository<QuantStudyBean, Long>, StudiesRepositoryCustom {
    
    //boolean existsQuantStudyBeanByStudyKey(String studyKey);

    
}