"""
Workflow run models for Seqera SDK.
"""

from datetime import datetime
from typing import Any

from pydantic import Field

from seqera.models.base import SeqeraModel


class WorkflowProgress(SeqeraModel):
    """Progress information for a workflow run."""

    pending: int = 0
    submitted: int = 0
    running: int = 0
    succeeded: int = 0
    cached: int = 0
    failed: int = 0
    aborted: int = 0
    stored: int = 0
    ignored: int = 0
    load_cpus: int = Field(0, alias="loadCpus")
    load_memory: int = Field(0, alias="loadMemory")
    peak_cpus: int = Field(0, alias="peakCpus")
    peak_memory: int = Field(0, alias="peakMemory")
    executors: list[str] | None = None


class NextflowInfo(SeqeraModel):
    """Nextflow version information."""

    version: str | None = None
    build: str | None = None
    timestamp: datetime | None = None


class Workflow(SeqeraModel):
    """
    Workflow run entity from the Seqera Platform.

    Represents an execution of a Nextflow pipeline.
    """

    id: str
    run_name: str | None = Field(None, alias="runName")
    session_id: str | None = Field(None, alias="sessionId")
    status: str | None = None
    start: datetime | None = None
    complete: datetime | None = None
    date_created: datetime | None = Field(None, alias="dateCreated")
    last_updated: datetime | None = Field(None, alias="lastUpdated")
    submit: datetime | None = None
    work_dir: str | None = Field(None, alias="workDir")
    project_dir: str | None = Field(None, alias="projectDir")
    project_name: str | None = Field(None, alias="projectName")
    script_name: str | None = Field(None, alias="scriptName")
    user_name: str | None = Field(None, alias="userName")
    commit_id: str | None = Field(None, alias="commitId")
    command_line: str | None = Field(None, alias="commandLine")
    container: str | None = None
    container_engine: str | None = Field(None, alias="containerEngine")
    error_message: str | None = Field(None, alias="errorMessage")
    error_report: str | None = Field(None, alias="errorReport")
    params: dict[str, Any] | None = None
    config_files: list[str] | None = Field(None, alias="configFiles")
    config_text: str | None = Field(None, alias="configText")
    manifest: dict[str, Any] | None = None
    nextflow: NextflowInfo | None = None
    stats: dict[str, Any] | None = None
    duration: int | None = None
    success: bool | None = None
    exit_status: int | None = Field(None, alias="exitStatus")
    repository: str | None = None
    revision: str | None = None
    resume: bool = False
    optimized: bool | None = None
    workspace_id: int | None = Field(None, alias="workspaceId")
    owner_id: int | None = Field(None, alias="ownerId")
    launch_id: str | None = Field(None, alias="launchId")


class Task(SeqeraModel):
    """
    Task within a workflow run.

    Represents an individual process execution within a workflow.
    """

    task_id: int = Field(alias="taskId")
    name: str | None = None
    process: str | None = None
    tag: str | None = None
    status: str | None = None
    hash: str | None = None
    submit: datetime | None = None
    start: datetime | None = None
    complete: datetime | None = None
    module: list[str] | None = None
    container: str | None = None
    attempt: int | None = None
    script: str | None = None
    scratch: str | None = None
    work_dir: str | None = Field(None, alias="workdir")
    queue: str | None = None
    cpus: int | None = None
    memory: int | None = None
    disk: int | None = None
    time: int | None = None
    env: str | None = None
    executor: str | None = None
    machine_type: str | None = Field(None, alias="machineType")
    cloud_zone: str | None = Field(None, alias="cloudZone")
    price_model: str | None = Field(None, alias="priceModel")
    cost: float | None = None
    error_action: str | None = Field(None, alias="errorAction")
    exit_status: int | None = Field(None, alias="exit")
    duration: int | None = None
    realtime: int | None = None
    native_id: str | None = Field(None, alias="nativeId")
    pcpu: float | None = None
    pmem: float | None = None
    rss: int | None = None
    vmem: int | None = None
    peak_rss: int | None = Field(None, alias="peakRss")
    peak_vmem: int | None = Field(None, alias="peakVmem")
    rchar: int | None = None
    wchar: int | None = None
    syscr: int | None = None
    syscw: int | None = None
    read_bytes: int | None = Field(None, alias="readBytes")
    write_bytes: int | None = Field(None, alias="writeBytes")
    vol_ctxt: int | None = Field(None, alias="volCtxt")
    inv_ctxt: int | None = Field(None, alias="invCtxt")

    @property
    def id(self) -> int:
        """Alias for task_id for convenience."""
        return self.task_id
