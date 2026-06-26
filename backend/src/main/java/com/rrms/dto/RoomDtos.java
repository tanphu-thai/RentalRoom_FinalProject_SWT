package com.rrms.dto;

import com.rrms.domain.enums.RoomStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public final class RoomDtos {
    private RoomDtos() { }

    public record RoomRequest(@NotBlank(message = "Room code is required.") String roomCode,
                              @NotBlank(message = "Room type is required.") String roomType,
                              @NotNull(message = "Area is required.") BigDecimal area,
                              @NotNull(message = "Base price is required.") BigDecimal basePrice,
                              @NotNull(message = "Room status is required.") RoomStatus status) { }

    public record RoomResponse(Long id, String roomCode, String roomType, BigDecimal area,
                               BigDecimal basePrice, RoomStatus status) { }
}
