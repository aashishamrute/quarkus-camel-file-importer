package io.example;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.support.processor.idempotent.FileIdempotentRepository;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import java.io.File;
import java.util.Map;

@ApplicationScoped
public class Routes extends RouteBuilder {

	@ConfigProperty(name = "file.trace.location")
	String fileTraceLocation;

	@ConfigProperty(name = "sql.insert")
	String sql;

	@Override
	public void configure() {

		from("file:{{gz.location}}?noop=true&include=.*.gz&delay=1000")
				.idempotentConsumer(header("CamelFileName"),	FileIdempotentRepository.fileIdempotentRepository(new File(fileTraceLocation)))
				.log("Reading log file ${header.CamelFileName} ")
				.unmarshal().gzipDeflater()
				.log("Unzip file ${header.CamelFileName}")
				.split(bodyAs(String.class).tokenize("\n"))
				.transform(new CsvToMap())
				.filter(exchange -> exchange.getIn().getBody(Map.class).containsKey("TT"))
				.log("Data to Insert ${header.CamelFileName} - ${header.CamelSplitIndex} - ${body}")
				.to("sql:" + sql);
	}
}