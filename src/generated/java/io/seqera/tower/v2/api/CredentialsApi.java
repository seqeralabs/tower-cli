package io.seqera.tower.v2.api;

import io.seqera.tower.v2.ApiException;
import io.seqera.tower.v2.ApiClient;
import io.seqera.tower.v2.ApiResponse;
import io.seqera.tower.v2.Configuration;
import io.seqera.tower.v2.Pair;

import javax.ws.rs.core.GenericType;

import io.seqera.tower.v2.model.CredentialsPage;
import io.seqera.tower.v2.model.CredentialsPatchModel;
import io.seqera.tower.v2.model.CredentialsReadModel;
import io.seqera.tower.v2.model.CredentialsWriteModel;
import io.seqera.tower.v2.model.EncryptedCredentialsKeysReadModel;
import io.seqera.tower.v2.model.ErrorDetail;
import io.seqera.tower.v2.model.ErrorDetailWithConflicts;
import io.seqera.tower.v2.model.SeqeraComputeCredentialsWriteModel;
import io.seqera.tower.v2.model.ValidateCredentialsResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.17.0")
public class CredentialsApi {
  private ApiClient apiClient;

  public CredentialsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public CredentialsApi(ApiClient apiClient) {
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
   * Create credentials
   * Creates credentials in the workspace.  **Required permission:** &#x60;credentials:write&#x60;
   * @param workspaceId  (required)
   * @param credentialsWriteModel  (required)
   * @param validateOnly Validates the request without creating anything. When &#x60;true&#x60;, the server runs the same validation it would for a real request and reports what the result would be, but persists nothing and triggers no side effects, so the fields the server populates on creation may be missing. Validation failures surface as they would otherwise. Not to be confused with the &#x60;/validate&#x60; operation, which checks the credentials against their provider. Defaults to &#x60;false&#x60;. (optional, default to false)
   * @return CredentialsReadModel
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 201 </td><td> The request has succeeded and a new resource has been created as a result. </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Malformed or invalid request. </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Missing or invalid authentication. </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Missing required permission. </td><td>  -  </td></tr>
       <tr><td> 409 </td><td> Another set of credentials in the workspace already goes by that name, or, for SCM credentials, already carries that &#x60;baseUrl&#x60;. </td><td>  -  </td></tr>
     </table>
   */
  public CredentialsReadModel createCredentials(@javax.annotation.Nonnull String workspaceId, @javax.annotation.Nonnull CredentialsWriteModel credentialsWriteModel, @javax.annotation.Nullable Boolean validateOnly) throws ApiException {
    return createCredentialsWithHttpInfo(workspaceId, credentialsWriteModel, validateOnly).getData();
  }

  /**
   * Create credentials
   * Creates credentials in the workspace.  **Required permission:** &#x60;credentials:write&#x60;
   * @param workspaceId  (required)
   * @param credentialsWriteModel  (required)
   * @param validateOnly Validates the request without creating anything. When &#x60;true&#x60;, the server runs the same validation it would for a real request and reports what the result would be, but persists nothing and triggers no side effects, so the fields the server populates on creation may be missing. Validation failures surface as they would otherwise. Not to be confused with the &#x60;/validate&#x60; operation, which checks the credentials against their provider. Defaults to &#x60;false&#x60;. (optional, default to false)
   * @return ApiResponse&lt;CredentialsReadModel&gt;
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 201 </td><td> The request has succeeded and a new resource has been created as a result. </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Malformed or invalid request. </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Missing or invalid authentication. </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Missing required permission. </td><td>  -  </td></tr>
       <tr><td> 409 </td><td> Another set of credentials in the workspace already goes by that name, or, for SCM credentials, already carries that &#x60;baseUrl&#x60;. </td><td>  -  </td></tr>
     </table>
   */
  public ApiResponse<CredentialsReadModel> createCredentialsWithHttpInfo(@javax.annotation.Nonnull String workspaceId, @javax.annotation.Nonnull CredentialsWriteModel credentialsWriteModel, @javax.annotation.Nullable Boolean validateOnly) throws ApiException {
    // Check required parameters
    if (workspaceId == null) {
      throw new ApiException(400, "Missing the required parameter 'workspaceId' when calling createCredentials");
    }
    if (credentialsWriteModel == null) {
      throw new ApiException(400, "Missing the required parameter 'credentialsWriteModel' when calling createCredentials");
    }

    // Path parameters
    String localVarPath = "/v2/workspaces/{workspaceId}/credentials"
            .replaceAll("\\{workspaceId}", apiClient.escapeString(workspaceId.toString()));

    // Query parameters
    List<Pair> localVarQueryParams = new ArrayList<>(
            apiClient.parameterToPairs("", "validateOnly", validateOnly)
    );

    String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/problem+json");
    String localVarContentType = apiClient.selectHeaderContentType("application/json");
    String[] localVarAuthNames = new String[] {"BearerAuth"};
    GenericType<CredentialsReadModel> localVarReturnType = new GenericType<CredentialsReadModel>() {};
    return apiClient.invokeAPI("CredentialsApi.createCredentials", localVarPath, "POST", localVarQueryParams, credentialsWriteModel,
                               new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept, localVarContentType,
                               localVarAuthNames, localVarReturnType, false);
  }
  /**
   * Create Seqera Compute credentials
   * Creates the Seqera Compute credentials of the workspace.  **Required permission:** &#x60;root&#x60;
   * @param workspaceId  (required)
   * @param seqeraComputeCredentialsWriteModel  (required)
   * @param validateOnly Validates the request without creating anything. When &#x60;true&#x60;, the server runs the same validation it would for a real request and reports what the result would be, but persists nothing and triggers no side effects, so the fields the server populates on creation may be missing. Validation failures surface as they would otherwise. Not to be confused with the &#x60;/validate&#x60; operation, which checks the credentials against their provider. Defaults to &#x60;false&#x60;. (optional, default to false)
   * @return CredentialsReadModel
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 201 </td><td> The request has succeeded and a new resource has been created as a result. </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Malformed or invalid request. </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Missing or invalid authentication. </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Missing required permission. </td><td>  -  </td></tr>
       <tr><td> 409 </td><td> The workspace already holds a set of Seqera Compute credentials. </td><td>  -  </td></tr>
     </table>
   */
  public CredentialsReadModel createSeqeraComputeCredentials(@javax.annotation.Nonnull String workspaceId, @javax.annotation.Nonnull SeqeraComputeCredentialsWriteModel seqeraComputeCredentialsWriteModel, @javax.annotation.Nullable Boolean validateOnly) throws ApiException {
    return createSeqeraComputeCredentialsWithHttpInfo(workspaceId, seqeraComputeCredentialsWriteModel, validateOnly).getData();
  }

  /**
   * Create Seqera Compute credentials
   * Creates the Seqera Compute credentials of the workspace.  **Required permission:** &#x60;root&#x60;
   * @param workspaceId  (required)
   * @param seqeraComputeCredentialsWriteModel  (required)
   * @param validateOnly Validates the request without creating anything. When &#x60;true&#x60;, the server runs the same validation it would for a real request and reports what the result would be, but persists nothing and triggers no side effects, so the fields the server populates on creation may be missing. Validation failures surface as they would otherwise. Not to be confused with the &#x60;/validate&#x60; operation, which checks the credentials against their provider. Defaults to &#x60;false&#x60;. (optional, default to false)
   * @return ApiResponse&lt;CredentialsReadModel&gt;
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 201 </td><td> The request has succeeded and a new resource has been created as a result. </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Malformed or invalid request. </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Missing or invalid authentication. </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Missing required permission. </td><td>  -  </td></tr>
       <tr><td> 409 </td><td> The workspace already holds a set of Seqera Compute credentials. </td><td>  -  </td></tr>
     </table>
   */
  public ApiResponse<CredentialsReadModel> createSeqeraComputeCredentialsWithHttpInfo(@javax.annotation.Nonnull String workspaceId, @javax.annotation.Nonnull SeqeraComputeCredentialsWriteModel seqeraComputeCredentialsWriteModel, @javax.annotation.Nullable Boolean validateOnly) throws ApiException {
    // Check required parameters
    if (workspaceId == null) {
      throw new ApiException(400, "Missing the required parameter 'workspaceId' when calling createSeqeraComputeCredentials");
    }
    if (seqeraComputeCredentialsWriteModel == null) {
      throw new ApiException(400, "Missing the required parameter 'seqeraComputeCredentialsWriteModel' when calling createSeqeraComputeCredentials");
    }

    // Path parameters
    String localVarPath = "/v2/workspaces/{workspaceId}/seqera-compute-credentials"
            .replaceAll("\\{workspaceId}", apiClient.escapeString(workspaceId.toString()));

    // Query parameters
    List<Pair> localVarQueryParams = new ArrayList<>(
            apiClient.parameterToPairs("", "validateOnly", validateOnly)
    );

    String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/problem+json");
    String localVarContentType = apiClient.selectHeaderContentType("application/json");
    String[] localVarAuthNames = new String[] {"BearerAuth"};
    GenericType<CredentialsReadModel> localVarReturnType = new GenericType<CredentialsReadModel>() {};
    return apiClient.invokeAPI("CredentialsApi.createSeqeraComputeCredentials", localVarPath, "POST", localVarQueryParams, seqeraComputeCredentialsWriteModel,
                               new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept, localVarContentType,
                               localVarAuthNames, localVarReturnType, false);
  }
  /**
   * Delete credentials
   * Deletes the credentials.  Compute environments and data links that authenticate through the credentials are invalidated, since they can no longer reach their provider.  **Required permission:** &#x60;credentials:delete&#x60;
   * @param workspaceId Workspace identifier. (required)
   * @param credentialsId Credentials identifier. (required)
   * @param force Deletes the credentials even when workflow runs or studio sessions are still running against them, stopping those jobs. When &#x60;false&#x60;, a running job blocks the deletion and the request is rejected instead, so the caller can decide whether to stop the jobs. Defaults to &#x60;false&#x60;. (optional, default to false)
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 204 </td><td> There is no content to send for this request, but the headers may be useful.  </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Malformed or invalid request. </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Missing or invalid authentication. </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Missing required permission. </td><td>  -  </td></tr>
       <tr><td> 409 </td><td> Jobs are still running against the credentials, and the deletion did not force to stop them. </td><td>  -  </td></tr>
     </table>
   */
  public void deleteCredentials(@javax.annotation.Nonnull String workspaceId, @javax.annotation.Nonnull String credentialsId, @javax.annotation.Nullable Boolean force) throws ApiException {
    deleteCredentialsWithHttpInfo(workspaceId, credentialsId, force);
  }

  /**
   * Delete credentials
   * Deletes the credentials.  Compute environments and data links that authenticate through the credentials are invalidated, since they can no longer reach their provider.  **Required permission:** &#x60;credentials:delete&#x60;
   * @param workspaceId Workspace identifier. (required)
   * @param credentialsId Credentials identifier. (required)
   * @param force Deletes the credentials even when workflow runs or studio sessions are still running against them, stopping those jobs. When &#x60;false&#x60;, a running job blocks the deletion and the request is rejected instead, so the caller can decide whether to stop the jobs. Defaults to &#x60;false&#x60;. (optional, default to false)
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
       <tr><td> 409 </td><td> Jobs are still running against the credentials, and the deletion did not force to stop them. </td><td>  -  </td></tr>
     </table>
   */
  public ApiResponse<Void> deleteCredentialsWithHttpInfo(@javax.annotation.Nonnull String workspaceId, @javax.annotation.Nonnull String credentialsId, @javax.annotation.Nullable Boolean force) throws ApiException {
    // Check required parameters
    if (workspaceId == null) {
      throw new ApiException(400, "Missing the required parameter 'workspaceId' when calling deleteCredentials");
    }
    if (credentialsId == null) {
      throw new ApiException(400, "Missing the required parameter 'credentialsId' when calling deleteCredentials");
    }

    // Path parameters
    String localVarPath = "/v2/workspaces/{workspaceId}/credentials/{credentialsId}"
            .replaceAll("\\{workspaceId}", apiClient.escapeString(workspaceId.toString()))
            .replaceAll("\\{credentialsId}", apiClient.escapeString(credentialsId.toString()));

    // Query parameters
    List<Pair> localVarQueryParams = new ArrayList<>(
            apiClient.parameterToPairs("", "force", force)
    );

    String localVarAccept = apiClient.selectHeaderAccept("application/problem+json", "application/json");
    String localVarContentType = apiClient.selectHeaderContentType();
    String[] localVarAuthNames = new String[] {"BearerAuth"};
    return apiClient.invokeAPI("CredentialsApi.deleteCredentials", localVarPath, "DELETE", localVarQueryParams, null,
                               new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept, localVarContentType,
                               localVarAuthNames, null, false);
  }
  /**
   * Get credentials
   * Retrieves the credentials.  **Required permission:** &#x60;credentials:read&#x60;
   * @param workspaceId  (required)
   * @param credentialsId  (required)
   * @return CredentialsReadModel
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 200 </td><td> The request has succeeded. </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Missing or invalid authentication. </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Missing required permission. </td><td>  -  </td></tr>
     </table>
   */
  public CredentialsReadModel getCredentials(@javax.annotation.Nonnull String workspaceId, @javax.annotation.Nonnull String credentialsId) throws ApiException {
    return getCredentialsWithHttpInfo(workspaceId, credentialsId).getData();
  }

  /**
   * Get credentials
   * Retrieves the credentials.  **Required permission:** &#x60;credentials:read&#x60;
   * @param workspaceId  (required)
   * @param credentialsId  (required)
   * @return ApiResponse&lt;CredentialsReadModel&gt;
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 200 </td><td> The request has succeeded. </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Missing or invalid authentication. </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Missing required permission. </td><td>  -  </td></tr>
     </table>
   */
  public ApiResponse<CredentialsReadModel> getCredentialsWithHttpInfo(@javax.annotation.Nonnull String workspaceId, @javax.annotation.Nonnull String credentialsId) throws ApiException {
    // Check required parameters
    if (workspaceId == null) {
      throw new ApiException(400, "Missing the required parameter 'workspaceId' when calling getCredentials");
    }
    if (credentialsId == null) {
      throw new ApiException(400, "Missing the required parameter 'credentialsId' when calling getCredentials");
    }

    // Path parameters
    String localVarPath = "/v2/workspaces/{workspaceId}/credentials/{credentialsId}"
            .replaceAll("\\{workspaceId}", apiClient.escapeString(workspaceId.toString()))
            .replaceAll("\\{credentialsId}", apiClient.escapeString(credentialsId.toString()));

    String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/problem+json");
    String localVarContentType = apiClient.selectHeaderContentType();
    String[] localVarAuthNames = new String[] {"BearerAuth"};
    GenericType<CredentialsReadModel> localVarReturnType = new GenericType<CredentialsReadModel>() {};
    return apiClient.invokeAPI("CredentialsApi.getCredentials", localVarPath, "GET", new ArrayList<>(), null,
                               new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept, localVarContentType,
                               localVarAuthNames, localVarReturnType, false);
  }
  /**
   * Get encrypted credentials keys
   * Retrieves the secret keys of the credentials, encrypted with the public key of the pairing identified by &#x60;pairingId&#x60;, so that only the holder of its private key can read them.  **Required permission:** &#x60;credentials_encrypted:read&#x60;
   * @param workspaceId Workspace identifier. (required)
   * @param credentialsId Credentials identifier. (required)
   * @param pairingId Identifier of the pairing whose public key the keys are encrypted with. (required)
   * @return EncryptedCredentialsKeysReadModel
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 200 </td><td> The request has succeeded. </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Malformed or invalid request. </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Missing or invalid authentication. </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Missing required permission. </td><td>  -  </td></tr>
       <tr><td> 404 </td><td> No pairing goes by that &#x60;pairingId&#x60;, or it has expired. </td><td>  -  </td></tr>
     </table>
   */
  public EncryptedCredentialsKeysReadModel getEncryptedCredentialsKeys(@javax.annotation.Nonnull String workspaceId, @javax.annotation.Nonnull String credentialsId, @javax.annotation.Nonnull String pairingId) throws ApiException {
    return getEncryptedCredentialsKeysWithHttpInfo(workspaceId, credentialsId, pairingId).getData();
  }

  /**
   * Get encrypted credentials keys
   * Retrieves the secret keys of the credentials, encrypted with the public key of the pairing identified by &#x60;pairingId&#x60;, so that only the holder of its private key can read them.  **Required permission:** &#x60;credentials_encrypted:read&#x60;
   * @param workspaceId Workspace identifier. (required)
   * @param credentialsId Credentials identifier. (required)
   * @param pairingId Identifier of the pairing whose public key the keys are encrypted with. (required)
   * @return ApiResponse&lt;EncryptedCredentialsKeysReadModel&gt;
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 200 </td><td> The request has succeeded. </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Malformed or invalid request. </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Missing or invalid authentication. </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Missing required permission. </td><td>  -  </td></tr>
       <tr><td> 404 </td><td> No pairing goes by that &#x60;pairingId&#x60;, or it has expired. </td><td>  -  </td></tr>
     </table>
   */
  public ApiResponse<EncryptedCredentialsKeysReadModel> getEncryptedCredentialsKeysWithHttpInfo(@javax.annotation.Nonnull String workspaceId, @javax.annotation.Nonnull String credentialsId, @javax.annotation.Nonnull String pairingId) throws ApiException {
    // Check required parameters
    if (workspaceId == null) {
      throw new ApiException(400, "Missing the required parameter 'workspaceId' when calling getEncryptedCredentialsKeys");
    }
    if (credentialsId == null) {
      throw new ApiException(400, "Missing the required parameter 'credentialsId' when calling getEncryptedCredentialsKeys");
    }
    if (pairingId == null) {
      throw new ApiException(400, "Missing the required parameter 'pairingId' when calling getEncryptedCredentialsKeys");
    }

    // Path parameters
    String localVarPath = "/v2/workspaces/{workspaceId}/credentials/{credentialsId}/encrypted-keys"
            .replaceAll("\\{workspaceId}", apiClient.escapeString(workspaceId.toString()))
            .replaceAll("\\{credentialsId}", apiClient.escapeString(credentialsId.toString()));

    // Query parameters
    List<Pair> localVarQueryParams = new ArrayList<>(
            apiClient.parameterToPairs("", "pairingId", pairingId)
    );

    String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/problem+json");
    String localVarContentType = apiClient.selectHeaderContentType();
    String[] localVarAuthNames = new String[] {"BearerAuth"};
    GenericType<EncryptedCredentialsKeysReadModel> localVarReturnType = new GenericType<EncryptedCredentialsKeysReadModel>() {};
    return apiClient.invokeAPI("CredentialsApi.getEncryptedCredentialsKeys", localVarPath, "GET", localVarQueryParams, null,
                               new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept, localVarContentType,
                               localVarAuthNames, localVarReturnType, false);
  }
  /**
   * List credentials
   * Lists the credentials of the workspace, ordered by creation date, newest first.  **Required permission:** &#x60;credentials:read&#x60;
   * @param workspaceId  (required)
   * @param pageToken Continuation token taken from the &#x60;nextPageToken&#x60; of a prior response. Omit it to retrieve the first page. A malformed token is rejected. (optional)
   * @param pageSize Maximum number of credentials to return in the page. Out-of-range values are rejected. (optional, default to 50)
   * @param filter Filter expression narrowing the page to the credentials it matches.  The filterable fields are the following:  - &#x60;keys.provider&#x60;: its values are the ones the &#x60;keys&#x60; variants are discriminated by, such as &#x60;AWS&#x60;, &#x60;AZURE_CLOUD&#x60;, or &#x60;S3&#x60;. It supports equality, membership, and disjunctions of them: &#x60;keys.provider &#x3D;&#x3D; \&quot;AWS\&quot;&#x60;, &#x60;keys.provider in [\&quot;AWS\&quot;, \&quot;S3\&quot;]&#x60;, &#x60;keys.provider &#x3D;&#x3D; \&quot;AWS\&quot; || keys.provider &#x3D;&#x3D; \&quot;S3\&quot;&#x60;.  Malformed expressions are rejected. (optional)
   * @return CredentialsPage
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
  public CredentialsPage listCredentials(@javax.annotation.Nonnull String workspaceId, @javax.annotation.Nullable String pageToken, @javax.annotation.Nullable Integer pageSize, @javax.annotation.Nullable String filter) throws ApiException {
    return listCredentialsWithHttpInfo(workspaceId, pageToken, pageSize, filter).getData();
  }

  /**
   * List credentials
   * Lists the credentials of the workspace, ordered by creation date, newest first.  **Required permission:** &#x60;credentials:read&#x60;
   * @param workspaceId  (required)
   * @param pageToken Continuation token taken from the &#x60;nextPageToken&#x60; of a prior response. Omit it to retrieve the first page. A malformed token is rejected. (optional)
   * @param pageSize Maximum number of credentials to return in the page. Out-of-range values are rejected. (optional, default to 50)
   * @param filter Filter expression narrowing the page to the credentials it matches.  The filterable fields are the following:  - &#x60;keys.provider&#x60;: its values are the ones the &#x60;keys&#x60; variants are discriminated by, such as &#x60;AWS&#x60;, &#x60;AZURE_CLOUD&#x60;, or &#x60;S3&#x60;. It supports equality, membership, and disjunctions of them: &#x60;keys.provider &#x3D;&#x3D; \&quot;AWS\&quot;&#x60;, &#x60;keys.provider in [\&quot;AWS\&quot;, \&quot;S3\&quot;]&#x60;, &#x60;keys.provider &#x3D;&#x3D; \&quot;AWS\&quot; || keys.provider &#x3D;&#x3D; \&quot;S3\&quot;&#x60;.  Malformed expressions are rejected. (optional)
   * @return ApiResponse&lt;CredentialsPage&gt;
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
  public ApiResponse<CredentialsPage> listCredentialsWithHttpInfo(@javax.annotation.Nonnull String workspaceId, @javax.annotation.Nullable String pageToken, @javax.annotation.Nullable Integer pageSize, @javax.annotation.Nullable String filter) throws ApiException {
    // Check required parameters
    if (workspaceId == null) {
      throw new ApiException(400, "Missing the required parameter 'workspaceId' when calling listCredentials");
    }

    // Path parameters
    String localVarPath = "/v2/workspaces/{workspaceId}/credentials"
            .replaceAll("\\{workspaceId}", apiClient.escapeString(workspaceId.toString()));

    // Query parameters
    List<Pair> localVarQueryParams = new ArrayList<>(
            apiClient.parameterToPairs("", "pageToken", pageToken)
    );
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "pageSize", pageSize));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));

    String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/problem+json");
    String localVarContentType = apiClient.selectHeaderContentType();
    String[] localVarAuthNames = new String[] {"BearerAuth"};
    GenericType<CredentialsPage> localVarReturnType = new GenericType<CredentialsPage>() {};
    return apiClient.invokeAPI("CredentialsApi.listCredentials", localVarPath, "GET", localVarQueryParams, null,
                               new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept, localVarContentType,
                               localVarAuthNames, localVarReturnType, false);
  }
  /**
   * Update credentials
   * Updates the credentials.  **Required permission:** &#x60;credentials:write&#x60;
   * @param workspaceId  (required)
   * @param credentialsId  (required)
   * @param credentialsPatchModel  (required)
   * @param updateMask Names of the fields to write, as a comma-separated list. When given, exactly those fields are written. When omitted, the mask is inferred from the request body, where a field carrying a value joins it and one carried as &#x60;null&#x60; or omitted stays out. (optional)
   * @param validateOnly Validates the request without updating anything. When &#x60;true&#x60;, the server runs the same validation it would for a real request and reports what the result would be, but persists nothing and triggers no side effects. Validation failures surface as they would otherwise. Not to be confused with the &#x60;/validate&#x60; operation, which checks the credentials against their provider. Defaults to &#x60;false&#x60;. (optional, default to false)
   * @return CredentialsReadModel
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 200 </td><td> The request has succeeded. </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Malformed or invalid request. </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Missing or invalid authentication. </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Missing required permission. </td><td>  -  </td></tr>
       <tr><td> 409 </td><td> Another set of SCM credentials in the workspace already carries that &#x60;baseUrl&#x60;. </td><td>  -  </td></tr>
     </table>
   */
  public CredentialsReadModel updateCredentials(@javax.annotation.Nonnull String workspaceId, @javax.annotation.Nonnull String credentialsId, @javax.annotation.Nonnull CredentialsPatchModel credentialsPatchModel, @javax.annotation.Nullable List<String> updateMask, @javax.annotation.Nullable Boolean validateOnly) throws ApiException {
    return updateCredentialsWithHttpInfo(workspaceId, credentialsId, credentialsPatchModel, updateMask, validateOnly).getData();
  }

  /**
   * Update credentials
   * Updates the credentials.  **Required permission:** &#x60;credentials:write&#x60;
   * @param workspaceId  (required)
   * @param credentialsId  (required)
   * @param credentialsPatchModel  (required)
   * @param updateMask Names of the fields to write, as a comma-separated list. When given, exactly those fields are written. When omitted, the mask is inferred from the request body, where a field carrying a value joins it and one carried as &#x60;null&#x60; or omitted stays out. (optional)
   * @param validateOnly Validates the request without updating anything. When &#x60;true&#x60;, the server runs the same validation it would for a real request and reports what the result would be, but persists nothing and triggers no side effects. Validation failures surface as they would otherwise. Not to be confused with the &#x60;/validate&#x60; operation, which checks the credentials against their provider. Defaults to &#x60;false&#x60;. (optional, default to false)
   * @return ApiResponse&lt;CredentialsReadModel&gt;
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 200 </td><td> The request has succeeded. </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Malformed or invalid request. </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Missing or invalid authentication. </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Missing required permission. </td><td>  -  </td></tr>
       <tr><td> 409 </td><td> Another set of SCM credentials in the workspace already carries that &#x60;baseUrl&#x60;. </td><td>  -  </td></tr>
     </table>
   */
  public ApiResponse<CredentialsReadModel> updateCredentialsWithHttpInfo(@javax.annotation.Nonnull String workspaceId, @javax.annotation.Nonnull String credentialsId, @javax.annotation.Nonnull CredentialsPatchModel credentialsPatchModel, @javax.annotation.Nullable List<String> updateMask, @javax.annotation.Nullable Boolean validateOnly) throws ApiException {
    // Check required parameters
    if (workspaceId == null) {
      throw new ApiException(400, "Missing the required parameter 'workspaceId' when calling updateCredentials");
    }
    if (credentialsId == null) {
      throw new ApiException(400, "Missing the required parameter 'credentialsId' when calling updateCredentials");
    }
    if (credentialsPatchModel == null) {
      throw new ApiException(400, "Missing the required parameter 'credentialsPatchModel' when calling updateCredentials");
    }

    // Path parameters
    String localVarPath = "/v2/workspaces/{workspaceId}/credentials/{credentialsId}"
            .replaceAll("\\{workspaceId}", apiClient.escapeString(workspaceId.toString()))
            .replaceAll("\\{credentialsId}", apiClient.escapeString(credentialsId.toString()));

    // Query parameters
    List<Pair> localVarQueryParams = new ArrayList<>(
            apiClient.parameterToPairs("csv", "updateMask", updateMask)
    );
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "validateOnly", validateOnly));

