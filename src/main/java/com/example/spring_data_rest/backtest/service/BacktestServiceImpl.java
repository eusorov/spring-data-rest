package com.example.spring_data_rest.backtest.service;

import java.util.Objects;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.spring_data_rest.backtest.api.BacktestMapper;
import com.example.spring_data_rest.backtest.api.dto.BacktestListItemDto;
import com.example.spring_data_rest.backtest.api.dto.BacktestResponseDto;
import com.example.spring_data_rest.backtest.domain.BacktestEntity;
import com.example.spring_data_rest.backtest.domain.BacktestRepository;
import com.example.spring_data_rest.backtest.exception.BacktestNotFoundException;

@Service
public class BacktestServiceImpl implements BacktestService {

	private final BacktestRepository backtestRepository;
	private final BacktestMapper backtestMapper;

	public BacktestServiceImpl(BacktestRepository backtestRepository) {
		this.backtestRepository = backtestRepository;
		this.backtestMapper = new BacktestMapper();
	}

	@Override
	public Page<BacktestListItemDto> listBacktests(
		String method,
		String asset,
		String currency,
		Long dateFrom,
		Long dateTo,
		Pageable pageable
	) {
		Page<BacktestEntity> page;
		if (method == null && asset == null && currency == null) {
			page = backtestRepository.findAll(Objects.requireNonNull(pageable));
		}
		else if (method != null && asset == null && currency == null) {
			page = backtestRepository.findByMethod(method, Objects.requireNonNull(pageable));
		}
		else if (method != null && asset != null && currency != null) {
			page = backtestRepository.findByMethodAndAssetAndCurrency(method, asset, currency, Objects.requireNonNull(pageable));
		}
		else {
			page = backtestRepository.findAll(Objects.requireNonNull(pageable));
		}

		return page.map(backtestMapper::toListItemDto);
	}

	@Override
	public BacktestResponseDto getById(Long id) {
		BacktestEntity entity = backtestRepository.findById(Objects.requireNonNull(id))
			.orElseThrow(() -> new BacktestNotFoundException("Backtest not found for id " + id));
		return backtestMapper.toResponseDto(entity);
	}

	@Override
	public BacktestResponseDto getByAnyKey(
		Optional<String> method,
		Optional<String> asset,
		Optional<String> currency,
		Optional<Long> dateFrom,
		Optional<Long> dateTo,
		Optional<String> configHash
	) {
		BacktestEntity entity = backtestRepository
			.findByMethodOrAssetOrCurrencyOrDateFromOrDateToOrConfigHash(
				method.orElse(null),
				asset.orElse(null),
				currency.orElse(null),
				dateFrom.orElse(null),
				dateTo.orElse(null),
				configHash.orElse(null)
			)
			.orElseThrow(() -> new BacktestNotFoundException("Backtest not found for provided key"));
		return backtestMapper.toResponseDto(entity);
	}
}
