/*
 * Copyright 2023, Seqera.
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
 *
 */

package io.seqera.tower.cli.exceptions;

import picocli.CommandLine;

public class ShowUsageException extends TowerException {

    private CommandLine.Model.CommandSpec specs;

    public ShowUsageException(CommandLine.Model.CommandSpec specs){
        this.specs = specs;
    }

    public ShowUsageException(CommandLine.Model.CommandSpec specs,String message){
        super(message);
        this.specs = specs;
    }

    public CommandLine.Model.CommandSpec getSpecs() {
        return specs;
    }
}
