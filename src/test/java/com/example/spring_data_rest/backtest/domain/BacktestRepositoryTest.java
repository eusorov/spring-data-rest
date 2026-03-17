package com.example.spring_data_rest.backtest.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

@MybatisTest
@TestPropertySource(properties = {
	"spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
})
class BacktestRepositoryTest {

	@Autowired
	private BacktestRepository backtestRepository;
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	@DisplayName("findByMethod should return list with matching entities and correct count")
	void findByMethod_returnsMatchingListAndCount() {
		insertBacktest("METHOD_A", "BTC", "USD", 1L, 10L, "hash-1");
		insertBacktest("METHOD_A", "ETH", "USD", 11L, 20L, "hash-2");
		insertBacktest("METHOD_B", "BTC", "USD", 21L, 30L, "hash-3");

		List<BacktestEntity> result = backtestRepository.findByMethod("METHOD_A", 10, 0);
		long count = backtestRepository.countByMethod("METHOD_A");

		assertThat(count).isEqualTo(2);
		assertThat(result)
			.extracting(BacktestEntity::getMethod)
			.containsOnly("METHOD_A");
	}

	@Test
	@DisplayName("findByMethodAndAssetAndCurrency should filter by all three fields and count correctly")
	void findByMethodAndAssetAndCurrency_filtersByAllFields() {
		insertBacktest("METHOD_A", "BTC", "USD", 1L, 10L, "hash-1");
		insertBacktest("METHOD_A", "ETH", "USD", 1L, 10L, "hash-2");
		insertBacktest("METHOD_A", "BTC", "EUR", 1L, 10L, "hash-3");

		List<BacktestEntity> result = backtestRepository.findByMethodAndAssetAndCurrency(
			"METHOD_A",
			"BTC",
			"USD",
			10,
			0
		);
		long count = backtestRepository.countByMethodAndAssetAndCurrency(
			"METHOD_A",
			"BTC",
			"USD"
		);

		assertThat(count).isEqualTo(1);
		assertThat(result).hasSize(1);
		BacktestEntity entity = result.get(0);
		assertThat(entity.getAsset()).isEqualTo("BTC");
		assertThat(entity.getCurrency()).isEqualTo("USD");
	}

	@Test
	@DisplayName("findByMethodAndDateFromGreaterThanEqualAndDateToLessThanEqual should apply date range for a method")
	void findByMethodAndDateRange_filtersByMethodAndDates() {
		insertBacktest("METHOD_A", "BTC", "USD", 5L, 15L, "hash-1");
		insertBacktest("METHOD_A", "BTC", "USD", 1L, 4L, "hash-2");
		insertBacktest("METHOD_A", "BTC", "USD", 16L, 20L, "hash-3");
		insertBacktest("METHOD_B", "BTC", "USD", 5L, 15L, "hash-4");

		List<BacktestEntity> result = backtestRepository
			.findByMethodAndDateFromGreaterThanEqualAndDateToLessThanEqual(
				"METHOD_A",
				5L,
				15L,
				10,
				0
			);
		long count = backtestRepository
			.countByMethodAndDateFromGreaterThanEqualAndDateToLessThanEqual(
				"METHOD_A",
				5L,
				15L
			);

		assertThat(count).isEqualTo(1);
		assertThat(result).hasSize(1);
		BacktestEntity entity = result.get(0);
		assertThat(entity.getMethod()).isEqualTo("METHOD_A");
		assertThat(entity.getDateFrom()).isGreaterThanOrEqualTo(5L);
		assertThat(entity.getDateTo()).isLessThanOrEqualTo(15L);
	}

