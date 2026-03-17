package com.example.spring_data_rest.backtest.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
	name = "backtest",
	uniqueConstraints = @UniqueConstraint(
		name = "uk_backtest_method_asset_currency_date_confighash",
		columnNames = { "method", "asset", "currency", "datefrom", "dateto", "confighash" }
	)
)
public class BacktestEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 20)
	private String method;

	@Column(nullable = false, length = 5)
	private String asset;

	@Column(nullable = false, length = 5)
	private String currency;

	@Column(name = "datefrom", nullable = false)
	private Long dateFrom;

	@Column(name = "dateto", nullable = false)
	private Long dateTo;

	@Column(name = "confighash", nullable = false, length = 255)
	private String configHash;

	@Column(columnDefinition = "json", nullable = false)
	private String config;

	@Column(columnDefinition = "json", nullable = false)
	private String backtest;

	@Column(columnDefinition = "json", nullable = false)
	private String performance;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getAsset() {
		return asset;
	}

	public void setAsset(String asset) {
		this.asset = asset;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public Long getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(Long dateFrom) {
		this.dateFrom = dateFrom;
	}

	public Long getDateTo() {
		return dateTo;
	}

	public void setDateTo(Long dateTo) {
		this.dateTo = dateTo;
	}

	public String getConfigHash() {
		return configHash;
	}

	public void setConfigHash(String configHash) {
		this.configHash = configHash;
	}

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

	public String getBacktest() {
		return backtest;
	}

	public void setBacktest(String backtest) {
		this.backtest = backtest;
	}

	public String getPerformance() {
		return performance;
	}

	public void setPerformance(String performance) {
		this.performance = performance;
	}
}

