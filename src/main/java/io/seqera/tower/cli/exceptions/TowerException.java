package io.seqera.tower.cli.exceptions;

import io.seqera.tower.ApiException;

public class TowerException extends ApiException {

    public TowerException() {
        super();
    }

    public TowerException(String message) {
        super(message);
    }
}
