package com.example.spring_data_rest.backtest.domain;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BacktestRepository extends JpaRepository<BacktestEntity, Long> {

	Optional<BacktestEntity> findByMethodAndAssetAndCurrencyAndDateFromAndDateToAndConfigHash(
		String method,
		String asset,
		String currency,
		Long dateFrom,
		Long dateTo,
		String configHash
	);

	Page<BacktestEntity> findByMethod(String method, Pageable pageable);

	Page<BacktestEntity> findByMethodAndAssetAndCurrency(String method, String asset, String currency, Pageable pageable);
}

