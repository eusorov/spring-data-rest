package com.example.spring_data_rest.backtest.exception;

public class BacktestNotFoundException extends RuntimeException {

	public BacktestNotFoundException(String message) {
		super(message);
	}
}

