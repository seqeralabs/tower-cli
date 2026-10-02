/*
 * Copyright 2021-2026, Seqera.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.seqera.tower.cli.exceptions;

/**
 * The resource was created or submitted, but polling its status kept failing, so its outcome is unknown.
 */
public class StatusCheckFailedException extends TowerRuntimeException {

    // Distinct from 1 so automation does not mistake "status unknown" for "failed" and retry, creating duplicates
    public static final int EXIT_CODE = 3;

    private final String reason;

    public StatusCheckFailedException(String reason, Throwable cause) {
        super(String.format("Status could not be checked (%s)", reason), cause);
        this.reason = reason;
    }

    public StatusCheckFailedException(String message, StatusCheckFailedException failure) {
        super(message, failure.getCause());
        this.reason = failure.reason;
    }

    public String getReason() {
        return reason;
    }

}
