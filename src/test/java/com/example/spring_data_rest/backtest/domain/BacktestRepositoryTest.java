package com.example.spring_data_rest.backtest.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.Objects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DataJpaTest
@TestPropertySource(properties = {
	"spring.jpa.hibernate.ddl-auto=none",
	"spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
	"spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
})
class BacktestRepositoryTest {

	@Autowired
	private BacktestRepository backtestRepository;

	private BacktestEntity createBacktestEntity(
		String method,
		String asset,
		String currency,
		Long dateFrom,
		Long dateTo,
		String configHash
	) {
		BacktestEntity entity = new BacktestEntity();
		entity.setMethod(method);
		entity.setAsset(asset);
		entity.setCurrency(currency);
		entity.setDateFrom(dateFrom);
		entity.setDateTo(dateTo);
		entity.setConfigHash(configHash);
		entity.setConfig("{\"config\":true}");
		entity.setBacktest("{\"backtest\":true}");
		entity.setPerformance("{\"performance\":true}");
		return entity;
	}

	@Test
	@DisplayName("findByMethod should return page with matching entities")
	void findByMethod_returnsMatchingPage() {
		BacktestEntity first = createBacktestEntity(
			"METHOD_A", "BTC", "USD", 1L, 10L, "hash-1"
		);
		BacktestEntity second = createBacktestEntity(
			"METHOD_A", "ETH", "USD", 11L, 20L, "hash-2"
		);
		BacktestEntity otherMethod = createBacktestEntity(
			"METHOD_B", "BTC", "USD", 21L, 30L, "hash-3"
		);

		backtestRepository.save(Objects.requireNonNull(first));
		backtestRepository.save(Objects.requireNonNull(second));
		backtestRepository.save(Objects.requireNonNull(otherMethod));

		Pageable pageable = PageRequest.of(0, 10);

		Page<BacktestEntity> page = backtestRepository.findByMethod("METHOD_A", pageable);

		assertThat(page.getTotalElements()).isEqualTo(2);
		assertThat(page.getContent())
			.extracting(BacktestEntity::getMethod)
			.containsOnly("METHOD_A");
	}

	@Test
	@DisplayName("findByMethodAndAssetAndCurrency should filter by all three fields")
	void findByMethodAndAssetAndCurrency_filtersByAllFields() {
		BacktestEntity match = createBacktestEntity(
			"METHOD_A", "BTC", "USD", 1L, 10L, "hash-1"
		);
		BacktestEntity differentAsset = createBacktestEntity(
			"METHOD_A", "ETH", "USD", 1L, 10L, "hash-2"
		);
		BacktestEntity differentCurrency = createBacktestEntity(
			"METHOD_A", "BTC", "EUR", 1L, 10L, "hash-3"
		);

		backtestRepository.save(Objects.requireNonNull(match));
		backtestRepository.save(Objects.requireNonNull(differentAsset));
		backtestRepository.save(Objects.requireNonNull(differentCurrency));

		Pageable pageable = PageRequest.of(0, 10);

		Page<BacktestEntity> page = backtestRepository.findByMethodAndAssetAndCurrency(
			"METHOD_A", "BTC", "USD", pageable
		);

		assertThat(page.getTotalElements()).isEqualTo(1);
		assertThat(page.getContent()).hasSize(1);
		BacktestEntity result = page.getContent().get(0);
		assertThat(result.getAsset()).isEqualTo("BTC");
		assertThat(result.getCurrency()).isEqualTo("USD");
	}

	@Test
	@DisplayName("findByMethodAndDateFromGreaterThanEqualAndDateToLessThanEqual should apply date range for a method")
	void findByMethodAndDateRange_filtersByMethodAndDates() {
		BacktestEntity insideRange = createBacktestEntity(
			"METHOD_A", "BTC", "USD", 5L, 15L, "hash-1"
		);
		BacktestEntity beforeRange = createBacktestEntity(
			"METHOD_A", "BTC", "USD", 1L, 4L, "hash-2"
		);
		BacktestEntity afterRange = createBacktestEntity(
			"METHOD_A", "BTC", "USD", 16L, 20L, "hash-3"
		);
		BacktestEntity differentMethod = createBacktestEntity(
			"METHOD_B", "BTC", "USD", 5L, 15L, "hash-4"
		);

		backtestRepository.save(Objects.requireNonNull(insideRange));
		backtestRepository.save(Objects.requireNonNull(beforeRange));
		backtestRepository.save(Objects.requireNonNull(afterRange));
		backtestRepository.save(Objects.requireNonNull(differentMethod));

		Pageable pageable = PageRequest.of(0, 10);

		Page<BacktestEntity> page = backtestRepository
			.findByMethodAndDateFromGreaterThanEqualAndDateToLessThanEqual(
				"METHOD_A",
				5L,
				15L,
				pageable
			);

		assertThat(page.getTotalElements()).isEqualTo(1);
		assertThat(page.getContent()).hasSize(1);
		BacktestEntity result = page.getContent().get(0);
		assertThat(result.getMethod()).isEqualTo("METHOD_A");
		assertThat(result.getDateFrom()).isGreaterThanOrEqualTo(5L);
		assertThat(result.getDateTo()).isLessThanOrEqualTo(15L);
	}

