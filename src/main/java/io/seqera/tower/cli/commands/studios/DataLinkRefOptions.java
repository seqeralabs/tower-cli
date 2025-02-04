/*
 * Copyright 2021-2023, Seqera.
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

package io.seqera.tower.cli.commands.studios;

import java.util.List;

import io.seqera.tower.cli.exceptions.TowerRuntimeException;
import picocli.CommandLine;

public class DataLinkRefOptions {

    // TODO: The use of the validate option is a work around to an existing Picocli issue. Please refer to https://github.com/seqeralabs/tower-cli/pull/72#issuecomment-952588876
    @CommandLine.ArgGroup(validate = false, heading = "Option to mount data by passing in one of the below options:\n")
    public DataLinkRef dataLinkRef;

    public static class DataLinkRef {
        @CommandLine.Option(names = {"--mount-data"}, description = "Optional configuration override for 'mountData' setting (comma separate list of data-link names).", split = ",")
        private List<String> mountDataNames;

        @CommandLine.Option(names = {"--mount-data-ids"}, description = "Optional configuration override for 'mountData' setting (comma separate list of data-link Ids).", split = ",")
        private List<String> mountDataIds;

        @CommandLine.Option(names = {"--mount-data-resource-refs"}, description = "Optional configuration override for 'mountData' setting (comma separate list of data-link resource refs).", split = ",")
        private List<String> mountDataResourceRefs;

        public List<String> getMountDataNames() {
            validate();
            return mountDataNames;
        }

        public List<String> getMountDataIds() {
            validate();
            return mountDataIds;
        }

        public List<String> getMountDataResourceRefs() {
            validate();
            return mountDataResourceRefs;
        }

        private void validate() {
            boolean namesProvided = mountDataNames != null;
            boolean resourceRefsProvided = mountDataResourceRefs != null;
            boolean idsProvided = mountDataIds != null;

            // check that exactly 1 is provided
            boolean valid = (namesProvided ? 1 : 0) + (resourceRefsProvided ? 1 : 0) + (idsProvided ? 1 : 0) == 1;

            if (!valid) {
                throw new TowerRuntimeException("Error: --mount-data=<mountDataNames>, --mount-data-ids=<mountDataIds>, --mount-data-resource-refs=<mountDataResourceRefs> are mutually exclusive (specify only one)");
            }
        }
    }

}
