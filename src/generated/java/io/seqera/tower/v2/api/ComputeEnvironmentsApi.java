package io.seqera.tower.v2.api;

import io.seqera.tower.v2.ApiException;
import io.seqera.tower.v2.ApiClient;
import io.seqera.tower.v2.ApiResponse;
import io.seqera.tower.v2.Configuration;
import io.seqera.tower.v2.Pair;

import javax.ws.rs.core.GenericType;

import io.seqera.tower.v2.model.ComputeEnvPatchModel;
import io.seqera.tower.v2.model.ComputeEnvReadModel;
import io.seqera.tower.v2.model.ComputeEnvWriteModel;
import io.seqera.tower.v2.model.ComputeEnvsPage;
import io.seqera.tower.v2.model.CreateComputeEnv400Response;
import io.seqera.tower.v2.model.ErrorDetail;
import io.seqera.tower.v2.model.ValidateComputeEnvResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.17.0")
public class ComputeEnvironmentsApi {
  private ApiClient apiClient;

  public ComputeEnvironmentsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public ComputeEnvironmentsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Get the API client
   *
   * @return API client
   */
  public ApiClient getApiClient() {
    return apiClient;
  }

  /**
   * Set the API client
   *
   * @param apiClient an instance of API client
   */
  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Create compute environment
   * Creates a new compute environment in the workspace.  The cloud resources of a compute environment whose configuration requests them (such as AWS Batch Forge) are provisioned asynchronously, while its status is &#x60;CREATING&#x60;.  **Required permission:** &#x60;compute_environment:write&#x60;
   * @param workspaceId Workspace identifier. (required)
   * @param computeEnvWriteModel  (required)
   * @param validateOnly Validates the request without creating anything. When &#x60;true&#x60;, the server runs the same validation it would for a real request and reports what the result would be, but persists nothing and triggers no side effects, so the fields the server populates on creation may be missing. Validation failures surface as they would otherwise. Not to be confused with the &#x60;/validate&#x60; operation, which re-runs the pre-flight checks of an existing compute environment. Defaults to &#x60;false&#x60;. (optional, default to false)
   * @return ComputeEnvReadModel
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 201 </td><td> The request has succeeded and a new resource has been created as a result. </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> The request is malformed or invalid. When some of the selected instance types fail the capability requirements of the compute environment, the problem document lists each of them, so they can be dealt with in one go. </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Missing or invalid authentication. </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Missing required permission. </td><td>  -  </td></tr>
       <tr><td> 409 </td><td> Another compute environment in the workspace already goes by that name. </td><td>  -  </td></tr>
     </table>
   */
  public ComputeEnvReadModel createComputeEnv(@javax.annotation.Nonnull String workspaceId, @javax.annotation.Nonnull ComputeEnvWriteModel computeEnvWriteModel, @javax.annotation.Nullable Boolean validateOnly) throws ApiException {
    return createComputeEnvWithHttpInfo(workspaceId, computeEnvWriteModel, validateOnly).getData();
  }

