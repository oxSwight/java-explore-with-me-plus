package ru.practicum.explore.comments.dto;

import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import lombok.Setter;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NewCommentDto {
    @Positive
    @NotNull
    private Long eventId;
    @NotBlank
    @Size(min = 1, max = 678)
    private String text;
}
