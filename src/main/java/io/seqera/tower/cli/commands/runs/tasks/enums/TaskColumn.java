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

import io.seqera.tower.cli.utils.FormatHelper;
import io.seqera.tower.model.Task;

import java.util.function.Function;

public enum TaskColumn {
    taskId("task_id", true, Task::getTaskId),
    process("process", true, Task::getProcess),
    tag("tag", true, Task::getTag),
    status("status", true, Task::getStatus),
    hash("hash", false, Task::getHash),
    exit("exit", false, Task::getExit),
    container("container", false, Task::getContainer),
    nativeId("native_id", false, Task::getNativeId),
    submit("submit", false, Task::getSubmit, compose(Task::getSubmit, FormatHelper::formatDate)),
    duration("duration", false, Task::getDuration, compose(Task::getDuration, FormatHelper::formatDurationMillis)),
    realtime("realtime", false, Task::getRealtime, compose(Task::getRealtime, FormatHelper::formatDurationMillis)),
    pcpu("pcpu", false, Task::getPcpu, compose(Task::getPcpu, FormatHelper::formatPercentage)),
    pmem("pmem", false, Task::getPmem, compose(Task::getPmem, FormatHelper::formatPercentage)),
    peakRss("peakRss", false, Task::getPeakRss, compose(Task::getPeakRss, FormatHelper::formatBits)),
    peakVmem("peakVmem", false, Task::getPeakVmem, compose(Task::getPeakVmem, FormatHelper::formatBits)),
    rchar("rchar", false, Task::getRchar, compose(Task::getRchar, FormatHelper::formatBits)),
    wchar("wchar", false, Task::getWchar, compose(Task::getWchar, FormatHelper::formatDurationMillis)),
    volCtxt("volCtxt", false, Task::getVolCtxt),
    invCtxt("invCtxt", false, Task::getInvCtxt);

    private final String description;
    private final boolean fixed;
    private final Function<Task, Object> objectFunction;
    private final Function<Task, String> prettyprint;

    TaskColumn(String description, boolean fixed, Function<Task, Object> objectFunction, Function<Task, String> prettyprint) {
        this.description = description;
        this.fixed = fixed;
        this.objectFunction = objectFunction;
        this.prettyprint = prettyprint;
    }

    TaskColumn(String description, boolean fixed, Function<Task, Object> objectFunction) {
        this.description = description;
        this.fixed = fixed;
        this.objectFunction = objectFunction;
        prettyprint = objectFunction.andThen(Object::toString);
    }

    public String getDescription() {
        return description;
    }

    public boolean isFixed() {
        return fixed;
    }

    public Function<Task, String> getPrettyPrint() {
        return prettyprint;
    }

    public Function<Task, Object> getObject() {
        return objectFunction;
    }

    private static <A, B, C> Function<A, C> compose(Function<A, B> f1, Function<B, C> f2) {
        return f1.andThen(f2);
    }
}
