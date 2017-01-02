package application.infrastructure;

import application.infrastructure.models.Api;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiRepository extends PagingAndSortingRepository<Api, Long> {
    Api findByNameAndVersion(String name, String version);

    Page<Api> findAllByOrderByNameAscVersionAsc(Pageable pageable);
}
