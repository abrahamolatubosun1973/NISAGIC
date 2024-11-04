package nisagic.nisagic.repository;

import nisagic.nisagic.model.NisaUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface NisaUserRepo extends JpaRepository<NisaUser, Long> {
    Optional<NisaUser> findByEmail(String email);
    Optional<NisaUser> findByConfirmationCode(String code);
}
