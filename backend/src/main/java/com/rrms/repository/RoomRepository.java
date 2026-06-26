package com.rrms.repository;

import com.rrms.domain.entity.Room;
import com.rrms.domain.enums.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByRoomCodeIgnoreCase(String roomCode);
    List<Room> findByStatus(RoomStatus status);
}