	@Test
	@DisplayName("findByMethodAndAssetAndCurrencyAndDateFromGreaterThanEqualAndDateToLessThanEqual should apply all filters")
	void findByMethodAssetCurrencyAndDateRange_filtersByAllFields() {
		BacktestEntity insideRange = createBacktestEntity(
			"METHOD_A", "BTC", "USD", 5L, 15L, "hash-1"
		);
		BacktestEntity differentAsset = createBacktestEntity(
			"METHOD_A", "ETH", "USD", 5L, 15L, "hash-2"
		);
		BacktestEntity differentCurrency = createBacktestEntity(
			"METHOD_A", "BTC", "EUR", 5L, 15L, "hash-3"
		);
		BacktestEntity beforeRange = createBacktestEntity(
			"METHOD_A", "BTC", "USD", 1L, 4L, "hash-4"
		);
		BacktestEntity afterRange = createBacktestEntity(
			"METHOD_A", "BTC", "USD", 16L, 20L, "hash-5"
		);

		backtestRepository.save(Objects.requireNonNull(insideRange));
		backtestRepository.save(Objects.requireNonNull(differentAsset));
		backtestRepository.save(Objects.requireNonNull(differentCurrency));
		backtestRepository.save(Objects.requireNonNull(beforeRange));
		backtestRepository.save(Objects.requireNonNull(afterRange));

		Pageable pageable = PageRequest.of(0, 10);

		Page<BacktestEntity> page = backtestRepository
			.findByMethodAndAssetAndCurrencyAndDateFromGreaterThanEqualAndDateToLessThanEqual(
				"METHOD_A",
				"BTC",
				"USD",
				5L,
				15L,
				pageable
			);

		assertThat(page.getTotalElements()).isEqualTo(1);
		assertThat(page.getContent()).hasSize(1);
		BacktestEntity result = page.getContent().get(0);
		assertThat(result.getMethod()).isEqualTo("METHOD_A");
		assertThat(result.getAsset()).isEqualTo("BTC");
		assertThat(result.getCurrency()).isEqualTo("USD");
		assertThat(result.getDateFrom()).isGreaterThanOrEqualTo(5L);
		assertThat(result.getDateTo()).isLessThanOrEqualTo(15L);
	}

	@Test
	@DisplayName("findByMethodOrAssetOrCurrencyOrDateFromOrDateToOrConfigHash should match by any field")
	void findByCompositeOrQuery_matchesByAnyField() {
		BacktestEntity target = createBacktestEntity(
			"METHOD_A", "BTC", "USD", 1L, 10L, "hash-1"
		);
		backtestRepository.save(Objects.requireNonNull(target));

		Optional<BacktestEntity> byMethod = backtestRepository
			.findByMethodOrAssetOrCurrencyOrDateFromOrDateToOrConfigHash(
				"METHOD_A", null, null, null, null, null
			);
		Optional<BacktestEntity> byAsset = backtestRepository
			.findByMethodOrAssetOrCurrencyOrDateFromOrDateToOrConfigHash(
				null, "BTC", null, null, null, null
			);
		Optional<BacktestEntity> byCurrency = backtestRepository
			.findByMethodOrAssetOrCurrencyOrDateFromOrDateToOrConfigHash(
				null, null, "USD", null, null, null
			);
		Optional<BacktestEntity> byDateFrom = backtestRepository
			.findByMethodOrAssetOrCurrencyOrDateFromOrDateToOrConfigHash(
				null, null, null, 1L, null, null
			);
		Optional<BacktestEntity> byDateTo = backtestRepository
			.findByMethodOrAssetOrCurrencyOrDateFromOrDateToOrConfigHash(
				null, null, null, null, 10L, null
			);
		Optional<BacktestEntity> byConfigHash = backtestRepository
			.findByMethodOrAssetOrCurrencyOrDateFromOrDateToOrConfigHash(
				null, null, null, null, null, "hash-1"
			);

		assertThat(byMethod).isPresent();
		assertThat(byAsset).isPresent();
		assertThat(byCurrency).isPresent();
		assertThat(byDateFrom).isPresent();
		assertThat(byDateTo).isPresent();
		assertThat(byConfigHash).isPresent();
	}
}
