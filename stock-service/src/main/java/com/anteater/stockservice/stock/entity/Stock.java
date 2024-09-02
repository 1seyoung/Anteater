package com.anteater.stockservice.stock.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "stocks")
public class Stock {

    //표준 코드
    @Id
    @Column(name = "isin_code", nullable = false, length = 12)
    private String isinCode;

    // 단축코드
    @Column(name = "ticker", nullable = false, length = 10)
    private String ticker;

    // 한글 종목명
    @Column(name = "name_ko", nullable = false, length = 100)
    private String nameKo;

    //한글 종목약명
    @Column(name = "name_short_ko", nullable = false, length = 50)
    private String nameShortKo;

    // 상장일
    @Column(name = "listing_date", nullable = false)
    private LocalDate listingDate;


    //시장
    @Column(name = "market", nullable = false, length = 10)
    private String market;

}