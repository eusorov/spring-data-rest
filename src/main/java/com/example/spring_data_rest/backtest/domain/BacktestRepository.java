package com.example.spring_data_rest.backtest.domain;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BacktestRepository {

	@Select("""
		SELECT
			id,
			method,
			asset,
			currency,
			datefrom AS dateFrom,
			dateto AS dateTo,
			confighash AS configHash,
			config,
			backtest,
			performance
		FROM backtest
		WHERE id = #{id}
		""")
	Optional<BacktestEntity> findById(@Param("id") Long id);

	@Select("""
		SELECT
			id,
			method,
			asset,
			currency,
			datefrom AS dateFrom,
			dateto AS dateTo,
			confighash AS configHash,
			config,
			backtest,
			performance
		FROM backtest
		WHERE method = #{method}
			OR asset = #{asset}
			OR currency = #{currency}
			OR datefrom = #{dateFrom}
			OR dateto = #{dateTo}
			OR confighash = #{configHash}
		LIMIT 1
		""")
	Optional<BacktestEntity> findByMethodOrAssetOrCurrencyOrDateFromOrDateToOrConfigHash(
		@Param("method") String method,
		@Param("asset") String asset,
		@Param("currency") String currency,
		@Param("dateFrom") Long dateFrom,
		@Param("dateTo") Long dateTo,
		@Param("configHash") String configHash
	);

	@Select("""
		SELECT
			id,
			method,
			asset,
			currency,
			datefrom AS dateFrom,
			dateto AS dateTo,
			confighash AS configHash,
			config,
			backtest,
			performance
		FROM backtest
		ORDER BY id
		LIMIT #{limit} OFFSET #{offset}
		""")
	List<BacktestEntity> findAll(@Param("limit") long limit, @Param("offset") long offset);

	@Select("SELECT COUNT(*) FROM backtest")
	long countAll();

	@Select("""
		SELECT
			id,
			method,
			asset,
			currency,
			datefrom AS dateFrom,
			dateto AS dateTo,
			confighash AS configHash,
			config,
			backtest,
			performance
		FROM backtest
		WHERE method = #{method}
		ORDER BY id
		LIMIT #{limit} OFFSET #{offset}
		""")
	List<BacktestEntity> findByMethod(
		@Param("method") String method,
		@Param("limit") long limit,
		@Param("offset") long offset
	);

	@Select("""
		SELECT COUNT(*)
		FROM backtest
		WHERE method = #{method}
		""")
	long countByMethod(@Param("method") String method);

	@Select("""
		SELECT
			id,
			method,
			asset,
			currency,
			datefrom AS dateFrom,
			dateto AS dateTo,
			confighash AS configHash,
			config,
			backtest,
			performance
		FROM backtest
		WHERE method = #{method}
			AND datefrom >= #{dateFrom}
			AND dateto <= #{dateTo}
		ORDER BY id
		LIMIT #{limit} OFFSET #{offset}
		""")
	List<BacktestEntity> findByMethodAndDateFromGreaterThanEqualAndDateToLessThanEqual(
		@Param("method") String method,
		@Param("dateFrom") Long dateFrom,
		@Param("dateTo") Long dateTo,
		@Param("limit") long limit,
		@Param("offset") long offset
	);

	@Select("""
		SELECT COUNT(*)
		FROM backtest
		WHERE method = #{method}
			AND datefrom >= #{dateFrom}
			AND dateto <= #{dateTo}
		""")
	long countByMethodAndDateFromGreaterThanEqualAndDateToLessThanEqual(
		@Param("method") String method,
		@Param("dateFrom") Long dateFrom,
		@Param("dateTo") Long dateTo
	);

	@Select("""
		SELECT
			id,
			method,
			asset,
			currency,
			datefrom AS dateFrom,
			dateto AS dateTo,
			confighash AS configHash,
			config,
			backtest,
			performance
		FROM backtest
		WHERE method = #{method}
			AND asset = #{asset}
			AND currency = #{currency}
		ORDER BY id
		LIMIT #{limit} OFFSET #{offset}
		""")
	List<BacktestEntity> findByMethodAndAssetAndCurrency(
		@Param("method") String method,
		@Param("asset") String asset,
		@Param("currency") String currency,
		@Param("limit") long limit,
		@Param("offset") long offset
	);

	@Select("""
		SELECT COUNT(*)
		FROM backtest
		WHERE method = #{method}
			AND asset = #{asset}
			AND currency = #{currency}
		""")
	long countByMethodAndAssetAndCurrency(
		@Param("method") String method,
		@Param("asset") String asset,
		@Param("currency") String currency
	);

	@Select("""
		SELECT
			id,
			method,
			asset,
			currency,
			datefrom AS dateFrom,
			dateto AS dateTo,
			confighash AS configHash,
			config,
			backtest,
			performance
		FROM backtest
		WHERE method = #{method}
			AND asset = #{asset}
			AND currency = #{currency}
			AND datefrom >= #{dateFrom}
			AND dateto <= #{dateTo}
		ORDER BY id
		LIMIT #{limit} OFFSET #{offset}
		""")
	List<BacktestEntity> findByMethodAndAssetAndCurrencyAndDateFromGreaterThanEqualAndDateToLessThanEqual(
		@Param("method") String method,
		@Param("asset") String asset,
		@Param("currency") String currency,
		@Param("dateFrom") Long dateFrom,
		@Param("dateTo") Long dateTo,
		@Param("limit") long limit,
		@Param("offset") long offset
	);

	@Select("""
		SELECT COUNT(*)
		FROM backtest
		WHERE method = #{method}
			AND asset = #{asset}
			AND currency = #{currency}
			AND datefrom >= #{dateFrom}
			AND dateto <= #{dateTo}
		""")
	long countByMethodAndAssetAndCurrencyAndDateFromGreaterThanEqualAndDateToLessThanEqual(
		@Param("method") String method,
		@Param("asset") String asset,
		@Param("currency") String currency,
		@Param("dateFrom") Long dateFrom,
		@Param("dateTo") Long dateTo
	);
}

