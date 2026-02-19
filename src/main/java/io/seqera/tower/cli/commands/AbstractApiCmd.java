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

package io.seqera.tower.cli.commands;

import io.seqera.tower.ApiClient;
import io.seqera.tower.ApiException;
import io.seqera.tower.api.ActionsApi;
import io.seqera.tower.api.AvatarsApi;
import io.seqera.tower.api.ComputeEnvsApi;
import io.seqera.tower.api.CredentialsApi;
import io.seqera.tower.api.DataLinksApi;
import io.seqera.tower.api.DatasetsApi;
import io.seqera.tower.api.Ga4ghApi;
import io.seqera.tower.api.LabelsApi;
import io.seqera.tower.api.LaunchApi;
import io.seqera.tower.api.OrgsApi;
import io.seqera.tower.api.PipelineSchemasApi;
import io.seqera.tower.api.PipelineSecretsApi;
import io.seqera.tower.api.PipelineVersionsApi;
import io.seqera.tower.api.PipelinesApi;
import io.seqera.tower.api.PlatformsApi;
import io.seqera.tower.api.ServiceInfoApi;
import io.seqera.tower.api.StudiosApi;
import io.seqera.tower.api.TeamsApi;
import io.seqera.tower.api.TokensApi;
import io.seqera.tower.api.TraceApi;
import io.seqera.tower.api.UsersApi;
import io.seqera.tower.api.WorkflowsApi;
import io.seqera.tower.api.WorkspacesApi;
import io.seqera.tower.cli.Tower;
import io.seqera.tower.cli.commands.labels.Label;
import io.seqera.tower.cli.commands.labels.LabelsFinder;
import io.seqera.tower.cli.commands.pipelines.versions.VersionRefOptions;
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
import io.seqera.tower.model.ComputeEnvComputeConfig;
import io.seqera.tower.model.ComputeEnvQueryAttribute;
import io.seqera.tower.model.ComputeEnvResponseDto;
import io.seqera.tower.model.Credentials;
import io.seqera.tower.model.DataStudioQueryAttribute;
import io.seqera.tower.model.ListComputeEnvsResponseEntry;
import io.seqera.tower.model.ListPipelineVersionsResponse;
import io.seqera.tower.model.ListWorkspacesAndOrgResponse;
import io.seqera.tower.model.OrgAndWorkspaceDto;
import io.seqera.tower.model.PipelineDbDto;
import io.seqera.tower.model.PipelineQueryAttribute;
import io.seqera.tower.model.PipelineVersionFullInfoDto;
import io.seqera.tower.model.UserResponseDto;
import io.seqera.tower.model.WorkflowQueryAttribute;
import org.glassfish.jersey.CommonProperties;
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
import java.util.function.Predicate;
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
    public static final List<PipelineQueryAttribute> NO_PIPELINE_ATTRIBUTES = Collections.EMPTY_LIST;
    public static final List<DataStudioQueryAttribute> NO_STUDIO_ATTRIBUTES = Collections.EMPTY_LIST;

    private ApiClient apiClient;

    private ActionsApi actionsApi;
    private AvatarsApi avatarsApi;
    private ComputeEnvsApi computeEnvsApi;
    private CredentialsApi credentialsApi;
    private DataLinksApi dataLinksApi;
    private DatasetsApi datasetsApi;
    private Ga4ghApi ga4ghApi;
    private LabelsApi labelsApi;
    private LaunchApi launchApi;
    private OrgsApi orgsApi;
    private PipelinesApi pipelinesApi;
    private PipelineSchemasApi pipelineSchemasApi;
    private PipelineSecretsApi pipelineSecretsApi;
    private PipelineVersionsApi pipelineVersionsApi;
    private PlatformsApi platformsApi;
    private ServiceInfoApi serviceInfoApi;
    private StudiosApi studiosApi;
    private TeamsApi teamsApi;
    private TokensApi tokensApi;
    private TraceApi traceApi;
    private UsersApi usersApi;
    private WorkflowsApi workflowsApi;
    private WorkspacesApi workspacesApi;

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

    private ApiClient apiClient() throws ApiException {

        // Check we are using HTTPS (unless 'insecure' option is enabled)
        if (!app().insecure && !app().url.startsWith("https")) {
            throw new TowerException(String.format("You are trying to connect to an insecure server: %s%n        if you want to force the connection use '--insecure'. NOT RECOMMENDED!", app().url));
        }

        if (apiClient == null) {

            if (app().token == null) {
                throw new MissingTowerAccessTokenException();
            }

            ApiClient client = buildApiClient();
            client.setServerIndex(null);
            client.setBasePath(app().url);
            client.setBearerToken(app().token);

            // FIXME: Workaround for Platform versions before 26.x returning exit as String. Remove once those versions are phased out (see #578).
            client.getJSON().getMapper().addMixIn(
                    io.seqera.tower.model.Task.class,
                    io.seqera.tower.cli.utils.TaskExitMixin.class
            );

            // Set HTTP Agent header
            Properties props = getCliProperties();
            client.setUserAgent(String.format("tw/%s (%s)", props.get("version"), props.get("platform")));

            apiClient = client;
        }

        return apiClient;
    }

    protected ActionsApi actionsApi() throws ApiException {
        return actionsApi == null ? new ActionsApi(apiClient()) : actionsApi;
    }

    protected AvatarsApi avatarsApi() throws ApiException {
        return avatarsApi == null ? new AvatarsApi(apiClient()) : avatarsApi;
    }

    protected ComputeEnvsApi computeEnvsApi() throws ApiException {
        return computeEnvsApi == null ? new ComputeEnvsApi(apiClient()) : computeEnvsApi;
    }

    protected CredentialsApi credentialsApi() throws ApiException {
        return credentialsApi == null ? new CredentialsApi(apiClient()) : credentialsApi;
    }

    protected DataLinksApi dataLinksApi() throws ApiException {
        return dataLinksApi == null ? new DataLinksApi(apiClient()) : dataLinksApi;
    }

    protected DatasetsApi datasetsApi() throws ApiException {
        return datasetsApi == null ? new DatasetsApi(apiClient()) : datasetsApi;
    }

    protected Ga4ghApi ga4ghApi() throws ApiException {
        return ga4ghApi == null ? new Ga4ghApi(apiClient()) : ga4ghApi;
    }

    protected LabelsApi labelsApi() throws ApiException {
        return labelsApi == null ? new LabelsApi(apiClient()) : labelsApi;
    }

    protected LaunchApi launchApi() throws ApiException {
        return launchApi == null ? new LaunchApi(apiClient()) : launchApi;
    }

    protected OrgsApi orgsApi() throws ApiException {
        return orgsApi == null ? new OrgsApi(apiClient()) : orgsApi;
    }

    protected PipelineSecretsApi pipelineSecretsApi() throws ApiException {
        return pipelineSecretsApi == null ? new PipelineSecretsApi(apiClient()) : pipelineSecretsApi;
    }

    protected PipelineSchemasApi pipelineSchemasApi() throws ApiException {
        return pipelineSchemasApi == null ? new PipelineSchemasApi(apiClient()) : pipelineSchemasApi;
    }

    protected PipelinesApi pipelinesApi() throws ApiException {
        return pipelinesApi == null ? new PipelinesApi(apiClient()) : pipelinesApi;
    }

    protected PipelineVersionsApi pipelineVersionsApi() throws ApiException {
        return pipelineVersionsApi == null ? new PipelineVersionsApi(apiClient()) : pipelineVersionsApi;
    }

    protected PlatformsApi platformsApi() throws ApiException {
        return platformsApi == null ? new PlatformsApi(apiClient()) : platformsApi;
    }

    protected ServiceInfoApi serviceInfoApi() throws ApiException {
        return serviceInfoApi == null ? new ServiceInfoApi(apiClient()) : serviceInfoApi;
    }

    protected StudiosApi studiosApi() throws ApiException {
        return studiosApi == null ? new StudiosApi(apiClient()) : studiosApi;
    }

    protected TeamsApi teamsApi() throws ApiException {
        return teamsApi == null ? new TeamsApi(apiClient()) : teamsApi;
    }

    protected TokensApi tokensApi() throws ApiException {
        return tokensApi == null ? new TokensApi(apiClient()) : tokensApi;
    }

    protected TraceApi traceApi() throws ApiException {
        return traceApi == null ? new TraceApi(apiClient()) : traceApi;
    }

    protected UsersApi usersApi() throws ApiException {
        return usersApi == null ? new UsersApi(apiClient()) : usersApi;
    }

    protected WorkflowsApi workflowsApi() throws ApiException {
        return workflowsApi == null ? new WorkflowsApi(apiClient()) : workflowsApi;
    }

    protected WorkspacesApi workspacesApi() throws ApiException {
        return workspacesApi == null ? new WorkspacesApi(apiClient()) : workspacesApi;
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
            public ClientConfig getDefaultClientConfig() {
                ClientConfig config = super.getDefaultClientConfig();
                // Disable conditionally enabled providers, as warnings get printed when their corresponding classes are not available in the classpath
                config.property(CommonProperties.PROVIDER_DEFAULT_DISABLE, "ALL");

                return config;
            }

            @Override
            protected void applyDebugSetting(ClientConfig clientConfig) {
                if (app().verbose) {
                    clientConfig.register(new LoggingFeature(Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME), java.util.logging.Level.INFO, LoggingFeature.Verbosity.PAYLOAD_ANY, 1024 * 50 /* Log payloads up to 50K */));
                    clientConfig.property(LoggingFeature.LOGGING_FEATURE_VERBOSITY, LoggingFeature.Verbosity.PAYLOAD_ANY);
                }
            }

            @Override
            public Entity<?> serialize(Object obj, Map<String, Object> formParams, String contentType, boolean isBodyNullable) throws ApiException {
                Entity<?> entity = super.serialize(obj, formParams, contentType, isBodyNullable);

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
                    OrgAndWorkspaceDto orgAndWorkspaceDbDto = this.findOrgAndWorkspaceByName(wspRefs[0].strip(), wspRefs[1].strip());
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

        return computeEnvsApi().describeComputeEnv(ceId, workspaceId, NO_CE_ATTRIBUTES).getComputeEnv();
    }

    protected ComputeEnvResponseDto primaryComputeEnv(Long workspaceId) throws ApiException {
        if (primaryComputeEnvId == null) {
            loadAvailableComputeEnvs(workspaceId);
        }

        if (primaryComputeEnvId == null) {
            throw new NoComputeEnvironmentException(workspaceRef(workspaceId));
        }

        return computeEnvsApi().describeComputeEnv(primaryComputeEnvId, workspaceId, NO_CE_ATTRIBUTES).getComputeEnv();
    }

    protected String credentialsByRef(ComputeEnvComputeConfig.PlatformEnum type, Long wspId, String credentialsRef) throws ApiException {
        List<Credentials> credentials = credentialsApi().listCredentials(wspId, type == null ? null : type.getValue()).getCredentials();

        if (credentials.isEmpty()) {
            throw new TowerException("No valid credentials found at the workspace");
        }

        Credentials cred;

        cred = credentials.stream()
                .filter(it -> Objects.equals(it.getId(), credentialsRef) || Objects.equals(it.getName(), credentialsRef))
                .findFirst()
                .orElse(null);

        if (cred == null) {
            throw new TowerException("No valid credentials found at the workspace");
        }

        return cred.getId();
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

    protected String token() {
        return app().token;
    }

    protected OrgAndWorkspaceDto findOrgAndWorkspaceByName(String organizationName, String workspaceName) throws ApiException {
        ListWorkspacesAndOrgResponse workspacesAndOrgResponse = workspacesApi().listWorkspacesUser(userId());

        if (workspacesAndOrgResponse == null || workspacesAndOrgResponse.getOrgsAndWorkspaces() == null) {
            if (workspaceName == null) {
                throw new OrganizationNotFoundException(organizationName);
            }

            throw new WorkspaceNotFoundException(workspaceName, organizationName);
        }

        List<OrgAndWorkspaceDto> orgAndWorkspaceDbDtoList = workspacesAndOrgResponse
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

    protected OrgAndWorkspaceDto findOrganizationByRef(String organizationRef) throws ApiException {
        ListWorkspacesAndOrgResponse workspacesAndOrgResponse = workspacesApi().listWorkspacesUser(userId());

        if (workspacesAndOrgResponse.getOrgsAndWorkspaces() == null) {
            throw new OrganizationNotFoundException(organizationRef);
        }

        List<OrgAndWorkspaceDto> orgAndWorkspaceDbDtoList = workspacesAndOrgResponse
                .getOrgsAndWorkspaces()
                .stream()
                .filter(
                        item -> Objects.equals(item.getWorkspaceName(), null) && (Objects.equals(item.getOrgId().toString(), organizationRef) || Objects.equals(item.getOrgName(), organizationRef))
                )
                .collect(Collectors.toList());

        return orgAndWorkspaceDbDtoList.stream().findFirst().orElseThrow(() -> new OrganizationNotFoundException(organizationRef));
    }

    protected List<Long> findOrCreateLabels(Long wspId, List<Label> labels) throws ApiException {
        if (labels != null && !labels.isEmpty()) {
            LabelsFinder finder = new LabelsFinder(labelsApi());
            return finder.findLabelsIds(wspId,labels, LabelsFinder.NotFoundLabelBehavior.CREATE);
        } else {
            return null;
        }
    }

    private void loadUser() throws ApiException {
        UserResponseDto user = usersApi().userInfo().getUser();
        userName = user.getUserName();
        userId = user.getId();
    }

    private void loadOrgAndWorkspaceFromIds(Long workspaceId) throws ApiException {
        for (OrgAndWorkspaceDto ow : workspacesApi().listWorkspacesUser(userId()).getOrgsAndWorkspaces()) {
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
        for (OrgAndWorkspaceDto ow : workspacesApi().listWorkspacesUser(userId()).getOrgsAndWorkspaces()) {
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
            for (ListComputeEnvsResponseEntry ce : computeEnvsApi().listComputeEnvs("AVAILABLE", workspaceId, List.of()).getComputeEnvs()) {

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

    protected PipelineVersionFullInfoDto findPipelineVersionByRef(Long pipelineId, Long wspId, VersionRefOptions.VersionRef ref) throws ApiException {
        String search = ref.versionName;
        Boolean isPublished = ref.versionName != null ? true : null;
        Predicate<PipelineVersionFullInfoDto> matcher = ref.versionId != null
                ? v -> ref.versionId.equals(v.getId())
                : v -> ref.versionName.equals(v.getName());

        ListPipelineVersionsResponse response = pipelineVersionsApi()
                .listPipelineVersions(pipelineId, wspId, null, null, search, isPublished);

        if (response.getVersions() == null) {
            throw new TowerException("No versions available for the pipeline, check if Pipeline versioning feature is enabled for the workspace");
        }

        return response.getVersions().stream()
                .map(PipelineDbDto::getVersion)
                .filter(Objects::nonNull)
                .filter(matcher)
                .findFirst()
                .orElse(null);
    }

    protected String resolvePipelineVersionId(Long pipelineId, Long wspId, VersionRefOptions.VersionRef versionRef) throws ApiException {
        if (versionRef == null) return null;
        if (versionRef.versionId != null) return versionRef.versionId;

        PipelineVersionFullInfoDto version = findPipelineVersionByRef(pipelineId, wspId, versionRef);
        if (version == null) {
            throw new TowerException(String.format("Pipeline version '%s' not found", versionRef.versionName));
        }
        return version.getId();
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

    protected Response exec() throws ApiException, IOException, InterruptedException {
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
