package com.rrms.controller;

import com.rrms.common.ApiResponse;
import com.rrms.domain.enums.RoomStatus;
import com.rrms.dto.RoomDtos;
import com.rrms.security.AuthContext;
import com.rrms.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {
    private final RoomService roomService;
    public RoomController(RoomService roomService) { this.roomService = roomService; }

    @GetMapping
    public ApiResponse<List<RoomDtos.RoomResponse>> list(@RequestParam(required = false) RoomStatus status,
                                                          @RequestParam(required = false) String q) {
        AuthContext.requireAdmin();
        return ApiResponse.ok("Rooms retrieved.", roomService.list(status, q));
    }

    @PostMapping
    public ApiResponse<RoomDtos.RoomResponse> create(@Valid @RequestBody RoomDtos.RoomRequest request) {
        AuthContext.requireAdmin();
        return ApiResponse.ok("Room created successfully.", roomService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<RoomDtos.RoomResponse> update(@PathVariable Long id, @Valid @RequestBody RoomDtos.RoomRequest request) {
        AuthContext.requireAdmin();
        return ApiResponse.ok("Room updated successfully.", roomService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        AuthContext.requireAdmin();
        roomService.delete(id);
        return ApiResponse.ok("Room deleted successfully.", null);
    }
}
