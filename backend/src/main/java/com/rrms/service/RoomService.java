package com.rrms.service;

import com.rrms.common.BusinessException;
import com.rrms.domain.entity.Room;
import com.rrms.domain.enums.RoomStatus;
import com.rrms.dto.RoomDtos;
import com.rrms.repository.RentalContractRepository;
import com.rrms.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
public class RoomService {
    private final RoomRepository roomRepository;
    private final RentalContractRepository contractRepository;

    public RoomService(RoomRepository roomRepository, RentalContractRepository contractRepository) {
        this.roomRepository = roomRepository;
        this.contractRepository = contractRepository;
    }

    public List<RoomDtos.RoomResponse> list(RoomStatus status, String keyword) {
        List<Room> rooms = status == null ? roomRepository.findAll() : roomRepository.findByStatus(status);
        String normalized = keyword == null ? "" : keyword.trim().toLowerCase();
        return rooms.stream()
                .filter(room -> normalized.isBlank() || room.getRoomCode().toLowerCase().contains(normalized)
                        || room.getRoomType().toLowerCase().contains(normalized))
                .sorted(Comparator.comparing(Room::getRoomCode))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public RoomDtos.RoomResponse create(RoomDtos.RoomRequest request) {
        validateRequest(request);
        String code = request.roomCode().trim().toUpperCase();
        if (roomRepository.findByRoomCodeIgnoreCase(code).isPresent()) {
            throw BusinessException.badRequest("Room ID already exists.");
        }
        Room room = new Room();
        apply(room, request, code);
        return toResponse(roomRepository.save(room));
    }

    @Transactional
    public RoomDtos.RoomResponse update(Long id, RoomDtos.RoomRequest request) {
        validateRequest(request);
        Room room = getEntity(id);
        String code = request.roomCode().trim().toUpperCase();
        roomRepository.findByRoomCodeIgnoreCase(code)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> { throw BusinessException.badRequest("Room ID already exists."); });
        if (room.getStatus() == RoomStatus.OCCUPIED && room.getBasePrice().compareTo(request.basePrice()) != 0) {
            throw BusinessException.badRequest("Base rental price cannot be modified when room status is Occupied.");
        }
        apply(room, request, code);
        return toResponse(roomRepository.save(room));
    }

    @Transactional
    public void delete(Long id) {
        Room room = getEntity(id);
        if (contractRepository.existsByRoomId(room.getId())) {
            throw BusinessException.badRequest("Cannot delete room because related contract or invoice data exists.");
        }
        roomRepository.delete(room);
    }

    public Room getEntity(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("Room not found."));
    }

    private void validateRequest(RoomDtos.RoomRequest request) {
        if (request.area().compareTo(BigDecimal.ZERO) <= 0) {
            throw BusinessException.badRequest("Area must be greater than 0.");
        }
        if (request.basePrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw BusinessException.badRequest("Base price must be greater than 0.");
        }
    }

    private void apply(Room room, RoomDtos.RoomRequest request, String code) {
        room.setRoomCode(code);
        room.setRoomType(request.roomType().trim());
        room.setArea(request.area());
        room.setBasePrice(request.basePrice());
        room.setStatus(request.status());
    }

    public RoomDtos.RoomResponse toResponse(Room room) {
        return new RoomDtos.RoomResponse(room.getId(), room.getRoomCode(), room.getRoomType(),
                room.getArea(), room.getBasePrice(), room.getStatus());
    }
}