  /**
   * Create compute environment
   * Creates a new compute environment in the workspace.  The cloud resources of a compute environment whose configuration requests them (such as AWS Batch Forge) are provisioned asynchronously, while its status is &#x60;CREATING&#x60;.  **Required permission:** &#x60;compute_environment:write&#x60;
   * @param workspaceId Workspace identifier. (required)
   * @param computeEnvWriteModel  (required)
   * @param validateOnly Validates the request without creating anything. When &#x60;true&#x60;, the server runs the same validation it would for a real request and reports what the result would be, but persists nothing and triggers no side effects, so the fields the server populates on creation may be missing. Validation failures surface as they would otherwise. Not to be confused with the &#x60;/validate&#x60; operation, which re-runs the pre-flight checks of an existing compute environment. Defaults to &#x60;false&#x60;. (optional, default to false)
   * @return ApiResponse&lt;ComputeEnvReadModel&gt;
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 201 </td><td> The request has succeeded and a new resource has been created as a result. </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> The request is malformed or invalid. When some of the selected instance types fail the capability requirements of the compute environment, the problem document lists each of them, so they can be dealt with in one go. </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Missing or invalid authentication. </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Missing required permission. </td><td>  -  </td></tr>
       <tr><td> 409 </td><td> Another compute environment in the workspace already goes by that name. </td><td>  -  </td></tr>
     </table>
   */
  public ApiResponse<ComputeEnvReadModel> createComputeEnvWithHttpInfo(@javax.annotation.Nonnull String workspaceId, @javax.annotation.Nonnull ComputeEnvWriteModel computeEnvWriteModel, @javax.annotation.Nullable Boolean validateOnly) throws ApiException {
    // Check required parameters
    if (workspaceId == null) {
      throw new ApiException(400, "Missing the required parameter 'workspaceId' when calling createComputeEnv");
    }
    if (computeEnvWriteModel == null) {
      throw new ApiException(400, "Missing the required parameter 'computeEnvWriteModel' when calling createComputeEnv");
    }

    // Path parameters
    String localVarPath = "/v2/workspaces/{workspaceId}/compute-envs"
            .replaceAll("\\{workspaceId}", apiClient.escapeString(workspaceId.toString()));

    // Query parameters
    List<Pair> localVarQueryParams = new ArrayList<>(
            apiClient.parameterToPairs("", "validateOnly", validateOnly)
    );

    String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/problem+json");
    String localVarContentType = apiClient.selectHeaderContentType("application/json");
    String[] localVarAuthNames = new String[] {"BearerAuth"};
    GenericType<ComputeEnvReadModel> localVarReturnType = new GenericType<ComputeEnvReadModel>() {};
    return apiClient.invokeAPI("ComputeEnvironmentsApi.createComputeEnv", localVarPath, "POST", localVarQueryParams, computeEnvWriteModel,
                               new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept, localVarContentType,
                               localVarAuthNames, localVarReturnType, false);
  }
  /**
   * Delete compute environment
   * Deletes the compute environment, along with the cloud resources provisioned for it, which are disposed of asynchronously while its status is &#x60;DELETING&#x60;. A compute environment with active jobs, that is, active workflow runs or Studios, cannot be deleted.  Append &#x60;?force&#x3D;true&#x60; to bypass the active-job checks and the cleanup of the cloud resources. Force deletion is only allowed for environments in &#x60;ERRORED&#x60;, &#x60;INVALID&#x60;, or &#x60;DELETING&#x60; status.  **Required permission:** &#x60;compute_environment:delete&#x60;
   * @param workspaceId Workspace identifier. (required)
   * @param computeEnvId Compute environment identifier. (required)
   * @param force If &#x60;true&#x60;, force-deletes a stuck compute environment, bypassing the active-job checks (that is, active workflow runs or Studios) and the cleanup of its cloud resources, which are left for the caller. Only valid for environments in &#x60;ERRORED&#x60;, &#x60;INVALID&#x60;, or &#x60;DELETING&#x60; status. Default: &#x60;false&#x60;. (optional, default to false)
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 204 </td><td> There is no content to send for this request, but the headers may be useful.  </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Malformed or invalid request. </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Missing or invalid authentication. </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Missing required permission. </td><td>  -  </td></tr>
       <tr><td> 409 </td><td> The compute environment cannot be deleted in its current status: it is still being created or already being deleted. </td><td>  -  </td></tr>
     </table>
   */
  public void deleteComputeEnv(@javax.annotation.Nonnull String workspaceId, @javax.annotation.Nonnull String computeEnvId, @javax.annotation.Nullable Boolean force) throws ApiException {
    deleteComputeEnvWithHttpInfo(workspaceId, computeEnvId, force);
  }

