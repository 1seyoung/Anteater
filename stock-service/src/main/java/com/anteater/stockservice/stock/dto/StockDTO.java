package com.anteater.stockservice.stock.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Data
public class StockDTO {

    private String isinCode;
    private String ticker;
    private String nameKo;
    private String nameShortKo;
    private LocalDate listingDate;
    private String market;
}
