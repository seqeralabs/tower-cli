package io.seqera.tower.v2.api;

import io.seqera.tower.v2.ApiException;
import io.seqera.tower.v2.ApiClient;
import io.seqera.tower.v2.ApiResponse;
import io.seqera.tower.v2.Configuration;
import io.seqera.tower.v2.Pair;

import javax.ws.rs.core.GenericType;

import io.seqera.tower.v2.model.ServiceInfoResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.17.0")
public class ServiceInfoApi {
  private ApiClient apiClient;

  public ServiceInfoApi() {
    this(Configuration.getDefaultApiClient());
  }

  public ServiceInfoApi(ApiClient apiClient) {
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
   * Get service info
   * Returns version and build metadata for the API.  **Required permission:** _none (public)_
   * @return ServiceInfoResponse
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 200 </td><td> The request has succeeded. </td><td>  -  </td></tr>
     </table>
   */
  public ServiceInfoResponse infoV2() throws ApiException {
    return infoV2WithHttpInfo().getData();
  }

  /**
   * Get service info
   * Returns version and build metadata for the API.  **Required permission:** _none (public)_
   * @return ApiResponse&lt;ServiceInfoResponse&gt;
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 200 </td><td> The request has succeeded. </td><td>  -  </td></tr>
     </table>
   */
  public ApiResponse<ServiceInfoResponse> infoV2WithHttpInfo() throws ApiException {
    String localVarAccept = apiClient.selectHeaderAccept("application/json");
    String localVarContentType = apiClient.selectHeaderContentType();
    String[] localVarAuthNames = new String[] {"BearerAuth"};
    GenericType<ServiceInfoResponse> localVarReturnType = new GenericType<ServiceInfoResponse>() {};
    return apiClient.invokeAPI("ServiceInfoApi.infoV2", "/v2/service-info", "GET", new ArrayList<>(), null,
                               new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept, localVarContentType,
                               localVarAuthNames, localVarReturnType, false);
  }
}