  /**
   * Delete compute environment
   * Deletes the compute environment, along with the cloud resources provisioned for it, which are disposed of asynchronously while its status is &#x60;DELETING&#x60;. A compute environment with active jobs, that is, active workflow runs or Studios, cannot be deleted.  Append &#x60;?force&#x3D;true&#x60; to bypass the active-job checks and the cleanup of the cloud resources. Force deletion is only allowed for environments in &#x60;ERRORED&#x60;, &#x60;INVALID&#x60;, or &#x60;DELETING&#x60; status.  **Required permission:** &#x60;compute_environment:delete&#x60;
   * @param workspaceId Workspace identifier. (required)
   * @param computeEnvId Compute environment identifier. (required)
   * @param force If &#x60;true&#x60;, force-deletes a stuck compute environment, bypassing the active-job checks (that is, active workflow runs or Studios) and the cleanup of its cloud resources, which are left for the caller. Only valid for environments in &#x60;ERRORED&#x60;, &#x60;INVALID&#x60;, or &#x60;DELETING&#x60; status. Default: &#x60;false&#x60;. (optional, default to false)
   * @return ApiResponse&lt;Void&gt;
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 204 </td><td> There is no content to send for this request, but the headers may be useful.  </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Malformed or invalid request. </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Missing or invalid authentication. </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Missing required permission. </td><td>  -  </td></tr>
       <tr><td> 409 </td><td> The compute environment cannot be deleted in its current status: it is still being created or already being deleted. </td><td>  -  </td></tr>
     </table>
   */
  public ApiResponse<Void> deleteComputeEnvWithHttpInfo(@javax.annotation.Nonnull String workspaceId, @javax.annotation.Nonnull String computeEnvId, @javax.annotation.Nullable Boolean force) throws ApiException {
    // Check required parameters
    if (workspaceId == null) {
      throw new ApiException(400, "Missing the required parameter 'workspaceId' when calling deleteComputeEnv");
    }
    if (computeEnvId == null) {
      throw new ApiException(400, "Missing the required parameter 'computeEnvId' when calling deleteComputeEnv");
    }

    // Path parameters
    String localVarPath = "/v2/workspaces/{workspaceId}/compute-envs/{computeEnvId}"
            .replaceAll("\\{workspaceId}", apiClient.escapeString(workspaceId.toString()))
            .replaceAll("\\{computeEnvId}", apiClient.escapeString(computeEnvId.toString()));

    // Query parameters
    List<Pair> localVarQueryParams = new ArrayList<>(
            apiClient.parameterToPairs("", "force", force)
    );

    String localVarAccept = apiClient.selectHeaderAccept("application/problem+json", "application/json");
    String localVarContentType = apiClient.selectHeaderContentType();
    String[] localVarAuthNames = new String[] {"BearerAuth"};
    return apiClient.invokeAPI("ComputeEnvironmentsApi.deleteComputeEnv", localVarPath, "DELETE", localVarQueryParams, null,
                               new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept, localVarContentType,
                               localVarAuthNames, null, false);
  }
  /**
   * Disable compute environment
   * Disables the compute environment until it is enabled again.  **Required permission:** &#x60;compute_environment:write&#x60;
   * @param workspaceId Workspace identifier. (required)
   * @param computeEnvId Compute environment identifier. (required)
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 204 </td><td> There is no content to send for this request, but the headers may be useful.  </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Malformed or invalid request. </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Missing or invalid authentication. </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Missing required permission. </td><td>  -  </td></tr>
     </table>
   */
  public void disableComputeEnv(@javax.annotation.Nonnull String workspaceId, @javax.annotation.Nonnull String computeEnvId) throws ApiException {
    disableComputeEnvWithHttpInfo(workspaceId, computeEnvId);
  }

  /**
   * Disable compute environment
   * Disables the compute environment until it is enabled again.  **Required permission:** &#x60;compute_environment:write&#x60;
   * @param workspaceId Workspace identifier. (required)
   * @param computeEnvId Compute environment identifier. (required)
   * @return ApiResponse&lt;Void&gt;
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 204 </td><td> There is no content to send for this request, but the headers may be useful.  </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Malformed or invalid request. </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Missing or invalid authentication. </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Missing required permission. </td><td>  -  </td></tr>
     </table>
   */
  public ApiResponse<Void> disableComputeEnvWithHttpInfo(@javax.annotation.Nonnull String workspaceId, @javax.annotation.Nonnull String computeEnvId) throws ApiException {
    // Check required parameters
    if (workspaceId == null) {
      throw new ApiException(400, "Missing the required parameter 'workspaceId' when calling disableComputeEnv");
    }
    if (computeEnvId == null) {
      throw new ApiException(400, "Missing the required parameter 'computeEnvId' when calling disableComputeEnv");
    }

    // Path parameters
    String localVarPath = "/v2/workspaces/{workspaceId}/compute-envs/{computeEnvId}/disable"
            .replaceAll("\\{workspaceId}", apiClient.escapeString(workspaceId.toString()))
            .replaceAll("\\{computeEnvId}", apiClient.escapeString(computeEnvId.toString()));

    String localVarAccept = apiClient.selectHeaderAccept("application/problem+json", "application/json");
    String localVarContentType = apiClient.selectHeaderContentType();
    String[] localVarAuthNames = new String[] {"BearerAuth"};
    return apiClient.invokeAPI("ComputeEnvironmentsApi.disableComputeEnv", localVarPath, "POST", new ArrayList<>(), null,
                               new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept, localVarContentType,
                               localVarAuthNames, null, false);
  }
  /**
   * Enable compute environment
   * Enables the compute environment, so it can be used again.  **Required permission:** &#x60;compute_environment:write&#x60;
   * @param workspaceId Workspace identifier. (required)
   * @param computeEnvId Compute environment identifier. (required)
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 204 </td><td> There is no content to send for this request, but the headers may be useful.  </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Malformed or invalid request. </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Missing or invalid authentication. </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Missing required permission. </td><td>  -  </td></tr>
     </table>
   */
  public void enableComputeEnv(@javax.annotation.Nonnull String workspaceId, @javax.annotation.Nonnull String computeEnvId) throws ApiException {
    enableComputeEnvWithHttpInfo(workspaceId, computeEnvId);
  }

