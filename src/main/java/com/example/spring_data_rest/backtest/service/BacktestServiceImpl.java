package com.example.spring_data_rest.backtest.service;

import java.util.Objects;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
		boolean hasMethod = method != null;
		boolean hasAsset = asset != null;
		boolean hasCurrency = currency != null;
		boolean hasDateFrom = dateFrom != null;
		boolean hasDateTo = dateTo != null;

		long limit = Objects.requireNonNull(pageable).getPageSize();
		long offset = pageable.getOffset();

		if (!hasMethod && !hasAsset && !hasCurrency && !hasDateFrom && !hasDateTo) {
			var content = backtestRepository.findAll(limit, offset);
			long total = backtestRepository.countAll();
			page = new PageImpl<>(content, pageable, total);
		}
		else if (hasMethod && !hasAsset && !hasCurrency && hasDateFrom && hasDateTo) {
			var content = backtestRepository
				.findByMethodAndDateFromGreaterThanEqualAndDateToLessThanEqual(
					method,
					dateFrom,
					dateTo,
					limit,
					offset
				);
			long total = backtestRepository
				.countByMethodAndDateFromGreaterThanEqualAndDateToLessThanEqual(
					method,
					dateFrom,
					dateTo
				);
			page = new PageImpl<>(content, pageable, total);
		}
		else if (hasMethod && !hasAsset && !hasCurrency) {
			var content = backtestRepository.findByMethod(method, limit, offset);
			long total = backtestRepository.countByMethod(method);
			page = new PageImpl<>(content, pageable, total);
		}
		else if (hasMethod && hasAsset && hasCurrency && hasDateFrom && hasDateTo) {
			var content = backtestRepository
				.findByMethodAndAssetAndCurrencyAndDateFromGreaterThanEqualAndDateToLessThanEqual(
					method,
					asset,
					currency,
					dateFrom,
					dateTo,
					limit,
					offset
				);
			long total = backtestRepository
				.countByMethodAndAssetAndCurrencyAndDateFromGreaterThanEqualAndDateToLessThanEqual(
					method,
					asset,
					currency,
					dateFrom,
					dateTo
				);
			page = new PageImpl<>(content, pageable, total);
		}
		else if (hasMethod && hasAsset && hasCurrency) {
			var content = backtestRepository.findByMethodAndAssetAndCurrency(
				method,
				asset,
				currency,
				limit,
				offset
			);
			long total = backtestRepository.countByMethodAndAssetAndCurrency(
				method,
				asset,
				currency
			);
			page = new PageImpl<>(content, pageable, total);
		}
		else {
			var content = backtestRepository.findAll(limit, offset);
			long total = backtestRepository.countAll();
			page = new PageImpl<>(content, pageable, total);
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
