package io.seqera.tower.cli.commands.data.links;

import io.seqera.tower.cli.commands.AbstractApiCmd;

public class AbstractDataLinksCmd extends AbstractApiCmd {

    public static String buildSearch(String name, String providers, String region, String uri) {
        StringBuilder builder = new StringBuilder();
        if (name != null && !name.isBlank()) {
            builder.append(name);
        }
        if (providers != null && !providers.isBlank()) {
            appendParameter(builder, providers, "provider");
        }
        if (region != null && !region.isBlank()) {
            appendParameter(builder, region, "region");
        }
        if (uri != null && !uri.isBlank()) {
            appendParameter(builder, uri, "resourceRef");
        }
        return builder.toString();
    }

    private static void appendParameter(StringBuilder input, String param, String paramName) {
        if (param != null && !param.isBlank()) {
            if (input.length() > 0) {
                input.append(" ");
            }
            input.append(paramName).append(":").append(param);
        }
    }
}
