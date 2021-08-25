package io.example;

import org.apache.camel.Exchange;
import org.apache.camel.Expression;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class CsvToMap implements Expression {

	@Override
	public <T> T evaluate(Exchange exchange, Class<T> type) {
		String body = exchange.getIn().getBody(String.class);
		final Map<String, Object> collect = Arrays.stream(body.split(",")).map(s -> s.split(":")).filter(s -> s.length > 1).collect(Collectors.toMap(s -> s[0], this::getNonNullString));
		collect.put("FILE_NAME", exchange.getIn().getHeader("CamelFileName", String.class));
		collect.put("ROW_NUMBER", exchange.getProperty(Exchange.SPLIT_INDEX, Long.class));
		return (T) collect;
	}

	private Object getNonNullString(String[] s) {
		return "NULL".equalsIgnoreCase(s[1]) ? "" : s[1];
	}
}
