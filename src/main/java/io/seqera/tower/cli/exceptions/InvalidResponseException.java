package io.seqera.tower.cli.exceptions;

import java.io.IOException;

public class InvalidResponseException extends TowerException {

    public InvalidResponseException(String message) {
        super(message);
    }

}
