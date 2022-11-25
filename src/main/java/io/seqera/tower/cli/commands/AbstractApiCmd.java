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

package io.seqera.tower.cli.commands;

import io.seqera.tower.ApiClient;
import io.seqera.tower.ApiException;
import io.seqera.tower.api.DefaultApi;
import io.seqera.tower.cli.Tower;
import io.seqera.tower.cli.exceptions.ComputeEnvNotFoundException;
import io.seqera.tower.cli.exceptions.InvalidWorkspaceParameterException;
import io.seqera.tower.cli.exceptions.MissingTowerAccessTokenException;
import io.seqera.tower.cli.exceptions.NoComputeEnvironmentException;
import io.seqera.tower.cli.exceptions.OrganizationNotFoundException;
import io.seqera.tower.cli.exceptions.ShowUsageException;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.exceptions.WorkspaceNotFoundException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.ActionQueryAttribute;
import io.seqera.tower.model.ComputeEnvQueryAttribute;
import io.seqera.tower.model.ComputeEnvResponseDto;
import io.seqera.tower.model.ListComputeEnvsResponseEntry;
import io.seqera.tower.model.ListWorkspacesAndOrgResponse;
import io.seqera.tower.model.OrgAndWorkspaceDbDto;
import io.seqera.tower.model.PipelineDbDto;
import io.seqera.tower.model.User;
import io.seqera.tower.model.WorkflowQueryAttribute;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.media.multipart.BodyPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import picocli.CommandLine;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URLConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static io.seqera.tower.cli.utils.ResponseHelper.errorMessage;
import static io.seqera.tower.cli.utils.ResponseHelper.outputFormat;

public abstract class AbstractApiCmd extends AbstractCmd {

    public static final String USER_WORKSPACE_NAME = "user";
    public static final String WORKSPACE_REF_SEPARATOR = "/";

    // No attributes constants
    public static final List<ComputeEnvQueryAttribute> NO_CE_ATTRIBUTES = Collections.EMPTY_LIST;
    public static final List<WorkflowQueryAttribute> NO_WORKFLOW_ATTRIBUTES = Collections.EMPTY_LIST;
    public static final List<ActionQueryAttribute> NO_ACTION_ATTRIBUTES = Collections.EMPTY_LIST;


    private DefaultApi api;

    private Long userId;
    private String userName;
    private Long workspaceId;
    private Long orgId;
    private String orgName;
    private String workspaceName;
    private String serverUrl;

    private Map<String, String> availableComputeEnvsNameToId;
    private Map<String, String> availableComputeEnvsIdToName;
    private String primaryComputeEnvId;

    protected AbstractApiCmd() {
    }

    public static String buildWorkspaceRef(String orgName, String workspaceName) {
        return String.format("[%s / %s]", orgName, workspaceName);
    }

    protected Tower app() {
        return (Tower) getSpec().root().userObject();
    }

    protected DefaultApi api() throws ApiException {

        // Check we are using HTTPS (unless 'insecure' option is enabled)
        if (!app().insecure && !app().url.startsWith("https")) {
            throw new TowerException(String.format("You are trying to connect to an insecure server: %s%n        if you want to force the connection use '--insecure'. NOT RECOMMENDED!", app().url));
        }

        if (api == null) {

            if (app().token == null) {
                throw new MissingTowerAccessTokenException();
            }

            ApiClient client = buildApiClient();
            client.setServerIndex(null);
            client.setBasePath(app().url);
            client.setBearerToken(app().token);

            // Set HTTP Agent header
            Properties props = getCliProperties();
            client.setUserAgent(String.format("tw/%s (%s)", props.get("version"), props.get("platform")));

            api = new DefaultApi(client);
        }

        return api;
    }

    protected Properties getCliProperties() throws ApiException {
        Properties properties = new Properties();
        try {
            properties.load(this.getClass().getResourceAsStream("/META-INF/build-info.properties"));
        } catch (IOException e) {
            throw new ApiException("loading build-info.properties");
        }
        return properties;
    }

