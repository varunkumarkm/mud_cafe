package com.resturant.mud_cafe.repository;

import com.resturant.mud_cafe.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByProcessedAtBetween(LocalDateTime start, LocalDateTime end);
}