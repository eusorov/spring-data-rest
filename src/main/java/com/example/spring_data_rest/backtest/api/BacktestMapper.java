package com.example.spring_data_rest.backtest.api;

import com.example.spring_data_rest.backtest.api.dto.BacktestListItemDto;
import com.example.spring_data_rest.backtest.api.dto.BacktestResponseDto;
import com.example.spring_data_rest.backtest.domain.BacktestEntity;

public class BacktestMapper {

	public BacktestListItemDto toListItemDto(BacktestEntity entity) {
		BacktestListItemDto dto = new BacktestListItemDto(
			entity.getId(),
			entity.getMethod(),
			entity.getAsset(),
			entity.getCurrency(),
			entity.getDateFrom(),
			entity.getDateTo(),
			entity.getConfigHash()
		);
		return dto;
	}

	public BacktestResponseDto toResponseDto(BacktestEntity entity) {
		BacktestResponseDto dto = new BacktestResponseDto(
			entity.getId(),
			entity.getMethod(),
			entity.getAsset(),
			entity.getCurrency(),
			entity.getDateFrom(),
			entity.getDateTo(),
			entity.getConfigHash(),
			entity.getConfig(),
			entity.getBacktest(),
			entity.getPerformance()
		);
		return dto;
	}
}

