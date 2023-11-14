package com.customizer;

import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;

@Slf4j
@Controller("/")
@Produces(MediaType.TEXT_PLAIN)
public class RestController {

    @Inject
    private RequestContext requestContext;

    public static final String PATH = "/controller";

    @Inject
    @Client(id = "wiremock")
    private HttpClient httpClient;

    @Get(uri = PATH)
    @SingleResult
    Publisher<String> fetch() {
        requestContext.setMagicUser(true);
        return httpClient.retrieve("/wiremock");
    }
}
