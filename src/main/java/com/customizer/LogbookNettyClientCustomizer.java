package com.customizer;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.http.client.netty.NettyClientCustomizer;
import io.micronaut.http.netty.channel.ChannelPipelineCustomizer;
import io.netty.channel.Channel;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.netty.LogbookClientHandler;

@Slf4j
@Singleton
public class LogbookNettyClientCustomizer implements BeanCreatedEventListener<NettyClientCustomizer.Registry> {

    @Inject
    private RequestContext requestContext;

    @Override
    public NettyClientCustomizer.Registry onCreated(
            BeanCreatedEvent<NettyClientCustomizer.Registry> event) {

        NettyClientCustomizer.Registry registry = event.getBean();
        registry.register(new Customizer(null));
        return registry;
    }

    private class Customizer implements NettyClientCustomizer {
        private final Channel channel;

        Customizer(Channel channel) {
            this.channel = channel;
        }

        @Override
        public NettyClientCustomizer specializeForChannel(Channel channel, ChannelRole role) {
            return new Customizer(channel);
        }

        @Override
        public void onRequestPipelineBuilt() {
            channel.pipeline().addBefore(
                    ChannelPipelineCustomizer.HANDLER_MICRONAUT_HTTP_RESPONSE,
                    "logbook",
                    new LogbookClientHandler(
                            Logbook.builder()
                                    .condition(httpRequest -> {
                                        try {
                                            return requestContext.getMagicUser();
                                        } catch (Exception ex) {
                                            log.error("LogbookClientHandler error: {}", ex.getMessage(), ex);
                                            return true;
                                        }
                                    })
                                    .build()
                    )
            );
        }
    }
}