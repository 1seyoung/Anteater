package com.anteater.stockservice.stock.service;


import com.anteater.stockservice.stock.dto.StockDTO;
import com.anteater.stockservice.stock.entity.Stock;
import com.anteater.stockservice.stock.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StockService {

    private final StockRepository stockRepository;

    @Autowired
    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    public Map<String, StockDTO> getStocksByIsinCodes(List<String> isinCodes) {
        return stockRepository.findAllByIsinCodeIn(isinCodes).stream()
                .collect(Collectors.toMap(Stock::getIsinCode, this::convertToDTO));
    }

    public Optional<StockDTO> getStockByIsinCode(String isinCode) {
        return stockRepository.findById(isinCode).map(this::convertToDTO);
    }



    private StockDTO convertToDTO(Stock stock) {
        StockDTO dto = new StockDTO();
        dto.setIsinCode(stock.getIsinCode());
        dto.setTicker(stock.getTicker());
        dto.setNameKo(stock.getNameKo());
        dto.setNameShortKo(stock.getNameShortKo());
        dto.setListingDate(stock.getListingDate());
        dto.setMarket(stock.getMarket());
        // 실시간 데이터 로직은 여기에 추가
        return dto;
    }
}