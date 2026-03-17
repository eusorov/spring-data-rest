package com.example.spring_data_rest.backtest.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.spring_data_rest.backtest.api.dto.BacktestListItemDto;
import com.example.spring_data_rest.backtest.api.dto.BacktestResponseDto;

public interface BacktestService {

	Page<BacktestListItemDto> listBacktests(
		String method,
		String asset,
		String currency,
		Long dateFrom,
		Long dateTo,
		Pageable pageable
	);

	BacktestResponseDto getById(Long id);

	BacktestResponseDto getByAnyKey(
		Optional<String> method,
		Optional<String> asset,
		Optional<String> currency,
		Optional<Long> dateFrom,
		Optional<Long> dateTo,
		Optional<String> configHash
	);
}

