package com.delivery.repository;

import com.delivery.model.Courier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourierRepository extends JpaRepository<Courier, Long> {
    Optional<Courier> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Courier> findByStatus(Courier.CourierStatus status);
    Optional<Courier> findFirstByStatusOrderByLastAssignmentAsc(Courier.CourierStatus status);
    List<Courier> findByLastAssignmentBeforeAndLastAssignmentIsNotNull(LocalDateTime date);
}
