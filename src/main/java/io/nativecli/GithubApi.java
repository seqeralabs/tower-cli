package io.nativecli;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;

/**
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Singleton
public class GithubApi {

    @Client("https://api.github.com")
    @Inject
    RxHttpClient httpClient;

    String GET(String endpoint) {
        Map<CharSequence,CharSequence> headers = new HashMap<>();
        headers.put("User-Agent", "Mozilla");

        HttpRequest req = HttpRequest.GET(endpoint) .headers(headers);
        String result = httpClient.toBlocking().retrieve(req);
        return result;
    }
}
