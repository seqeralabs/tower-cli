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
import java.util.Optional;
import java.util.stream.Collectors;

public class LabelsFinder {

    private final DefaultApi api;


    public LabelsFinder(DefaultApi api) {
        this.api = api;
    }


    public List<Long> findLabelsIds(Long wspId, List<Label> labels, boolean noCreate) throws TowerException {
        return labels.stream().map(e -> fetchOrCreateLabelId(wspId, e, noCreate)).collect(Collectors.toList());
    }

    private Long fetchOrCreateLabelId(Long wspId, Label label, boolean noCreate) throws TowerRuntimeException {
        int offset = 0;
        int pageSize = 100;
        long maxElements = Long.MAX_VALUE;
        try {
            while (offset + pageSize < maxElements) {
                ListLabelsResponse resp = api.listLabels(wspId, pageSize, offset, label.name, label.getType());
                maxElements = resp.getTotalSize();
                Optional<LabelDbDto> labelResponse = resp.getLabels().stream().filter(l -> label.matches(l.getName(), l.getValue())).findFirst();
                if (labelResponse.isPresent()) {
                    return labelResponse.get().getId();
                }
                offset += pageSize;
            }
        } catch (ApiException e) {
            throw new TowerRuntimeException(String.format("Failed to list labels [%s]", e.getMessage()));
        }
        if (!noCreate) {
            return createLabel(wspId, label);
        }
        throw new TowerRuntimeException(String.format("Label '%s' does not exists in workspace '%s'", label, wspId));
    }


    private Long createLabel(Long wspId, Label label) throws TowerRuntimeException {
        try {
            CreateLabelRequest request = new CreateLabelRequest().name(label.name);
            if (label.value != null) {
                request.value(label.value);
            }
            return api.createLabel(request, wspId).getId();
        } catch (ApiException e) {
            throw new TowerRuntimeException(String.format("Failed to create label '%s' in workspace %s", label, wspId));
        }
    }
}