  /**
   * Enable compute environment
   * Enables the compute environment, so it can be used again.  **Required permission:** &#x60;compute_environment:write&#x60;
   * @param workspaceId Workspace identifier. (required)
   * @param computeEnvId Compute environment identifier. (required)
   * @return ApiResponse&lt;Void&gt;
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 204 </td><td> There is no content to send for this request, but the headers may be useful.  </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Malformed or invalid request. </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Missing or invalid authentication. </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Missing required permission. </td><td>  -  </td></tr>
     </table>
   */
  public ApiResponse<Void> enableComputeEnvWithHttpInfo(@javax.annotation.Nonnull String workspaceId, @javax.annotation.Nonnull String computeEnvId) throws ApiException {
    // Check required parameters
    if (workspaceId == null) {
      throw new ApiException(400, "Missing the required parameter 'workspaceId' when calling enableComputeEnv");
    }
    if (computeEnvId == null) {
      throw new ApiException(400, "Missing the required parameter 'computeEnvId' when calling enableComputeEnv");
    }

    // Path parameters
    String localVarPath = "/v2/workspaces/{workspaceId}/compute-envs/{computeEnvId}/enable"
            .replaceAll("\\{workspaceId}", apiClient.escapeString(workspaceId.toString()))
            .replaceAll("\\{computeEnvId}", apiClient.escapeString(computeEnvId.toString()));

    String localVarAccept = apiClient.selectHeaderAccept("application/problem+json", "application/json");
    String localVarContentType = apiClient.selectHeaderContentType();
    String[] localVarAuthNames = new String[] {"BearerAuth"};
    return apiClient.invokeAPI("ComputeEnvironmentsApi.enableComputeEnv", localVarPath, "POST", new ArrayList<>(), null,
                               new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept, localVarContentType,
                               localVarAuthNames, null, false);
  }
  /**
   * Get compute environment
   * Retrieves detailed information for the compute environment.  **Required permission:** &#x60;compute_environment:read&#x60;
   * @param workspaceId Workspace identifier. (required)
   * @param computeEnvId Compute environment identifier. (required)
   * @param include Additional fields to include in the compute environment, as a comma-separated list. Accepts &#x60;resourceLabels&#x60; or &#x60;resources&#x60;. Fields not included are &#x60;null&#x60;; unknown fields are rejected. (optional)
   * @return ComputeEnvReadModel
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 200 </td><td> The request has succeeded. </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Malformed or invalid request. </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Missing or invalid authentication. </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Missing required permission. </td><td>  -  </td></tr>
     </table>
   */
  public ComputeEnvReadModel getComputeEnv(@javax.annotation.Nonnull String workspaceId, @javax.annotation.Nonnull String computeEnvId, @javax.annotation.Nullable List<String> include) throws ApiException {
    return getComputeEnvWithHttpInfo(workspaceId, computeEnvId, include).getData();
  }

