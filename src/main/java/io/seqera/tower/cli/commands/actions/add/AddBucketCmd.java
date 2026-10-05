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

package io.seqera.tower.cli.commands.actions.add;

import io.seqera.tower.model.ActionSource;
import io.seqera.tower.model.BucketActionRequest;
import io.seqera.tower.model.CreateActionRequest;
import picocli.CommandLine;

import java.util.List;

@CommandLine.Command(
        name = "bucket",
        description = "Add a pipeline action triggered by cloud storage events"
)
public class AddBucketCmd extends AbstractAddCmd {

    @CommandLine.Option(names = {"--data-link-id"}, description = "Data link of the bucket to watch. It must be an explicitly created data link with credentials.", required = true)
    public String dataLinkId;

    @CommandLine.Option(names = {"--marker-file"}, description = "Marker file, relative to the data link path, whose events trigger the action (e.g., incoming/.done or signals/*.complete).", required = true)
    public String markerFile;

    @CommandLine.Option(names = {"--events"}, split = ",", description = "Comma-separated bucket events that trigger the action: object:created, object:deleted.", required = true)
    public List<String> events;

    @Override
    protected ActionSource getSource() {
        return ActionSource.bucket;
    }

    @Override
    protected void configureTrigger(CreateActionRequest request) {
        request.setBucket(new BucketActionRequest().dataLinkId(dataLinkId).markerFile(markerFile).events(events));
    }
}
