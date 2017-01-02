package application.infrastructure;

import application.infrastructure.models.SpecificationFile;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpecificationFileRepository extends PagingAndSortingRepository<SpecificationFile, Long> {

}
