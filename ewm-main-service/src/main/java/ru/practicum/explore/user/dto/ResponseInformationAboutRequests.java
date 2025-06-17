package ru.practicum.explore.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseInformationAboutRequests {
    private List<RequestDto> confirmedRequests;
    private List<RequestDto> rejectedRequests;
    private List<RequestDto> pendingRequests;
}