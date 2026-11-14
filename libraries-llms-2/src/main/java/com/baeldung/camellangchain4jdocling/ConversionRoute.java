package com.baeldung.camellangchain4jdocling;

import java.util.Objects;
import java.util.function.Consumer;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;

class ConversionRoute extends RouteBuilder {

    private final Consumer<Throwable> fatalErrorHandler;

    ConversionRoute(Consumer<Throwable> fatalErrorHandler) {
        this.fatalErrorHandler = Objects.requireNonNull(fatalErrorHandler, "fatalErrorHandler");
    }

    @Override
    public void configure() {
        onException(Exception.class)
            .maximumRedeliveries(0)
            .handled(true)
            .process(exchange -> {
                Throwable exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);
                if (exception == null) {
                    throw new IllegalStateException("Camel did not provide the document processing exception");
                }
                fatalErrorHandler.accept(exception);
            });

        from("file:documents?include=.*\\.(pdf|docx|pptx|html|md)&noop=true&idempotent=true").routeId("document-analysis-workflow")
            .log("Processing document: ${header.CamelFileName}")
            .to("docling:CONVERT_TO_MARKDOWN?useDoclingServe=true&doclingServeUrl=http://localhost:5001&contentInBody=true")
            .setProperty("convertedMarkdown", body())
            .setHeader(Exchange.FILE_NAME, simple("${header.CamelFileName.replaceFirst('\\.[^.]+$', '')}.md"))
            .to("file:output")
            .setBody(simple("""
                You are a helpful document analysis assistant. Please analyze
                the following document and provide:
                1. A brief summary (2-3 sentences)
                2. Key topics and main points
                3. Any important findings or conclusions
                
                Document content:
                ${exchangeProperty.convertedMarkdown}
                """))
            .to("langchain4j-chat:analysis?chatModel=#chatModel")
            .setHeader(Exchange.FILE_NAME, simple("${header.CamelFileName.replaceFirst('\\.[^.]+$', '')}-analysis.md"))
            .to("file:analysis");
    }
}
