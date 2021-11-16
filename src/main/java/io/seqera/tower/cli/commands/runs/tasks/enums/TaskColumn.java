/*
 * Copyright (c) 2021, Seqera Labs.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */

package io.seqera.tower.cli.commands.runs.tasks.enums;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

import io.seqera.tower.model.Task;

public enum TaskColumn {
    taskId("ID", true, Task::getTaskId),
    status("Status", false, Task::getStatus),
    submit("Submit", true, Task::getSubmit, t -> ((OffsetDateTime) t).format(DateTimeFormatter.RFC_1123_DATE_TIME)),
    container("Container", true, Task::getContainer),
    nativeId("Native ID", true, Task::getNativeId),
    process("Process", true, Task::getProcess),
    tag("Tag", false, Task::getTag),
    hash("Hash", false, Task::getHash),
    exit("Exit", false, Task::getExit);

    private final String description;
    private final boolean fixed;
    private final Function<Task, Object> objectFunction;
    private final Function<Object, String> prettyprint;

    TaskColumn(String description, boolean fixed, Function<Task, Object> objectFunction, Function<Object, String> prettyprint) {
        this.description = description;
        this.fixed = fixed;
        this.objectFunction = objectFunction;
        this.prettyprint = prettyprint;
    }

    TaskColumn(String description, boolean fixed, Function<Task, Object> objectFunction) {
        this.description = description;
        this.fixed = fixed;
        this.objectFunction = objectFunction;
        prettyprint = Object::toString;
    }

    public String getDescription() {
        return description;
    }

    public boolean isFixed() {
        return fixed;
    }

    public Function<Object, String> getPrettyPrint() {
        return prettyprint;
    }

    public Function<Task, Object> getObject() {
        return objectFunction;
    }
}
