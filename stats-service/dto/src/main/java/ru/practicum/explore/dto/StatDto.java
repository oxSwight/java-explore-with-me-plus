package ru.practicum.explore.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class StatDto {

    String app;
    String uri;
    Long hits;

}