    String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/problem+json");
    String localVarContentType = apiClient.selectHeaderContentType("application/json");
    String[] localVarAuthNames = new String[] {"BearerAuth"};
    GenericType<CredentialsReadModel> localVarReturnType = new GenericType<CredentialsReadModel>() {};
    return apiClient.invokeAPI("CredentialsApi.updateCredentials", localVarPath, "PATCH", localVarQueryParams, credentialsPatchModel,
                               new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept, localVarContentType,
                               localVarAuthNames, localVarReturnType, false);
  }
  /**
   * Validate credentials
   * Validates the credentials against their cloud provider and reports the outcome.  **Required permission:** &#x60;credentials:write&#x60;
   * @param workspaceId Workspace identifier. (required)
   * @param credentialsId Credentials identifier. (required)
   * @param force Skips the provider check and marks the credentials &#x60;AVAILABLE&#x60;, to recover credentials left &#x60;INVALID&#x60; that are known to be good. Credentials carrying any other status are rejected instead. Defaults to &#x60;false&#x60;. (optional, default to false)
   * @return ValidateCredentialsResponse
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 200 </td><td> The request has succeeded. </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Malformed or invalid request. </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Missing or invalid authentication. </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Missing required permission. </td><td>  -  </td></tr>
       <tr><td> 409 </td><td> The credentials are not &#x60;INVALID&#x60;, so their status cannot be forced to &#x60;AVAILABLE&#x60;. </td><td>  -  </td></tr>
     </table>
   */
  public ValidateCredentialsResponse validateCredentials(@javax.annotation.Nonnull String workspaceId, @javax.annotation.Nonnull String credentialsId, @javax.annotation.Nullable Boolean force) throws ApiException {
    return validateCredentialsWithHttpInfo(workspaceId, credentialsId, force).getData();
  }

  /**
   * Validate credentials
   * Validates the credentials against their cloud provider and reports the outcome.  **Required permission:** &#x60;credentials:write&#x60;
   * @param workspaceId Workspace identifier. (required)
   * @param credentialsId Credentials identifier. (required)
   * @param force Skips the provider check and marks the credentials &#x60;AVAILABLE&#x60;, to recover credentials left &#x60;INVALID&#x60; that are known to be good. Credentials carrying any other status are rejected instead. Defaults to &#x60;false&#x60;. (optional, default to false)
   * @return ApiResponse&lt;ValidateCredentialsResponse&gt;
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 200 </td><td> The request has succeeded. </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Malformed or invalid request. </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Missing or invalid authentication. </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Missing required permission. </td><td>  -  </td></tr>
       <tr><td> 409 </td><td> The credentials are not &#x60;INVALID&#x60;, so their status cannot be forced to &#x60;AVAILABLE&#x60;. </td><td>  -  </td></tr>
     </table>
   */
  public ApiResponse<ValidateCredentialsResponse> validateCredentialsWithHttpInfo(@javax.annotation.Nonnull String workspaceId, @javax.annotation.Nonnull String credentialsId, @javax.annotation.Nullable Boolean force) throws ApiException {
    // Check required parameters
    if (workspaceId == null) {
      throw new ApiException(400, "Missing the required parameter 'workspaceId' when calling validateCredentials");
    }
    if (credentialsId == null) {
      throw new ApiException(400, "Missing the required parameter 'credentialsId' when calling validateCredentials");
    }

    // Path parameters
    String localVarPath = "/v2/workspaces/{workspaceId}/credentials/{credentialsId}/validate"
            .replaceAll("\\{workspaceId}", apiClient.escapeString(workspaceId.toString()))
            .replaceAll("\\{credentialsId}", apiClient.escapeString(credentialsId.toString()));

    // Query parameters
    List<Pair> localVarQueryParams = new ArrayList<>(
            apiClient.parameterToPairs("", "force", force)
    );

    String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/problem+json");
    String localVarContentType = apiClient.selectHeaderContentType();
    String[] localVarAuthNames = new String[] {"BearerAuth"};
    GenericType<ValidateCredentialsResponse> localVarReturnType = new GenericType<ValidateCredentialsResponse>() {};
    return apiClient.invokeAPI("CredentialsApi.validateCredentials", localVarPath, "POST", localVarQueryParams, null,
                               new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept, localVarContentType,
                               localVarAuthNames, localVarReturnType, false);
  }
}
