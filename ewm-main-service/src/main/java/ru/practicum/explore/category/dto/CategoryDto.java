package ru.practicum.explore.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDto {
    @NotBlank                          // «не null, не пусто, не пробелы»
    @Size(max = 50)                    // ограничение из ТЗ
    private String name;
}