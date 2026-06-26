package com.rrms.service;

import com.rrms.common.BusinessException;
import com.rrms.domain.entity.Room;
import com.rrms.domain.enums.RoomStatus;
import com.rrms.dto.RoomDtos;
import com.rrms.repository.RentalContractRepository;
import com.rrms.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {
    @Mock private RoomRepository roomRepository;
    @Mock private RentalContractRepository contractRepository;
    private RoomService roomService;

    @BeforeEach
    void setUp() { roomService = new RoomService(roomRepository, contractRepository); }

    @Test
    void create_duplicateRoomCode_throwBusinessException() {
        Room existing = new Room(); existing.setId(1L); existing.setRoomCode("R101");
        when(roomRepository.findByRoomCodeIgnoreCase("R101")).thenReturn(Optional.of(existing));
        RoomDtos.RoomRequest request = new RoomDtos.RoomRequest("R101", "Single", new BigDecimal("20"), new BigDecimal("2500000"), RoomStatus.VACANT);

        BusinessException ex = assertThrows(BusinessException.class, () -> roomService.create(request));

        assertEquals("Room ID already exists.", ex.getMessage());
        verify(roomRepository, never()).save(any());
    }

    @Test
    void create_validRoom_saveSuccessfully() {
        when(roomRepository.findByRoomCodeIgnoreCase("R103")).thenReturn(Optional.empty());
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> {
            Room room = invocation.getArgument(0); room.setId(3L); return room;
        });
        RoomDtos.RoomRequest request = new RoomDtos.RoomRequest("r103", "Single", new BigDecimal("21"), new BigDecimal("2700000"), RoomStatus.VACANT);

        RoomDtos.RoomResponse response = roomService.create(request);

        assertEquals(3L, response.id());
        assertEquals("R103", response.roomCode());
        ArgumentCaptor<Room> captor = ArgumentCaptor.forClass(Room.class);
        verify(roomRepository).save(captor.capture());
        assertEquals(RoomStatus.VACANT, captor.getValue().getStatus());
    }
}