    private ApiClient buildApiClient() {
        return new ApiClient() {
            @Override
            protected void performAdditionalClientConfiguration(ClientConfig clientConfig) {
                if (app().verbose) {
                    clientConfig.register(new LoggingFeature(Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME), java.util.logging.Level.INFO, LoggingFeature.Verbosity.PAYLOAD_ANY, 1024 * 50 /* Log payloads up to 50K */));
                    clientConfig.property(LoggingFeature.LOGGING_FEATURE_VERBOSITY, LoggingFeature.Verbosity.PAYLOAD_ANY);
                }
            }

            @Override
            public Entity<?> serialize(Object obj, Map<String, Object> formParams, String contentType) throws ApiException {
                Entity<?> entity = super.serialize(obj, formParams, contentType);

                // Current SDK sends all multipart files as 'application/octet-stream'
                // this is a workaround to try to automatically detect the correct
                // content-type depending on the file name.
                if (entity.getEntity() instanceof MultiPart) {
                    for (BodyPart bodyPart : ((MultiPart) entity.getEntity()).getBodyParts()) {
                        String fileName = bodyPart.getContentDisposition().getFileName();
                        bodyPart.setMediaType(guessMediaType(fileName));
                    }
                }
                return entity;
            }

            private MediaType guessMediaType(String fileName) {
                if (fileName.endsWith(".csv")) {
                    return MediaType.valueOf("text/csv");
                }

                if (fileName.endsWith(".tsv")) {
                    return MediaType.valueOf("text/tab-separated-values");
                }

                String mediaType = URLConnection.guessContentTypeFromName(fileName);
                if (mediaType != null) {
                    return MediaType.valueOf(mediaType);
                }

                return MediaType.APPLICATION_OCTET_STREAM_TYPE;
            }

        };
    }

    protected Long orgId(Long workspaceId) throws ApiException {
        if (orgId == null) {
            if (workspaceId != null) {
                loadOrgAndWorkspaceFromIds(workspaceId);
            } else {
                loadOrgAndWorkspaceFromNames(workspaceId);
            }
        }
        return orgId;
    }

    protected Long userId() throws ApiException {
        if (userId == null) {
            loadUser();
        }
        return userId;
    }

    protected String userName() throws ApiException {
        if (userName == null) {
            loadUser();
        }
        return userName;
    }

    protected String orgName(Long workspaceId) throws ApiException {
        if (orgName == null) {
            loadOrgAndWorkspaceFromIds(workspaceId);
        }
        return orgName;
    }

    protected String workspaceName(Long workspaceId) throws ApiException {
        if (workspaceName == null) {
            loadOrgAndWorkspaceFromIds(workspaceId);
        }
        return workspaceName;
    }

    protected Long workspaceId(String workspaceRef) throws ApiException {
        if (workspaceRef != null) {
            if (workspaceId == null) {
                if (workspaceRef.contains("/")) {
                    String[] wspRefs = workspaceRef.split(WORKSPACE_REF_SEPARATOR);
                    if (wspRefs.length != 2) {
                        throw new InvalidWorkspaceParameterException(workspaceRef);
                    }
                    OrgAndWorkspaceDbDto orgAndWorkspaceDbDto = this.findOrgAndWorkspaceByName(wspRefs[0].strip(), wspRefs[1].strip());
                    if (orgAndWorkspaceDbDto != null) {
                        workspaceName = orgAndWorkspaceDbDto.getWorkspaceName();
                        workspaceId = orgAndWorkspaceDbDto.getWorkspaceId();
                        orgName = orgAndWorkspaceDbDto.getOrgName();
                        orgId = orgAndWorkspaceDbDto.getOrgId();
                    }
                } else {
                    try {
                        workspaceId = Long.valueOf(workspaceRef);
                    } catch (NumberFormatException e) {
                        throw new InvalidWorkspaceParameterException(workspaceRef);
                    }
                }
            }
        }

        return workspaceId;
    }

    protected ComputeEnvResponseDto computeEnvByRef(Long workspaceId, String ref) throws ApiException {
        loadAvailableComputeEnvs(workspaceId);

        String ceId = availableComputeEnvsIdToName.containsKey(ref) ? ref : availableComputeEnvsNameToId.getOrDefault(ref, null);
        if (ceId == null) {
            throw new ComputeEnvNotFoundException(ref, workspaceId);
        }

        return api.describeComputeEnv(ceId, workspaceId, NO_CE_ATTRIBUTES).getComputeEnv();
    }

    protected ComputeEnvResponseDto primaryComputeEnv(Long workspaceId) throws ApiException {
        if (primaryComputeEnvId == null) {
            loadAvailableComputeEnvs(workspaceId);
        }

        if (primaryComputeEnvId == null) {
            throw new NoComputeEnvironmentException(workspaceRef(workspaceId));
        }

        return api().describeComputeEnv(primaryComputeEnvId, workspaceId, NO_CE_ATTRIBUTES).getComputeEnv();
    }

    protected String serverUrl() {
        if (serverUrl == null) {
            serverUrl = app().url.replaceFirst("api\\.", "").replaceFirst("/api", "");
        }
        return serverUrl;
    }

    protected String apiUrl() {
        return app().url;
    }

    protected OrgAndWorkspaceDbDto findOrgAndWorkspaceByName(String organizationName, String workspaceName) throws ApiException {
        ListWorkspacesAndOrgResponse workspacesAndOrgResponse = api().listWorkspacesUser(userId());

        if (workspacesAndOrgResponse == null || workspacesAndOrgResponse.getOrgsAndWorkspaces() == null) {
            if (workspaceName == null) {
                throw new OrganizationNotFoundException(organizationName);
            }

            throw new WorkspaceNotFoundException(workspaceName, organizationName);
        }

        List<OrgAndWorkspaceDbDto> orgAndWorkspaceDbDtoList = workspacesAndOrgResponse
                .getOrgsAndWorkspaces()
                .stream()
                .filter(
                        item -> Objects.equals(item.getWorkspaceName(), workspaceName) && Objects.equals(item.getOrgName(), organizationName)
                )
                .collect(Collectors.toList());

        if (orgAndWorkspaceDbDtoList.isEmpty()) {
            if (workspaceName == null) {
                throw new OrganizationNotFoundException(organizationName);
            }

            throw new WorkspaceNotFoundException(workspaceName, organizationName);
        }

        return orgAndWorkspaceDbDtoList.stream().findFirst().orElse(null);
    }

    protected OrgAndWorkspaceDbDto findOrganizationByRef(String organizationRef) throws ApiException {
        ListWorkspacesAndOrgResponse workspacesAndOrgResponse = api().listWorkspacesUser(userId());

        if (workspacesAndOrgResponse.getOrgsAndWorkspaces() == null) {
            throw new OrganizationNotFoundException(organizationRef);
        }

        List<OrgAndWorkspaceDbDto> orgAndWorkspaceDbDtoList = workspacesAndOrgResponse
                .getOrgsAndWorkspaces()
                .stream()
                .filter(
                        item -> Objects.equals(item.getWorkspaceName(), null) && (Objects.equals(item.getOrgId().toString(), organizationRef) || Objects.equals(item.getOrgName(), organizationRef))
                )
                .collect(Collectors.toList());

        return orgAndWorkspaceDbDtoList.stream().findFirst().orElseThrow(() -> new OrganizationNotFoundException(organizationRef));
    }

    private void loadUser() throws ApiException {
        User user = api().profile().getUser();
        userName = user.getUserName();
        userId = user.getId();
    }

    private void loadOrgAndWorkspaceFromIds(Long workspaceId) throws ApiException {
        for (OrgAndWorkspaceDbDto ow : api().listWorkspacesUser(userId()).getOrgsAndWorkspaces()) {
            if ((workspaceId == null && ow.getWorkspaceId() == null) || (workspaceId != null && workspaceId.equals(ow.getWorkspaceId()))) {
                workspaceName = ow.getWorkspaceName();
                orgId = ow.getOrgId();
                orgName = ow.getOrgName();
                return;
            }
        }

        throw new WorkspaceNotFoundException(workspaceId);
    }

    private void loadOrgAndWorkspaceFromNames(Long workspaceId) throws ApiException {
        String wName = workspaceName(workspaceId);
        String oName = orgName(workspaceId);
        for (OrgAndWorkspaceDbDto ow : api().listWorkspacesUser(userId()).getOrgsAndWorkspaces()) {
            if (wName.equalsIgnoreCase(ow.getWorkspaceName()) && oName.equalsIgnoreCase(ow.getOrgName())) {
                workspaceName = ow.getWorkspaceName();
                orgName = ow.getOrgName();
                workspaceId = ow.getWorkspaceId();
                orgId = ow.getOrgId();
                return;
            }
        }

        throw new WorkspaceNotFoundException(workspaceName, orgName);
    }

    private void loadAvailableComputeEnvs(Long workspaceId) throws ApiException {
        if (availableComputeEnvsNameToId == null) {
            availableComputeEnvsNameToId = new HashMap<>();
            availableComputeEnvsIdToName = new HashMap<>();
            for (ListComputeEnvsResponseEntry ce : api().listComputeEnvs("AVAILABLE", workspaceId).getComputeEnvs()) {

                if (ce.getPrimary() != null && ce.getPrimary()) {
                    primaryComputeEnvId = ce.getId();
                }
                availableComputeEnvsNameToId.put(ce.getName(), ce.getId());
                availableComputeEnvsIdToName.put(ce.getId(), ce.getName());
            }
        }
    }

    protected String workspaceRef(Long workspaceId) throws ApiException {
        //TODO Use a WorkspaceRef class instead of this method?
        if (workspaceId == null) {
            return USER_WORKSPACE_NAME;
        }
        return buildWorkspaceRef(orgName(workspaceId), workspaceName(workspaceId));
    }

    protected Long sourceWorkspaceId(Long currentWorkspace, PipelineDbDto pipeline) {
        if (pipeline == null)
            return null;

        if (pipeline.getWorkspaceId() == null)
            return null;

        if (pipeline.getWorkspaceId().equals(currentWorkspace))
            return null;

        return pipeline.getWorkspaceId();
    }

    @Override
    public Integer call() {
        try {
            Response response = exec();
            int exitCode = outputFormat(app().getOut(), response, app().output);
            return onBeforeExit(exitCode, response);
        } catch (Exception e) {
            errorMessage(app(), e);
        }
        return CommandLine.ExitCode.SOFTWARE;
    }

    protected Integer onBeforeExit(int exitCode, Response response) throws ApiException {
        return exitCode;
    }

    protected Response exec() throws ApiException, IOException {
        throw new ShowUsageException(getSpec());
    }

    protected String baseWorkspaceUrl(Long workspaceId) throws ApiException {
        if (workspaceId == null) {
            return String.format("%s/user/%s", serverUrl(), userName());
        }
        return String.format("%s/orgs/%s/workspaces/%s", serverUrl(), orgName(workspaceId), workspaceName(workspaceId));
    }

    protected String baseOrgUrl(String orgName) throws ApiException {
        return String.format("%s/orgs/%s", serverUrl(), orgName);
    }

}
