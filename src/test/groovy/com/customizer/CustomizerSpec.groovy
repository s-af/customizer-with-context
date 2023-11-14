package com.customizer

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.Appender
import ch.qos.logback.core.AppenderBase
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import org.slf4j.LoggerFactory
import org.testcontainers.spock.Testcontainers
import org.wiremock.integrations.testcontainers.WireMockContainer
import spock.lang.Specification
import jakarta.inject.Inject

@MicronautTest
@Testcontainers
class CustomizerSpec extends Specification implements TestPropertyProvider {

    @Inject
    EmbeddedServer server

    static WireMockContainer wiremockServer = new WireMockContainer(WireMockContainer.WIREMOCK_2_LATEST)
            .withMappingFromResource("wiremock/mocks-config.json")

    static {
        wiremockServer.start()
    }

    @Override
    Map<String, String> getProperties() {
        return Map.of(
                "micronaut.http.services.wiremock.url", wiremockServer.getBaseUrl(),
        )
    }

    private class LogbackAppender extends AppenderBase<ILoggingEvent> {

        List<String> logMessages = new ArrayList<>()

        @Override
        protected void append(ILoggingEvent iLoggingEvent) {
            logMessages.add(iLoggingEvent.getFormattedMessage())
        }
    }

    void 'Test client customizer work with RequestScope'() {
        setup:
        LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory()
        Logger logger = (Logger) LoggerFactory.getLogger("com.customizer")
        logger.setLevel(Level.ERROR)
        Appender appender = new LogbackAppender()
        appender.setContext(ctx)
        appender.start()
        logger.addAppender(appender)

        when:
        def process = Runtime.getRuntime().exec("curl " + server.getURL() + RestController.PATH)
        process.waitFor()
        def response = new String(process.inputStream.readAllBytes());

        then:
        response == "OK"
        appender.logMessages.size() == 0
    }

}
