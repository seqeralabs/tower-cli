package io.seqera.tower.v2.api;

import io.seqera.tower.v2.ApiException;
import io.seqera.tower.v2.ApiClient;
import io.seqera.tower.v2.ApiResponse;
import io.seqera.tower.v2.Configuration;
import io.seqera.tower.v2.Pair;

import javax.ws.rs.core.GenericType;

import io.seqera.tower.v2.model.ErrorDetail;
import io.seqera.tower.v2.model.PipelineSecretPage;
import io.seqera.tower.v2.model.PipelineSecretPatchModel;
import io.seqera.tower.v2.model.PipelineSecretReadModel;
import io.seqera.tower.v2.model.PipelineSecretWriteModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.17.0")
public class PipelineSecretsApi {
  private ApiClient apiClient;

  public PipelineSecretsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public PipelineSecretsApi(ApiClient apiClient) {
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
   * Create pipeline secret
   * Creates a pipeline secret in the workspace.  **Required permission:** &#x60;pipeline_secrets:write&#x60;
   * @param workspaceId  (required)
   * @param pipelineSecretWriteModel  (required)
   * @param validateOnly Validates the request without creating anything. When &#x60;true&#x60;, the server runs the same validation it would for a real request and reports what the result would be, but persists nothing and triggers no side effects, so the fields the server populates on creation may be missing. Validation failures surface as they would otherwise. Defaults to &#x60;false&#x60;. (optional, default to false)
   * @return PipelineSecretReadModel
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 201 </td><td> The request has succeeded and a new resource has been created as a result. </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Malformed or invalid request. </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Missing or invalid authentication. </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Missing required permission. </td><td>  -  </td></tr>
       <tr><td> 409 </td><td> The request conflicts with the current state of the resource. </td><td>  -  </td></tr>
     </table>
   */
  public PipelineSecretReadModel createPipelineSecret(@javax.annotation.Nonnull String workspaceId, @javax.annotation.Nonnull PipelineSecretWriteModel pipelineSecretWriteModel, @javax.annotation.Nullable Boolean validateOnly) throws ApiException {
    return createPipelineSecretWithHttpInfo(workspaceId, pipelineSecretWriteModel, validateOnly).getData();
  }

  /**
   * Create pipeline secret
   * Creates a pipeline secret in the workspace.  **Required permission:** &#x60;pipeline_secrets:write&#x60;
   * @param workspaceId  (required)
   * @param pipelineSecretWriteModel  (required)
   * @param validateOnly Validates the request without creating anything. When &#x60;true&#x60;, the server runs the same validation it would for a real request and reports what the result would be, but persists nothing and triggers no side effects, so the fields the server populates on creation may be missing. Validation failures surface as they would otherwise. Defaults to &#x60;false&#x60;. (optional, default to false)
   * @return ApiResponse&lt;PipelineSecretReadModel&gt;
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 201 </td><td> The request has succeeded and a new resource has been created as a result. </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Malformed or invalid request. </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Missing or invalid authentication. </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Missing required permission. </td><td>  -  </td></tr>
       <tr><td> 409 </td><td> The request conflicts with the current state of the resource. </td><td>  -  </td></tr>
     </table>
   */
  public ApiResponse<PipelineSecretReadModel> createPipelineSecretWithHttpInfo(@javax.annotation.Nonnull String workspaceId, @javax.annotation.Nonnull PipelineSecretWriteModel pipelineSecretWriteModel, @javax.annotation.Nullable Boolean validateOnly) throws ApiException {
    // Check required parameters
    if (workspaceId == null) {
      throw new ApiException(400, "Missing the required parameter 'workspaceId' when calling createPipelineSecret");
    }
    if (pipelineSecretWriteModel == null) {
      throw new ApiException(400, "Missing the required parameter 'pipelineSecretWriteModel' when calling createPipelineSecret");
    }

    // Path parameters
    String localVarPath = "/v2/workspaces/{workspaceId}/pipeline-secrets"
            .replaceAll("\\{workspaceId}", apiClient.escapeString(workspaceId.toString()));

    // Query parameters
    List<Pair> localVarQueryParams = new ArrayList<>(
            apiClient.parameterToPairs("", "validateOnly", validateOnly)
    );

    String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/problem+json");
    String localVarContentType = apiClient.selectHeaderContentType("application/json");
    String[] localVarAuthNames = new String[] {"BearerAuth"};
    GenericType<PipelineSecretReadModel> localVarReturnType = new GenericType<PipelineSecretReadModel>() {};
    return apiClient.invokeAPI("PipelineSecretsApi.createPipelineSecret", localVarPath, "POST", localVarQueryParams, pipelineSecretWriteModel,
                               new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept, localVarContentType,
                               localVarAuthNames, localVarReturnType, false);
  }
  /**
   * Delete pipeline secret
   * Deletes the pipeline secret.  **Required permission:** &#x60;pipeline_secrets:delete&#x60;
   * @param workspaceId  (required)
   * @param secretId Pipeline secret identifier. (required)
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
  public void deletePipelineSecret(@javax.annotation.Nonnull String workspaceId, @javax.annotation.Nonnull String secretId) throws ApiException {
    deletePipelineSecretWithHttpInfo(workspaceId, secretId);
  }

  /**
   * Delete pipeline secret
   * Deletes the pipeline secret.  **Required permission:** &#x60;pipeline_secrets:delete&#x60;
   * @param workspaceId  (required)
   * @param secretId Pipeline secret identifier. (required)
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
  public ApiResponse<Void> deletePipelineSecretWithHttpInfo(@javax.annotation.Nonnull String workspaceId, @javax.annotation.Nonnull String secretId) throws ApiException {
    // Check required parameters
    if (workspaceId == null) {
      throw new ApiException(400, "Missing the required parameter 'workspaceId' when calling deletePipelineSecret");
    }
    if (secretId == null) {
      throw new ApiException(400, "Missing the required parameter 'secretId' when calling deletePipelineSecret");
    }

    // Path parameters
    String localVarPath = "/v2/workspaces/{workspaceId}/pipeline-secrets/{secretId}"
            .replaceAll("\\{workspaceId}", apiClient.escapeString(workspaceId.toString()))
            .replaceAll("\\{secretId}", apiClient.escapeString(secretId.toString()));

    String localVarAccept = apiClient.selectHeaderAccept("application/problem+json", "application/json");
    String localVarContentType = apiClient.selectHeaderContentType();
    String[] localVarAuthNames = new String[] {"BearerAuth"};
    return apiClient.invokeAPI("PipelineSecretsApi.deletePipelineSecret", localVarPath, "DELETE", new ArrayList<>(), null,
                               new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept, localVarContentType,
                               localVarAuthNames, null, false);
  }
  /**
   * Get pipeline secret
   * Retrieves the pipeline secret.  **Required permission:** &#x60;pipeline_secrets:read&#x60;
   * @param workspaceId  (required)
   * @param secretId Pipeline secret identifier. (required)
   * @return PipelineSecretReadModel
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
  public PipelineSecretReadModel getPipelineSecret(@javax.annotation.Nonnull String workspaceId, @javax.annotation.Nonnull String secretId) throws ApiException {
    return getPipelineSecretWithHttpInfo(workspaceId, secretId).getData();
  }

  /**
   * Get pipeline secret
   * Retrieves the pipeline secret.  **Required permission:** &#x60;pipeline_secrets:read&#x60;
   * @param workspaceId  (required)
   * @param secretId Pipeline secret identifier. (required)
   * @return ApiResponse&lt;PipelineSecretReadModel&gt;
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
  public ApiResponse<PipelineSecretReadModel> getPipelineSecretWithHttpInfo(@javax.annotation.Nonnull String workspaceId, @javax.annotation.Nonnull String secretId) throws ApiException {
    // Check required parameters
    if (workspaceId == null) {
      throw new ApiException(400, "Missing the required parameter 'workspaceId' when calling getPipelineSecret");
    }
    if (secretId == null) {
      throw new ApiException(400, "Missing the required parameter 'secretId' when calling getPipelineSecret");
    }

    // Path parameters
    String localVarPath = "/v2/workspaces/{workspaceId}/pipeline-secrets/{secretId}"
            .replaceAll("\\{workspaceId}", apiClient.escapeString(workspaceId.toString()))
            .replaceAll("\\{secretId}", apiClient.escapeString(secretId.toString()));

    String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/problem+json");
    String localVarContentType = apiClient.selectHeaderContentType();
    String[] localVarAuthNames = new String[] {"BearerAuth"};
    GenericType<PipelineSecretReadModel> localVarReturnType = new GenericType<PipelineSecretReadModel>() {};
    return apiClient.invokeAPI("PipelineSecretsApi.getPipelineSecret", localVarPath, "GET", new ArrayList<>(), null,
                               new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept, localVarContentType,
                               localVarAuthNames, localVarReturnType, false);
  }
  /**
   * List pipeline secrets
   * Lists the pipeline secrets of the workspace.  **Required permission:** &#x60;pipeline_secrets:read&#x60;
   * @param workspaceId  (required)
   * @param pageToken Continuation token taken from the &#x60;nextPageToken&#x60; of a prior response. Omit it to retrieve the first page. A malformed token is rejected. (optional)
   * @param pageSize Maximum number of secrets to return in the page. Out-of-range values are rejected. (optional, default to 50)
   * @return PipelineSecretPage
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
  public PipelineSecretPage listPipelineSecrets(@javax.annotation.Nonnull String workspaceId, @javax.annotation.Nullable String pageToken, @javax.annotation.Nullable Integer pageSize) throws ApiException {
    return listPipelineSecretsWithHttpInfo(workspaceId, pageToken, pageSize).getData();
  }

  /**
   * List pipeline secrets
   * Lists the pipeline secrets of the workspace.  **Required permission:** &#x60;pipeline_secrets:read&#x60;
   * @param workspaceId  (required)
   * @param pageToken Continuation token taken from the &#x60;nextPageToken&#x60; of a prior response. Omit it to retrieve the first page. A malformed token is rejected. (optional)
   * @param pageSize Maximum number of secrets to return in the page. Out-of-range values are rejected. (optional, default to 50)
   * @return ApiResponse&lt;PipelineSecretPage&gt;
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
  public ApiResponse<PipelineSecretPage> listPipelineSecretsWithHttpInfo(@javax.annotation.Nonnull String workspaceId, @javax.annotation.Nullable String pageToken, @javax.annotation.Nullable Integer pageSize) throws ApiException {
    // Check required parameters
    if (workspaceId == null) {
      throw new ApiException(400, "Missing the required parameter 'workspaceId' when calling listPipelineSecrets");
    }

    // Path parameters
    String localVarPath = "/v2/workspaces/{workspaceId}/pipeline-secrets"
            .replaceAll("\\{workspaceId}", apiClient.escapeString(workspaceId.toString()));

    // Query parameters
    List<Pair> localVarQueryParams = new ArrayList<>(
            apiClient.parameterToPairs("", "pageToken", pageToken)
    );
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "pageSize", pageSize));

    String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/problem+json");
    String localVarContentType = apiClient.selectHeaderContentType();
    String[] localVarAuthNames = new String[] {"BearerAuth"};
    GenericType<PipelineSecretPage> localVarReturnType = new GenericType<PipelineSecretPage>() {};
    return apiClient.invokeAPI("PipelineSecretsApi.listPipelineSecrets", localVarPath, "GET", localVarQueryParams, null,
                               new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept, localVarContentType,
                               localVarAuthNames, localVarReturnType, false);
  }
  /**
   * Update pipeline secret
   * Updates the value of the pipeline secret.  **Required permission:** &#x60;pipeline_secrets:write&#x60;
   * @param workspaceId  (required)
   * @param secretId Pipeline secret identifier. (required)
   * @param pipelineSecretPatchModel  (required)
   * @param updateMask Names of the fields to write, as a comma-separated list. When given, exactly those fields are written; &#x60;value&#x60; is the only field this operation can write, so a mask naming anything else is rejected with &#x60;400&#x60;. When omitted, the mask is inferred from the request body, where a field carrying a value joins it and one carried as &#x60;null&#x60; or omitted stays out. (optional)
   * @param validateOnly Validates the request without updating anything. When &#x60;true&#x60;, the server runs the same validation it would for a real request and reports what the result would be, but persists nothing and triggers no side effects. Validation failures surface as they would otherwise. Defaults to &#x60;false&#x60;. (optional, default to false)
   * @return PipelineSecretReadModel
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
  public PipelineSecretReadModel updatePipelineSecret(@javax.annotation.Nonnull String workspaceId, @javax.annotation.Nonnull String secretId, @javax.annotation.Nonnull PipelineSecretPatchModel pipelineSecretPatchModel, @javax.annotation.Nullable List<String> updateMask, @javax.annotation.Nullable Boolean validateOnly) throws ApiException {
    return updatePipelineSecretWithHttpInfo(workspaceId, secretId, pipelineSecretPatchModel, updateMask, validateOnly).getData();
  }

  /**
   * Update pipeline secret
   * Updates the value of the pipeline secret.  **Required permission:** &#x60;pipeline_secrets:write&#x60;
   * @param workspaceId  (required)
   * @param secretId Pipeline secret identifier. (required)
   * @param pipelineSecretPatchModel  (required)
   * @param updateMask Names of the fields to write, as a comma-separated list. When given, exactly those fields are written; &#x60;value&#x60; is the only field this operation can write, so a mask naming anything else is rejected with &#x60;400&#x60;. When omitted, the mask is inferred from the request body, where a field carrying a value joins it and one carried as &#x60;null&#x60; or omitted stays out. (optional)
   * @param validateOnly Validates the request without updating anything. When &#x60;true&#x60;, the server runs the same validation it would for a real request and reports what the result would be, but persists nothing and triggers no side effects. Validation failures surface as they would otherwise. Defaults to &#x60;false&#x60;. (optional, default to false)
   * @return ApiResponse&lt;PipelineSecretReadModel&gt;
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
  public ApiResponse<PipelineSecretReadModel> updatePipelineSecretWithHttpInfo(@javax.annotation.Nonnull String workspaceId, @javax.annotation.Nonnull String secretId, @javax.annotation.Nonnull PipelineSecretPatchModel pipelineSecretPatchModel, @javax.annotation.Nullable List<String> updateMask, @javax.annotation.Nullable Boolean validateOnly) throws ApiException {
    // Check required parameters
    if (workspaceId == null) {
      throw new ApiException(400, "Missing the required parameter 'workspaceId' when calling updatePipelineSecret");
    }
    if (secretId == null) {
      throw new ApiException(400, "Missing the required parameter 'secretId' when calling updatePipelineSecret");
    }
    if (pipelineSecretPatchModel == null) {
      throw new ApiException(400, "Missing the required parameter 'pipelineSecretPatchModel' when calling updatePipelineSecret");
    }

    // Path parameters
    String localVarPath = "/v2/workspaces/{workspaceId}/pipeline-secrets/{secretId}"
            .replaceAll("\\{workspaceId}", apiClient.escapeString(workspaceId.toString()))
            .replaceAll("\\{secretId}", apiClient.escapeString(secretId.toString()));

    // Query parameters
    List<Pair> localVarQueryParams = new ArrayList<>(
            apiClient.parameterToPairs("csv", "updateMask", updateMask)
    );
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "validateOnly", validateOnly));

    String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/problem+json");
    String localVarContentType = apiClient.selectHeaderContentType("application/json");
    String[] localVarAuthNames = new String[] {"BearerAuth"};
    GenericType<PipelineSecretReadModel> localVarReturnType = new GenericType<PipelineSecretReadModel>() {};
    return apiClient.invokeAPI("PipelineSecretsApi.updatePipelineSecret", localVarPath, "PATCH", localVarQueryParams, pipelineSecretPatchModel,
                               new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept, localVarContentType,
                               localVarAuthNames, localVarReturnType, false);
  }
}