  /**
   * Get compute environment
   * Retrieves detailed information for the compute environment.  **Required permission:** &#x60;compute_environment:read&#x60;
   * @param workspaceId Workspace identifier. (required)
   * @param computeEnvId Compute environment identifier. (required)
   * @param include Additional fields to include in the compute environment, as a comma-separated list. Accepts &#x60;resourceLabels&#x60; or &#x60;resources&#x60;. Fields not included are &#x60;null&#x60;; unknown fields are rejected. (optional)
   * @return ApiResponse&lt;ComputeEnvReadModel&gt;
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 200 </td><td> The request has succeeded. </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Malformed or invalid request. </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Missing or invalid authentication. </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Missing required permission. </td><td>  -  </td></tr>
     </table>
   */
  public ApiResponse<ComputeEnvReadModel> getComputeEnvWithHttpInfo(@javax.annotation.Nonnull String workspaceId, @javax.annotation.Nonnull String computeEnvId, @javax.annotation.Nullable List<String> include) throws ApiException {
    // Check required parameters
    if (workspaceId == null) {
      throw new ApiException(400, "Missing the required parameter 'workspaceId' when calling getComputeEnv");
    }
    if (computeEnvId == null) {
      throw new ApiException(400, "Missing the required parameter 'computeEnvId' when calling getComputeEnv");
    }

    // Path parameters
    String localVarPath = "/v2/workspaces/{workspaceId}/compute-envs/{computeEnvId}"
            .replaceAll("\\{workspaceId}", apiClient.escapeString(workspaceId.toString()))
            .replaceAll("\\{computeEnvId}", apiClient.escapeString(computeEnvId.toString()));

    // Query parameters
    List<Pair> localVarQueryParams = new ArrayList<>(
            apiClient.parameterToPairs("csv", "include", include)
    );

    String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/problem+json");
    String localVarContentType = apiClient.selectHeaderContentType();
    String[] localVarAuthNames = new String[] {"BearerAuth"};
    GenericType<ComputeEnvReadModel> localVarReturnType = new GenericType<ComputeEnvReadModel>() {};
    return apiClient.invokeAPI("ComputeEnvironmentsApi.getComputeEnv", localVarPath, "GET", localVarQueryParams, null,
                               new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept, localVarContentType,
                               localVarAuthNames, localVarReturnType, false);
  }
  /**
   * List compute environments
   * Lists the compute environments of the workspace.  **Required permission:** &#x60;compute_environment:read&#x60;
   * @param workspaceId Workspace identifier. (required)
   * @param pageToken Continuation token taken from the &#x60;nextPageToken&#x60; of a prior response. Omit it to retrieve the first page. A malformed token is rejected. (optional)
   * @param pageSize Maximum number of compute environments to return. Must be between 1 and 100. Default: &#x60;50&#x60;. (optional, default to 50)
   * @param filter Filter expression narrowing the page to the compute environments it matches.  The filterable fields are the following:  - &#x60;status&#x60;: its values are the ones of the &#x60;status&#x60; field, such as &#x60;AVAILABLE&#x60; or &#x60;INVALID&#x60;. It supports:   - equality: &#x60;status &#x3D;&#x3D; \&quot;AVAILABLE\&quot;&#x60;   - membership: &#x60;status in [\&quot;AVAILABLE\&quot;, \&quot;DISABLED\&quot;]&#x60;   - disjunctions of them: &#x60;status &#x3D;&#x3D; \&quot;AVAILABLE\&quot; || status &#x3D;&#x3D; \&quot;DISABLED\&quot;&#x60;  Malformed expressions are rejected. (optional)
   * @param include Additional fields to include in each compute environment, as a comma-separated list. Accepts &#x60;resourceLabels&#x60; or &#x60;resources&#x60;. Fields not included are &#x60;null&#x60;; unknown fields are rejected. (optional)
   * @return ComputeEnvsPage
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 200 </td><td> The request has succeeded. </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Malformed or invalid request. </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Missing or invalid authentication. </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Missing required permission. </td><td>  -  </td></tr>
     </table>
   */
  public ComputeEnvsPage listComputeEnvs(@javax.annotation.Nonnull String workspaceId, @javax.annotation.Nullable String pageToken, @javax.annotation.Nullable Integer pageSize, @javax.annotation.Nullable String filter, @javax.annotation.Nullable List<String> include) throws ApiException {
    return listComputeEnvsWithHttpInfo(workspaceId, pageToken, pageSize, filter, include).getData();
  }

