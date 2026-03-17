package com.example.spring_data_rest.backtest.api.dto;

public record BacktestListItemDto(
	Long id,
	String method,
	String asset,
	String currency,
	Long dateFrom,
	Long dateTo,
	String configHash
) {
}

