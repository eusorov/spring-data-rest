package com.example.spring_data_rest.backtest.api;

import java.util.Objects;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.spring_data_rest.backtest.api.dto.BacktestListItemDto;
import com.example.spring_data_rest.backtest.api.dto.BacktestResponseDto;
import com.example.spring_data_rest.backtest.service.BacktestService;

@RestController
@RequestMapping("/api/v1/backtests")
public class BacktestController {

	private static final int MAX_PAGE_SIZE = 100;

	private final BacktestService backtestService;

	public BacktestController(BacktestService backtestService) {
		this.backtestService = backtestService;
	}

	@GetMapping
	public Page<BacktestListItemDto> listBacktests(
		@RequestParam(value = "page", defaultValue = "0") int page,
		@RequestParam(value = "size", defaultValue = "20") int size,
		@RequestParam(value = "sort", defaultValue = "id,desc") String sort,
		@RequestParam(value = "method", required = false) String method,
		@RequestParam(value = "asset", required = false) String asset,
		@RequestParam(value = "currency", required = false) String currency,
		@RequestParam(value = "dateFrom", required = false) Long dateFrom,
		@RequestParam(value = "dateTo", required = false) Long dateTo
	) {
		if (size > MAX_PAGE_SIZE) {
			size = MAX_PAGE_SIZE;
		}

		String[] sortParts = sort.split(",");
		Sort sortSpec;
		if (sortParts.length == 2 && !sortParts[0].isEmpty() && !sortParts[1].isEmpty()) {
			Sort.Direction direction = Sort.Direction.fromString(Objects.requireNonNull(sortParts[1]));
			sortSpec = Sort.by(direction, sortParts[0]);
		}
		else {
			sortSpec = Sort.by(Sort.Direction.DESC, "id");
		}

		Pageable pageable = PageRequest.of(page, size, sortSpec);
		return backtestService.listBacktests(method, asset, currency, dateFrom, dateTo, pageable);
	}

	@GetMapping("/{id}")
	public ResponseEntity<BacktestResponseDto> getById(@PathVariable("id") Long id) {
		BacktestResponseDto dto = backtestService.getById(id);
		return ResponseEntity.ok(dto);
	}

	@GetMapping("/by-key")
	public ResponseEntity<BacktestResponseDto> getByAnyKey(
		@RequestParam("method") String method,
		@RequestParam("asset") String asset,
		@RequestParam("currency") String currency,
		@RequestParam("dateFrom") Long dateFrom,
		@RequestParam("dateTo") Long dateTo,
		@RequestParam("configHash") String configHash
	) {
		BacktestResponseDto dto = backtestService.getByAnyKey(
			Optional.of(method),
			Optional.of(asset),
			Optional.of(currency),
			Optional.of(dateFrom),
			Optional.of(dateTo),
			Optional.of(configHash)
		);
		return ResponseEntity.ok(dto);
	}
}
