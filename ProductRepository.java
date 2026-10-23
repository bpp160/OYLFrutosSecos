package cl.oyl.backend;
import org.springframework.data.jpa.repository.*;import org.springframework.data.repository.query.Param;import jakarta.persistence.LockModeType;import java.util.*;
public interface ProductRepository extends JpaRepository<Product,Integer>{
@Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select p from Product p where p.id=:id") Optional<Product> locked(@Param("id") Integer id);
}