  /**
   * List compute environments
   * Lists the compute environments of the workspace.  **Required permission:** &#x60;compute_environment:read&#x60;
   * @param workspaceId Workspace identifier. (required)
   * @param pageToken Continuation token taken from the &#x60;nextPageToken&#x60; of a prior response. Omit it to retrieve the first page. A malformed token is rejected. (optional)
   * @param pageSize Maximum number of compute environments to return. Must be between 1 and 100. Default: &#x60;50&#x60;. (optional, default to 50)
   * @param filter Filter expression narrowing the page to the compute environments it matches.  The filterable fields are the following:  - &#x60;status&#x60;: its values are the ones of the &#x60;status&#x60; field, such as &#x60;AVAILABLE&#x60; or &#x60;INVALID&#x60;. It supports:   - equality: &#x60;status &#x3D;&#x3D; \&quot;AVAILABLE\&quot;&#x60;   - membership: &#x60;status in [\&quot;AVAILABLE\&quot;, \&quot;DISABLED\&quot;]&#x60;   - disjunctions of them: &#x60;status &#x3D;&#x3D; \&quot;AVAILABLE\&quot; || status &#x3D;&#x3D; \&quot;DISABLED\&quot;&#x60;  Malformed expressions are rejected. (optional)
   * @param include Additional fields to include in each compute environment, as a comma-separated list. Accepts &#x60;resourceLabels&#x60; or &#x60;resources&#x60;. Fields not included are &#x60;null&#x60;; unknown fields are rejected. (optional)
   * @return ApiResponse&lt;ComputeEnvsPage&gt;
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 200 </td><td> The request has succeeded. </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Malformed or invalid request. </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Missing or invalid authentication. </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Missing required permission. </td><td>  -  </td></tr>
     </table>
   */
  public ApiResponse<ComputeEnvsPage> listComputeEnvsWithHttpInfo(@javax.annotation.Nonnull String workspaceId, @javax.annotation.Nullable String pageToken, @javax.annotation.Nullable Integer pageSize, @javax.annotation.Nullable String filter, @javax.annotation.Nullable List<String> include) throws ApiException {
    // Check required parameters
    if (workspaceId == null) {
      throw new ApiException(400, "Missing the required parameter 'workspaceId' when calling listComputeEnvs");
    }

    // Path parameters
    String localVarPath = "/v2/workspaces/{workspaceId}/compute-envs"
            .replaceAll("\\{workspaceId}", apiClient.escapeString(workspaceId.toString()));

    // Query parameters
    List<Pair> localVarQueryParams = new ArrayList<>(
            apiClient.parameterToPairs("", "pageToken", pageToken)
    );
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "pageSize", pageSize));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("csv", "include", include));

    String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/problem+json");
    String localVarContentType = apiClient.selectHeaderContentType();
    String[] localVarAuthNames = new String[] {"BearerAuth"};
    GenericType<ComputeEnvsPage> localVarReturnType = new GenericType<ComputeEnvsPage>() {};
    return apiClient.invokeAPI("ComputeEnvironmentsApi.listComputeEnvs", localVarPath, "GET", localVarQueryParams, null,
                               new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept, localVarContentType,
                               localVarAuthNames, localVarReturnType, false);
  }
  /**
   * Set primary compute environment
   * Makes the compute environment the primary one of the workspace.  **Required permission:** &#x60;compute_environment:write&#x60;
   * @param workspaceId Workspace identifier. (required)
   * @param computeEnvId Compute environment identifier. (required)
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 204 </td><td> There is no content to send for this request, but the headers may be useful.  </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Malformed or invalid request. </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Missing or invalid authentication. </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Missing required permission. </td><td>  -  </td></tr>
     </table>
   */
  public void setPrimaryComputeEnv(@javax.annotation.Nonnull String workspaceId, @javax.annotation.Nonnull String computeEnvId) throws ApiException {
    setPrimaryComputeEnvWithHttpInfo(workspaceId, computeEnvId);
  }

  /**
   * Set primary compute environment
   * Makes the compute environment the primary one of the workspace.  **Required permission:** &#x60;compute_environment:write&#x60;
   * @param workspaceId Workspace identifier. (required)
   * @param computeEnvId Compute environment identifier. (required)
   * @return ApiResponse&lt;Void&gt;
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 204 </td><td> There is no content to send for this request, but the headers may be useful.  </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Malformed or invalid request. </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Missing or invalid authentication. </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Missing required permission. </td><td>  -  </td></tr>
     </table>
   */
  public ApiResponse<Void> setPrimaryComputeEnvWithHttpInfo(@javax.annotation.Nonnull String workspaceId, @javax.annotation.Nonnull String computeEnvId) throws ApiException {
    // Check required parameters
    if (workspaceId == null) {
      throw new ApiException(400, "Missing the required parameter 'workspaceId' when calling setPrimaryComputeEnv");
    }
    if (computeEnvId == null) {
      throw new ApiException(400, "Missing the required parameter 'computeEnvId' when calling setPrimaryComputeEnv");
    }

    // Path parameters
    String localVarPath = "/v2/workspaces/{workspaceId}/compute-envs/{computeEnvId}/set-primary"
            .replaceAll("\\{workspaceId}", apiClient.escapeString(workspaceId.toString()))
            .replaceAll("\\{computeEnvId}", apiClient.escapeString(computeEnvId.toString()));

    String localVarAccept = apiClient.selectHeaderAccept("application/problem+json", "application/json");
    String localVarContentType = apiClient.selectHeaderContentType();
    String[] localVarAuthNames = new String[] {"BearerAuth"};
    return apiClient.invokeAPI("ComputeEnvironmentsApi.setPrimaryComputeEnv", localVarPath, "POST", new ArrayList<>(), null,
                               new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept, localVarContentType,
                               localVarAuthNames, null, false);
  }
  /**
   * Update compute environment
   * Updates the compute environment with the provided values. Only the fields named by &#x60;updateMask&#x60;, or supplied in the request body when it is omitted, are modified.  Replacing the credentials probes the platform with the new ones, and makes the compute environment &#x60;AVAILABLE&#x60;.  **Required permission:** &#x60;compute_environment:write&#x60;
   * @param workspaceId Workspace identifier. (required)
   * @param computeEnvId Compute environment identifier. (required)
   * @param computeEnvPatchModel  (required)
   * @param updateMask Names of the fields to write, as a comma-separated list. When given, exactly those fields are written. When omitted, the mask is inferred from the request body, where a field carrying a value joins it and one carried as &#x60;null&#x60; or omitted stays out. (optional)
   * @param validateOnly Validates the request without updating anything. When &#x60;true&#x60;, the server runs the same validation it would for a real request and reports what the result would be, but persists nothing and triggers no side effects. Validation failures surface as they would otherwise. Not to be confused with the &#x60;/validate&#x60; operation, which re-runs the pre-flight checks of an existing compute environment. Defaults to &#x60;false&#x60;. (optional, default to false)
   * @return ComputeEnvReadModel
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 200 </td><td> The request has succeeded. </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Malformed or invalid request. </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Missing or invalid authentication. </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Missing required permission. </td><td>  -  </td></tr>
       <tr><td> 409 </td><td> Another compute environment in the workspace already goes by that name. </td><td>  -  </td></tr>
     </table>
   */
  public ComputeEnvReadModel updateComputeEnv(@javax.annotation.Nonnull String workspaceId, @javax.annotation.Nonnull String computeEnvId, @javax.annotation.Nonnull ComputeEnvPatchModel computeEnvPatchModel, @javax.annotation.Nullable List<String> updateMask, @javax.annotation.Nullable Boolean validateOnly) throws ApiException {
    return updateComputeEnvWithHttpInfo(workspaceId, computeEnvId, computeEnvPatchModel, updateMask, validateOnly).getData();
  }

  /**
   * Update compute environment
   * Updates the compute environment with the provided values. Only the fields named by &#x60;updateMask&#x60;, or supplied in the request body when it is omitted, are modified.  Replacing the credentials probes the platform with the new ones, and makes the compute environment &#x60;AVAILABLE&#x60;.  **Required permission:** &#x60;compute_environment:write&#x60;
   * @param workspaceId Workspace identifier. (required)
   * @param computeEnvId Compute environment identifier. (required)
   * @param computeEnvPatchModel  (required)
   * @param updateMask Names of the fields to write, as a comma-separated list. When given, exactly those fields are written. When omitted, the mask is inferred from the request body, where a field carrying a value joins it and one carried as &#x60;null&#x60; or omitted stays out. (optional)
   * @param validateOnly Validates the request without updating anything. When &#x60;true&#x60;, the server runs the same validation it would for a real request and reports what the result would be, but persists nothing and triggers no side effects. Validation failures surface as they would otherwise. Not to be confused with the &#x60;/validate&#x60; operation, which re-runs the pre-flight checks of an existing compute environment. Defaults to &#x60;false&#x60;. (optional, default to false)
   * @return ApiResponse&lt;ComputeEnvReadModel&gt;
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 200 </td><td> The request has succeeded. </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Malformed or invalid request. </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Missing or invalid authentication. </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Missing required permission. </td><td>  -  </td></tr>
       <tr><td> 409 </td><td> Another compute environment in the workspace already goes by that name. </td><td>  -  </td></tr>
     </table>
   */
  public ApiResponse<ComputeEnvReadModel> updateComputeEnvWithHttpInfo(@javax.annotation.Nonnull String workspaceId, @javax.annotation.Nonnull String computeEnvId, @javax.annotation.Nonnull ComputeEnvPatchModel computeEnvPatchModel, @javax.annotation.Nullable List<String> updateMask, @javax.annotation.Nullable Boolean validateOnly) throws ApiException {
    // Check required parameters
    if (workspaceId == null) {
      throw new ApiException(400, "Missing the required parameter 'workspaceId' when calling updateComputeEnv");
    }
    if (computeEnvId == null) {
      throw new ApiException(400, "Missing the required parameter 'computeEnvId' when calling updateComputeEnv");
    }
    if (computeEnvPatchModel == null) {
      throw new ApiException(400, "Missing the required parameter 'computeEnvPatchModel' when calling updateComputeEnv");
    }

    // Path parameters
    String localVarPath = "/v2/workspaces/{workspaceId}/compute-envs/{computeEnvId}"
            .replaceAll("\\{workspaceId}", apiClient.escapeString(workspaceId.toString()))
            .replaceAll("\\{computeEnvId}", apiClient.escapeString(computeEnvId.toString()));

    // Query parameters
    List<Pair> localVarQueryParams = new ArrayList<>(
            apiClient.parameterToPairs("csv", "updateMask", updateMask)
    );
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "validateOnly", validateOnly));

    String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/problem+json");
    String localVarContentType = apiClient.selectHeaderContentType("application/json");
    String[] localVarAuthNames = new String[] {"BearerAuth"};
    GenericType<ComputeEnvReadModel> localVarReturnType = new GenericType<ComputeEnvReadModel>() {};
    return apiClient.invokeAPI("ComputeEnvironmentsApi.updateComputeEnv", localVarPath, "PATCH", localVarQueryParams, computeEnvPatchModel,
                               new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept, localVarContentType,
                               localVarAuthNames, localVarReturnType, false);
  }
  /**
   * Validate compute environment
   * Re-runs the pre-flight checks for the compute environment, including credential validation and work directory accessibility. Persists the outcome to the compute environment and its credentials. Use to recover an &#x60;INVALID&#x60; compute environment after the underlying problem has been fixed. Only compute environments in &#x60;AVAILABLE&#x60; or &#x60;INVALID&#x60; status can be validated.  **Required permission:** &#x60;compute_environment:write&#x60;
   * @param workspaceId Workspace identifier. (required)
   * @param computeEnvId Compute environment identifier. (required)
   * @param force If &#x60;true&#x60;, skips the credential and work directory checks and forces the compute environment to &#x60;AVAILABLE&#x60;. Only valid for an &#x60;INVALID&#x60; compute environment whose credentials are &#x60;AVAILABLE&#x60;. Default: &#x60;false&#x60;. (optional, default to false)
   * @return ValidateComputeEnvResponse
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 200 </td><td> The request has succeeded. </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Malformed or invalid request. </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Missing or invalid authentication. </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Missing required permission. </td><td>  -  </td></tr>
       <tr><td> 405 </td><td> Validation is not available, since the pre-flight checks are disabled in this installation. </td><td>  -  </td></tr>
       <tr><td> 409 </td><td> The checks were forced, but the compute environment is not &#x60;INVALID&#x60; or its credentials are not &#x60;AVAILABLE&#x60;, so its status cannot be forced to &#x60;AVAILABLE&#x60;. </td><td>  -  </td></tr>
     </table>
   */
  public ValidateComputeEnvResponse validateComputeEnv(@javax.annotation.Nonnull String workspaceId, @javax.annotation.Nonnull String computeEnvId, @javax.annotation.Nullable Boolean force) throws ApiException {
    return validateComputeEnvWithHttpInfo(workspaceId, computeEnvId, force).getData();
  }

  /**
   * Validate compute environment
   * Re-runs the pre-flight checks for the compute environment, including credential validation and work directory accessibility. Persists the outcome to the compute environment and its credentials. Use to recover an &#x60;INVALID&#x60; compute environment after the underlying problem has been fixed. Only compute environments in &#x60;AVAILABLE&#x60; or &#x60;INVALID&#x60; status can be validated.  **Required permission:** &#x60;compute_environment:write&#x60;
   * @param workspaceId Workspace identifier. (required)
   * @param computeEnvId Compute environment identifier. (required)
   * @param force If &#x60;true&#x60;, skips the credential and work directory checks and forces the compute environment to &#x60;AVAILABLE&#x60;. Only valid for an &#x60;INVALID&#x60; compute environment whose credentials are &#x60;AVAILABLE&#x60;. Default: &#x60;false&#x60;. (optional, default to false)
   * @return ApiResponse&lt;ValidateComputeEnvResponse&gt;
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 200 </td><td> The request has succeeded. </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Malformed or invalid request. </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Missing or invalid authentication. </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Missing required permission. </td><td>  -  </td></tr>
       <tr><td> 405 </td><td> Validation is not available, since the pre-flight checks are disabled in this installation. </td><td>  -  </td></tr>
       <tr><td> 409 </td><td> The checks were forced, but the compute environment is not &#x60;INVALID&#x60; or its credentials are not &#x60;AVAILABLE&#x60;, so its status cannot be forced to &#x60;AVAILABLE&#x60;. </td><td>  -  </td></tr>
     </table>
   */
  public ApiResponse<ValidateComputeEnvResponse> validateComputeEnvWithHttpInfo(@javax.annotation.Nonnull String workspaceId, @javax.annotation.Nonnull String computeEnvId, @javax.annotation.Nullable Boolean force) throws ApiException {
    // Check required parameters
    if (workspaceId == null) {
      throw new ApiException(400, "Missing the required parameter 'workspaceId' when calling validateComputeEnv");
    }
    if (computeEnvId == null) {
      throw new ApiException(400, "Missing the required parameter 'computeEnvId' when calling validateComputeEnv");
    }

    // Path parameters
    String localVarPath = "/v2/workspaces/{workspaceId}/compute-envs/{computeEnvId}/validate"
            .replaceAll("\\{workspaceId}", apiClient.escapeString(workspaceId.toString()))
            .replaceAll("\\{computeEnvId}", apiClient.escapeString(computeEnvId.toString()));

    // Query parameters
    List<Pair> localVarQueryParams = new ArrayList<>(
            apiClient.parameterToPairs("", "force", force)
    );

    String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/problem+json");
    String localVarContentType = apiClient.selectHeaderContentType();
    String[] localVarAuthNames = new String[] {"BearerAuth"};
    GenericType<ValidateComputeEnvResponse> localVarReturnType = new GenericType<ValidateComputeEnvResponse>() {};
    return apiClient.invokeAPI("ComputeEnvironmentsApi.validateComputeEnv", localVarPath, "POST", localVarQueryParams, null,
                               new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept, localVarContentType,
                               localVarAuthNames, localVarReturnType, false);
  }
}
