"""
Seqera CLI Exceptions

Custom exception classes for the Seqera CLI.
"""

from typing import Optional


class SeqeraError(Exception):
    """Base exception for all Seqera CLI errors."""

    pass


class ApiError(SeqeraError):
    """Exception raised for API errors."""

    def __init__(self, status_code: int, message: str) -> None:
        self.status_code = status_code
        self.message = message
        super().__init__(f"API Error {status_code}: {message}")


class AuthenticationError(SeqeraError):
    """Exception raised for authentication failures."""

    pass


class NotFoundError(SeqeraError):
    """Exception raised when a resource is not found."""

    pass


class ValidationError(SeqeraError):
    """Exception raised for validation errors."""

    pass


class CredentialsNotFoundException(NotFoundError):
    """Exception raised when credentials are not found."""

    def __init__(self, credentials_id: str, workspace: str) -> None:
        self.credentials_id = credentials_id
        self.workspace = workspace
        super().__init__(f"Credentials '{credentials_id}' not found in workspace '{workspace}'")


class ComputeEnvNotFoundException(NotFoundError):
    """Exception raised when a compute environment is not found."""

    def __init__(self, compute_env_name: str, workspace: str) -> None:
        self.compute_env_name = compute_env_name
        self.workspace = workspace
        super().__init__(
            f"Compute environment '{compute_env_name}' not found in workspace '{workspace}'"
        )


class WorkspaceNotFoundException(NotFoundError):
    """Exception raised when a workspace is not found."""

    def __init__(self, workspace_ref: str) -> None:
        self.workspace_ref = workspace_ref
        super().__init__(f"Workspace '{workspace_ref}' not found")


class OrganizationNotFoundException(NotFoundError):
    """Exception raised when an organization is not found."""

    def __init__(self, org_name: str) -> None:
        self.org_name = org_name
        super().__init__(f"Organization '{org_name}' not found")


class PipelineNotFoundException(NotFoundError):
    """Exception raised when a pipeline is not found."""

    def __init__(self, pipeline_name: str, workspace: str) -> None:
        self.pipeline_name = pipeline_name
        self.workspace = workspace
        super().__init__(f"Pipeline '{pipeline_name}' not found in workspace '{workspace}'")


class RunNotFoundException(NotFoundError):
    """Exception raised when a workflow run is not found."""

    def __init__(self, run_id: str, workspace: str) -> None:
        self.run_id = run_id
        self.workspace = workspace
        super().__init__(f"Run '{run_id}' not found in workspace '{workspace}'")


class ActionNotFoundException(NotFoundError):
    """Exception raised when an action is not found."""

    def __init__(self, action_name: str, workspace: str) -> None:
        self.action_name = action_name
        self.workspace = workspace
        super().__init__(f"Action '{action_name}' not found in workspace '{workspace}'")


class MissingRequiredOptionError(SeqeraError):
    """Exception raised when a required option is missing."""

    def __init__(self, option: str) -> None:
        self.option = option
        super().__init__(f"Missing required option: {option}")


class InvalidWorkspaceParameterError(ValidationError):
    """Exception raised when workspace parameter is invalid."""

    def __init__(self, workspace: str) -> None:
        self.workspace = workspace
        super().__init__(
            f"Invalid workspace parameter: {workspace}. Expected format: 'organization/workspace'"
        )


class MultiplePipelinesFoundException(SeqeraError):
    """Exception raised when multiple pipelines match a search."""

    def __init__(self, pipeline_name: str, workspace: str) -> None:
        self.pipeline_name = pipeline_name
        self.workspace = workspace
        super().__init__(
            f"Multiple pipelines found matching '{pipeline_name}' in workspace '{workspace}'. "
            f"Please specify the pipeline ID with --id instead."
        )


class NoComputeEnvironmentException(SeqeraError):
    """Exception raised when no compute environment is available."""

    def __init__(self, workspace: str) -> None:
        self.workspace = workspace
        super().__init__(
            f"No compute environment available in workspace '{workspace}'. "
            f"Please create a compute environment first or specify one with --compute-env."
        )


class InvalidResponseException(SeqeraError):
    """Exception raised when API response is invalid."""

    def __init__(self, message: str) -> None:
        self.message = message
        super().__init__(message)