	@Test
	@DisplayName("findByMethodAndAssetAndCurrencyAndDateFromGreaterThanEqualAndDateToLessThanEqual should apply all filters")
	void findByMethodAssetCurrencyAndDateRange_filtersByAllFields() {
		insertBacktest("METHOD_A", "BTC", "USD", 5L, 15L, "hash-1");
		insertBacktest("METHOD_A", "ETH", "USD", 5L, 15L, "hash-2");
		insertBacktest("METHOD_A", "BTC", "EUR", 5L, 15L, "hash-3");
		insertBacktest("METHOD_A", "BTC", "USD", 1L, 4L, "hash-4");
		insertBacktest("METHOD_A", "BTC", "USD", 16L, 20L, "hash-5");

		List<BacktestEntity> result = backtestRepository
			.findByMethodAndAssetAndCurrencyAndDateFromGreaterThanEqualAndDateToLessThanEqual(
				"METHOD_A",
				"BTC",
				"USD",
				5L,
				15L,
				10,
				0
			);
		long count = backtestRepository
			.countByMethodAndAssetAndCurrencyAndDateFromGreaterThanEqualAndDateToLessThanEqual(
				"METHOD_A",
				"BTC",
				"USD",
				5L,
				15L
			);

		assertThat(count).isEqualTo(1);
		assertThat(result).hasSize(1);
		BacktestEntity entity = result.get(0);
		assertThat(entity.getMethod()).isEqualTo("METHOD_A");
		assertThat(entity.getAsset()).isEqualTo("BTC");
		assertThat(entity.getCurrency()).isEqualTo("USD");
		assertThat(entity.getDateFrom()).isGreaterThanOrEqualTo(5L);
		assertThat(entity.getDateTo()).isLessThanOrEqualTo(15L);
	}

	@Test
	@DisplayName("findByMethodOrAssetOrCurrencyOrDateFromOrDateToOrConfigHash should match by any field")
	void findByCompositeOrQuery_matchesByAnyField() {
		insertBacktest("METHOD_A", "BTC", "USD", 1L, 10L, "hash-1");

		Optional<BacktestEntity> byMethod = backtestRepository
			.findByMethodOrAssetOrCurrencyOrDateFromOrDateToOrConfigHash(
				"METHOD_A",
				null,
				null,
				null,
				null,
				null
			);
		Optional<BacktestEntity> byAsset = backtestRepository
			.findByMethodOrAssetOrCurrencyOrDateFromOrDateToOrConfigHash(
				null,
				"BTC",
				null,
				null,
				null,
				null
			);
		Optional<BacktestEntity> byCurrency = backtestRepository
			.findByMethodOrAssetOrCurrencyOrDateFromOrDateToOrConfigHash(
				null,
				null,
				"USD",
				null,
				null,
				null
			);
		Optional<BacktestEntity> byDateFrom = backtestRepository
			.findByMethodOrAssetOrCurrencyOrDateFromOrDateToOrConfigHash(
				null,
				null,
				null,
				1L,
				null,
				null
			);
		Optional<BacktestEntity> byDateTo = backtestRepository
			.findByMethodOrAssetOrCurrencyOrDateFromOrDateToOrConfigHash(
				null,
				null,
				null,
				null,
				10L,
				null
			);
		Optional<BacktestEntity> byConfigHash = backtestRepository
			.findByMethodOrAssetOrCurrencyOrDateFromOrDateToOrConfigHash(
				null,
				null,
				null,
				null,
				null,
				"hash-1"
			);

		assertThat(byMethod).isPresent();
		assertThat(byAsset).isPresent();
		assertThat(byCurrency).isPresent();
		assertThat(byDateFrom).isPresent();
		assertThat(byDateTo).isPresent();
		assertThat(byConfigHash).isPresent();
	}

	private void insertBacktest(
		String method,
		String asset,
		String currency,
		Long dateFrom,
		Long dateTo,
		String configHash
	) {
		jdbcTemplate.update(
			"""
				INSERT INTO backtest (
					method,
					asset,
					currency,
					datefrom,
					dateto,
					confighash,
					config,
					backtest,
					performance
				) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
				""",
			method,
			asset,
			currency,
			dateFrom,
			dateTo,
			configHash,
			"{\"config\":true}",
			"{\"backtest\":true}",
			"{\"performance\":true}"
		);
	}
}
