package com.resturant.mud_cafe.repository;

import com.resturant.mud_cafe.entity.Bill;
import com.resturant.mud_cafe.entity.RestaurantTable;
import com.resturant.mud_cafe.enums.BillStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    Optional<Bill> findByTableAndStatus(RestaurantTable table, BillStatus status);

    List<Bill> findByStatus(BillStatus status);

    List<Bill> findByPaidAtBetween(LocalDateTime start, LocalDateTime end);

    // Eagerly fetch table and staff to avoid LazyInitializationException
    @Query("SELECT b FROM Bill b " +
            "JOIN FETCH b.table " +
            "JOIN FETCH b.staff " +
            "WHERE b.status = 'PAID' " +
            "ORDER BY b.paidAt DESC")
    List<Bill> findRecentPaidBills(Pageable pageable);
}