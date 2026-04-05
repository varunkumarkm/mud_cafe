package com.resturant.mud_cafe.repository;

import com.resturant.mud_cafe.entity.RestaurantTable;
import com.resturant.mud_cafe.enums.TableStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TableRepository extends JpaRepository<RestaurantTable, Long> {
    List<RestaurantTable> findByStatus(TableStatus status);
    List<RestaurantTable> findByFloor(String floor);
}