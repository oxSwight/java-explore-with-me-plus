package ru.practicum.explore.comments.dto;

import jakarta.validation.constraints.Size;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateCommentDto {
    @Size(min = 1, max = 678)
    @NotBlank
    private String text;
}
