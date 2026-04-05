package com.resturant.mud_cafe.controller;

import com.resturant.mud_cafe.dto.request.CreateTableRequest;
import com.resturant.mud_cafe.dto.request.UpdateTableStatusRequest;
import com.resturant.mud_cafe.dto.response.TableResponse;
import com.resturant.mud_cafe.service.TableService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tables")
@RequiredArgsConstructor
public class TableController {

    private final TableService tableService;

    @GetMapping
    public ResponseEntity<List<TableResponse>> getAllTables() {
        return ResponseEntity.ok(tableService.getAllTables());
    }

    @PostMapping
    public ResponseEntity<TableResponse> createTable(@RequestBody CreateTableRequest request) {
        return ResponseEntity.ok(tableService.createTable(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TableResponse> updateTable(
            @PathVariable Long id,
            @RequestBody CreateTableRequest request) {
        return ResponseEntity.ok(tableService.updateTable(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTable(@PathVariable Long id) {
        tableService.deleteTable(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TableResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateTableStatusRequest request) {
        return ResponseEntity.ok(tableService.updateStatus(id, request));
    }
}