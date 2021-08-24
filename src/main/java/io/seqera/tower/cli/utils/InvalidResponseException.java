package io.seqera.tower.cli.utils;

import java.io.IOException;

public class InvalidResponseException extends IOException {

    public InvalidResponseException(String message) {
        super(message);
    }

}
