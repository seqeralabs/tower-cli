package io.nativecli;

import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.client.annotation.Client;
import io.seqera.tower.exchange.ErrorResponse;
import io.seqera.tower.exchange.serviceinfo.ServiceInfoResponse;

/**
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */

@Header(name = "User-Agent", value = "Tower Client")
@Client(value = "https://api.tower.nf", errorType = ErrorResponse.class)
public interface TowerApi {

    @Get("/service-info")
    ServiceInfoResponse getServiceInfo();

}
