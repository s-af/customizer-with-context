package com.customizer;

import io.micronaut.runtime.http.scope.RequestScope;
import lombok.Getter;
import lombok.Setter;

@RequestScope
@Getter
@Setter
public class RequestContext {

    private Boolean magicUser = false;

}
