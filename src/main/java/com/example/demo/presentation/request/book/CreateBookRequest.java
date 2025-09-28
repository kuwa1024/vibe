package com.example.demo.presentation.request.book;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateBookRequest {
    @NotNull
    @Size(min = 13, max = 13)
    private String isbn;

    @NotEmpty
    private String title;

    @NotNull
    @Min(0)
    private Integer price;

    @NotNull
    @Min(0)
    private Integer stock;
}
