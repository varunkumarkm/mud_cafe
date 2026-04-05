package com.resturant.mud_cafe.service;

import com.resturant.mud_cafe.dto.request.CreateTableRequest;
import com.resturant.mud_cafe.dto.request.UpdateTableStatusRequest;
import com.resturant.mud_cafe.dto.response.TableResponse;
import com.resturant.mud_cafe.entity.RestaurantTable;
import com.resturant.mud_cafe.enums.TableStatus;
import com.resturant.mud_cafe.exception.ResourceNotFoundException;
import com.resturant.mud_cafe.repository.TableRepository;
import com.resturant.mud_cafe.websocket.NotificationPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TableService {

    private final TableRepository tableRepository;
    private final NotificationPublisher notificationPublisher;

    public List<TableResponse> getAllTables() {
        return tableRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public TableResponse createTable(CreateTableRequest request) {
        RestaurantTable table = RestaurantTable.builder()
                .name(request.getName())
                .floor(request.getFloor())
                .section(request.getSection())
                .capacity(request.getCapacity())
                .status(TableStatus.AVAILABLE)
                .build();
        return toResponse(tableRepository.save(table));
    }

    public TableResponse updateTable(Long id, CreateTableRequest request) {
        RestaurantTable table = tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Table not found"));
        table.setName(request.getName());
        table.setFloor(request.getFloor());
        table.setSection(request.getSection());
        table.setCapacity(request.getCapacity());
        return toResponse(tableRepository.save(table));
    }

    public void deleteTable(Long id) {
        if (!tableRepository.existsById(id)) {
            throw new ResourceNotFoundException("Table not found with id: " + id);
        }
        tableRepository.deleteById(id);
    }

    public TableResponse updateStatus(Long id, UpdateTableStatusRequest request) {
        RestaurantTable table = tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Table not found with id: " + id));
        table.setStatus(TableStatus.valueOf(request.getStatus()));
        RestaurantTable saved = tableRepository.save(table);
        notificationPublisher.publishTableStatusUpdate(id, request.getStatus());
        return toResponse(saved);
    }

    public TableResponse toResponse(RestaurantTable table) {
        return TableResponse.builder()
                .id(table.getId())
                .name(table.getName())
                .floor(table.getFloor())
                .section(table.getSection())
                .capacity(table.getCapacity())
                .status(table.getStatus().name())
                .createdAt(table.getCreatedAt())
                .build();
    }
}