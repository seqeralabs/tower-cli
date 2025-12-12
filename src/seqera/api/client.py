"""
Seqera Platform API Client

This module provides the HTTP client for interacting with the Seqera Platform API.
"""

from typing import Any
from urllib.parse import urljoin

import httpx

from seqera.exceptions import (
    ApiError,
    AuthenticationError,
    NotFoundError,
    ValidationError,
)


class SeqeraClient:
    """
    HTTP client for Seqera Platform API.

    This client handles:
    - Authentication via Bearer token
    - Request/response serialization
    - Error handling and conversion to custom exceptions
    - Base URL and endpoint management
    """

    def __init__(
        self,
        base_url: str,
        token: str,
        insecure: bool = False,
        verbose: bool = False,
        timeout: float = 30.0,
    ) -> None:
        """
        Initialize the Seqera API client.

        Args:
            base_url: Base URL of the Seqera Platform API
            token: Personal access token for authentication
            insecure: Allow non-SSL connections (not recommended)
            verbose: Enable verbose logging of requests/responses
            timeout: Request timeout in seconds
        """
        self.base_url = base_url.rstrip("/")
        self.token = token
        self.verbose = verbose

        # Determine the scheme
        if insecure and not self.base_url.startswith("http"):
            self.base_url = f"http://{self.base_url}"
        elif not self.base_url.startswith("http"):
            self.base_url = f"https://{self.base_url}"

        # Create httpx client
        self.client = httpx.Client(
            base_url=self.base_url,
            headers={
                "Authorization": f"Bearer {self.token}",
                "Content-Type": "application/json",
                "Accept": "application/json",
            },
            timeout=timeout,
            verify=not insecure,
        )

    def _handle_response(self, response: httpx.Response) -> dict[str, Any]:
        """
        Handle HTTP response and convert errors to exceptions.

        Args:
            response: The HTTP response object

        Returns:
            Parsed JSON response body

        Raises:
            AuthenticationError: For 401 Unauthorized
            NotFoundError: For 403/404 Not Found
            ValidationError: For 400 Bad Request
            ApiError: For other HTTP errors
        """
        if self.verbose:
            print(f"[HTTP] {response.request.method} {response.url}", file=__import__("sys").stderr)
            print(f"[HTTP] Status: {response.status_code}", file=__import__("sys").stderr)
            if response.request.content:
                print(
                    f"[HTTP] Request: {response.request.content.decode()}",
                    file=__import__("sys").stderr,
                )
            if response.content:
                print(f"[HTTP] Response: {response.text}", file=__import__("sys").stderr)

        # Handle success responses
        if 200 <= response.status_code < 300:
            # Handle 204 No Content
            if response.status_code == 204 or not response.content:
                return {}

            try:
                return response.json()
            except Exception:
                # If JSON parsing fails, return empty dict
                return {}

        # Handle error responses
        error_msg = f"HTTP {response.status_code}"
        try:
            error_data = response.json()
            if "message" in error_data:
                error_msg = error_data["message"]
            elif "error" in error_data:
                error_msg = error_data["error"]
        except Exception:
            error_msg = response.text or error_msg

        if response.status_code == 401:
            raise AuthenticationError(f"Unauthorized: {error_msg}")
        elif response.status_code in (403, 404):
            raise NotFoundError(error_msg)
        elif response.status_code == 400:
            raise ValidationError(error_msg)
        else:
            raise ApiError(response.status_code, error_msg)

    def get(
        self,
        endpoint: str,
        params: dict[str, Any] | None = None,
        raw: bool = False,
    ) -> dict[str, Any] | bytes:
        """
        Perform a GET request.

        Args:
            endpoint: API endpoint (relative to base_url)
            params: Query parameters
            raw: If True, return raw bytes instead of parsed JSON

        Returns:
            Parsed JSON response or raw bytes if raw=True
        """
        url = (
            endpoint
            if endpoint.startswith("http")
            else urljoin(self.base_url + "/", endpoint.lstrip("/"))
        )
        response = self.client.get(url, params=params)

        if raw:
            # For raw requests, just check status and return content
            if 200 <= response.status_code < 300:
                return response.content
            # Handle errors
            self._handle_response(response)
            return b""

        return self._handle_response(response)

    def post(
        self,
        endpoint: str,
        json: dict[str, Any] | None = None,
        params: dict[str, Any] | None = None,
    ) -> dict[str, Any]:
        """
        Perform a POST request.

        Args:
            endpoint: API endpoint (relative to base_url)
            json: Request body as JSON
            params: Query parameters

        Returns:
            Parsed JSON response
        """
        url = (
            endpoint
            if endpoint.startswith("http")
            else urljoin(self.base_url + "/", endpoint.lstrip("/"))
        )
        response = self.client.post(url, json=json, params=params)
        return self._handle_response(response)

    def put(
        self,
        endpoint: str,
        json: dict[str, Any] | None = None,
        params: dict[str, Any] | None = None,
    ) -> dict[str, Any]:
        """
        Perform a PUT request.

        Args:
            endpoint: API endpoint (relative to base_url)
            json: Request body as JSON
            params: Query parameters

        Returns:
            Parsed JSON response
        """
        url = (
            endpoint
            if endpoint.startswith("http")
            else urljoin(self.base_url + "/", endpoint.lstrip("/"))
        )
        response = self.client.put(url, json=json, params=params)
        return self._handle_response(response)

    def delete(
        self,
        endpoint: str,
        params: dict[str, Any] | None = None,
    ) -> dict[str, Any]:
        """
        Perform a DELETE request.

        Args:
            endpoint: API endpoint (relative to base_url)
            params: Query parameters

        Returns:
            Parsed JSON response
        """
        url = (
            endpoint
            if endpoint.startswith("http")
            else urljoin(self.base_url + "/", endpoint.lstrip("/"))
        )
        response = self.client.delete(url, params=params)
        return self._handle_response(response)

    def close(self) -> None:
        """Close the HTTP client."""
        self.client.close()

    def __enter__(self) -> "SeqeraClient":
        """Context manager entry."""
        return self

    def __exit__(self, *args: Any) -> None:
        """Context manager exit."""
        self.close()
