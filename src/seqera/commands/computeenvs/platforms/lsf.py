"""
LSF compute environment platform implementation.

Ported from LsfPlatform.java
"""

from typing import Optional

import typer
from typing_extensions import Annotated


def add_lsf(
    name: Annotated[
        str,
        typer.Option("-n", "--name", help="Compute environment name"),
    ],
    work_dir: Annotated[
        str,
        typer.Option("--work-dir", help="Work directory"),
    ],
    head_queue: Annotated[
        str,
        typer.Option("-q", "--head-queue", help="The name of the queue on the cluster used to launch the execution of the Nextflow pipeline"),
    ],
    user_name: Annotated[
        Optional[str],
        typer.Option("-u", "--user-name", help="The username on the cluster used to launch the pipeline execution"),
    ] = None,
    host_name: Annotated[
        Optional[str],
        typer.Option("-H", "--host-name", help="The pipeline execution is launched by connecting via SSH to the hostname specified. This usually is the cluster login node. Local IP addresses e.g. 127.*, 172.*, 192.*, etc. are not allowed, use a fully qualified hostname instead"),
    ] = None,
    port: Annotated[
        Optional[int],
        typer.Option("-p", "--port", help="Port number for the login connection"),
    ] = None,
    compute_queue: Annotated[
        Optional[str],
        typer.Option("--compute-queue", help="The name of queue on the cluster to which pipeline jobs are submitted. This queue can be overridden by the pipeline configuration"),
    ] = None,
    launch_dir: Annotated[
        Optional[str],
        typer.Option("--launch-dir", help="The directory where Nextflow runs. It must be an absolute directory and the user should have read-write access permissions to it [default: pipeline work directory]"),
    ] = None,
    max_queue_size: Annotated[
        Optional[int],
        typer.Option("--max-queue-size", help="This option limits the number of jobs Nextflow can submit to the LSF queue at the same time [default: 100]"),
    ] = None,
    head_job_options: Annotated[
        Optional[str],
        typer.Option("--head-job-options", help="LSF submit options for the Nextflow head job. These options are added to the 'bsub' command run by Tower to launch the pipeline execution"),
    ] = None,
    unit_for_limits: Annotated[
        Optional[str],
        typer.Option("--unit-for-limits", help="This option defines the unit used by your LSF cluster for memory limits. It should match the attribute LSF_UNIT_FOR_LIMITS setting in your lsf.conf file"),
    ] = None,
    per_job_mem_limit: Annotated[
        Optional[bool],
        typer.Option("--per-job-mem-limit", help="Whether the memory limit is interpreted as per-job or per-process. It should match the attribute LSB_JOB_MEMLIMIT in your lsf.conf file"),
    ] = None,
    per_task_reserve: Annotated[
        Optional[bool],
        typer.Option("--per-task-reserve", help="Whether the memory reservation is made on job tasks instead of per-host. It should match the attribute RESOURCE_RESERVE_PER_TASK in your lsf.conf file"),
    ] = None,
    credentials: Annotated[
        Optional[str],
        typer.Option("-c", "--credentials", help="Credentials identifier [default: workspace credentials]"),
    ] = None,
) -> None:
    """Add new IBM LSF compute environment."""
    # Import here to avoid circular imports
    from seqera.main import get_client, get_output_format
    from seqera.responses.computeenvs import ComputeEnvAdded
    from seqera.commands.computeenvs import (
        USER_WORKSPACE_NAME,
        handle_compute_env_error,
        output_response,
    )
    from seqera.exceptions import SeqeraError

    try:
        client = get_client()
        output_format = get_output_format()

        # Find credentials for lsf-platform
        credentials_id = None
        if credentials:
            # Use provided credentials reference
            # First try to find by name
            creds_response = client.get("/credentials", params={"platformId": "lsf-platform"})
            creds_list = creds_response.get("credentials", [])
            for cred in creds_list:
                if cred.get("name") == credentials or cred.get("id") == credentials:
                    credentials_id = cred.get("id")
                    break
            if not credentials_id:
                raise SeqeraError(f"Credentials '{credentials}' not found for lsf-platform")
        else:
            # Find workspace credentials automatically
            creds_response = client.get("/credentials", params={"platformId": "lsf-platform"})
            creds_list = creds_response.get("credentials", [])

            if not creds_list:
                raise SeqeraError("No valid credentials found at the workspace for lsf-platform")

            if len(creds_list) > 1:
                raise SeqeraError("Multiple credentials match this compute environment. Please provide the credentials identifier that you want to use")

            credentials_id = creds_list[0].get("id")

        # Build config payload
        config = {
            "workDir": work_dir,
            "headQueue": head_queue,
        }

        # Add optional main parameters
        if user_name:
            config["userName"] = user_name
        if host_name:
            config["hostName"] = host_name
        if port:
            config["port"] = port
        if compute_queue:
            config["computeQueue"] = compute_queue
        if launch_dir:
            config["launchDir"] = launch_dir

        # Add advanced options
        if max_queue_size:
            config["maxQueueSize"] = max_queue_size
        if head_job_options:
            config["headJobOptions"] = head_job_options
        if unit_for_limits:
            config["unitForLimits"] = unit_for_limits
        if per_job_mem_limit is not None:
            config["perJobMemLimit"] = per_job_mem_limit
        if per_task_reserve is not None:
            config["perTaskReserve"] = per_task_reserve

        # Create compute environment
        payload = {
            "computeEnv": {
                "name": name,
                "platform": "lsf-platform",
                "credentialsId": credentials_id,
                "config": config,
            }
        }

        response = client.post("/compute-envs", json=payload)

        # Output response
        result = ComputeEnvAdded(
            platform="lsf-platform",
            compute_env_id=response.get("computeEnvId", ""),
            name=name,
            workspace_id=None,
            workspace=USER_WORKSPACE_NAME,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_compute_env_error(e)
