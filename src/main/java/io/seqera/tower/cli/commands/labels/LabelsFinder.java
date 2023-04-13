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

package io.seqera.tower.cli.commands.labels;

import io.seqera.tower.ApiException;
import io.seqera.tower.api.DefaultApi;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.exceptions.TowerRuntimeException;
import io.seqera.tower.model.CreateLabelRequest;
import io.seqera.tower.model.LabelDbDto;
import io.seqera.tower.model.ListLabelsResponse;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class LabelsFinder {

    public enum NotFoundLabelBehavior {
        CREATE,
        FAIL,
        FILTER
    }

    private final DefaultApi api;


    public LabelsFinder(DefaultApi api) {
        this.api = api;
    }


    public List<Long> findLabelsIds(Long wspId, List<Label> labels, NotFoundLabelBehavior behavior) throws TowerException {
        return labels.stream().map(e -> fetchOrCreateLabelId(wspId, e, behavior))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Long fetchOrCreateLabelId(Long wspId, Label label, NotFoundLabelBehavior notFound) throws TowerRuntimeException {
        int offset = 0;
        int pageSize = 100;
        long maxElements = Long.MAX_VALUE;
        try {
            while (offset + pageSize < maxElements) {
                ListLabelsResponse resp = api.listLabels(wspId, pageSize, offset, label.name, label.getType());
                maxElements = Objects.requireNonNull(resp.getTotalSize(), "List labels API didn't return any result.");
                Optional<LabelDbDto> labelResponse = Objects.requireNonNull(resp.getLabels(), "List labels API didn't return labels.")
                        .stream()
                        .filter(l -> label.matches(l.getName(), l.getValue()))
                        .findFirst();
                if (labelResponse.isPresent()) {
                    return labelResponse.get().getId();
                }
                offset += pageSize;
            }
        } catch (ApiException e) {
            throw new TowerRuntimeException(String.format("Failed to list labels [%s]", e.getMessage()));
        }
        switch (notFound) {
            case CREATE:
                return createLabel(wspId,label);
            case FILTER:
                return null;
            default:
                throw new TowerRuntimeException(String.format("Label '%s' does not exists in workspace '%s'", label, wspId));

        }
    }

    private Long createLabel(Long wspId, Label label) throws TowerRuntimeException {
        try {
            CreateLabelRequest request = new CreateLabelRequest().name(label.name);
            if (label.value != null) {
                request.value(label.value);
                request.resource(true);
            }
            return api.createLabel(request, wspId).getId();
        } catch (ApiException e) {
            throw new TowerRuntimeException(String.format("Failed to create label '%s' in workspace %s", label, wspId));
        }
    }
}
