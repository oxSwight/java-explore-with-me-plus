package ru.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndHitDto {

    Long id;
    String app;
    String uri;
    String ip;
    String timestamp;

}