"""
Google Life Sciences compute environment platform implementation.

Ported from GoogleLifeSciencesPlatform.java
"""

from typing import Annotated

import typer


def add_google_life_sciences(
    name: Annotated[
        str,
        typer.Option("-n", "--name", help="Compute environment name"),
    ],
    work_dir: Annotated[
        str,
        typer.Option("--work-dir", help="Work directory"),
    ],
    region: Annotated[
        str,
        typer.Option("-r", "--region", help="The region where the workload will be executed"),
    ],
    zones: Annotated[
        list[str] | None,
        typer.Option(
            "--zones",
            help="One or more zones where the workload will be executed. If specified, it has priority over the region setting",
        ),
    ] = None,
    location: Annotated[
        str | None,
        typer.Option(
            "--location",
            help="The location where the job executions are deployed to Cloud Life Sciences API [default: same as the specified region/zone]",
        ),
    ] = None,
    preemptible: Annotated[
        bool | None,
        typer.Option("--preemptible", help="Use preemptible virtual machines"),
    ] = None,
    nfs_target: Annotated[
        str | None,
        typer.Option(
            "--nfs-target",
            help="The Filestore instance IP address and share file name e.g. 1.2.3.4:/my_share_name",
        ),
    ] = None,
    nfs_mount: Annotated[
        str | None,
        typer.Option(
            "--nfs-mount",
            help="Specify the NFS mount path. It should be the same as the pipeline work directory or a parent path of it [default: pipeline work directory]",
        ),
    ] = None,
    use_private_address: Annotated[
        bool | None,
        typer.Option(
            "--use-private-address",
            help="Do not attach a public IP address to the VM. When enabled only Google internal services are accessible",
        ),
    ] = None,
    boot_disk_size: Annotated[
        int | None,
        typer.Option("--boot-disk-size", help="Enter the boot disk size as GB"),
    ] = None,
    head_job_cpus: Annotated[
        int | None,
        typer.Option(
            "--head-job-cpus", help="The number of CPUs to be allocated for the Nextflow runner job"
        ),
    ] = None,
    head_job_memory: Annotated[
        int | None,
        typer.Option(
            "--head-job-memory",
            help="The number of MiB of memory reserved for the Nextflow runner job (value should be a multiple of 256MiB and from 0.5 GB to 8 GB per CPU)",
        ),
    ] = None,
    credentials: Annotated[
        str | None,
        typer.Option(
            "-c", "--credentials", help="Credentials identifier [default: workspace credentials]"
        ),
    ] = None,
    # Labels option
    labels: Annotated[
        str | None,
        typer.Option("--labels", help="Comma-separated list of labels"),
    ] = None,
    # Wait option
    wait: Annotated[
        str | None,
        typer.Option(
            "--wait",
            help="Wait until compute environment reaches status (CREATING, AVAILABLE, ERRORED, INVALID)",
        ),
    ] = None,
    # Workspace option
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
) -> None:
    """Add new Google Life Sciences compute environment."""
    # Import here to avoid circular imports
    from seqera.commands.computeenvs import (
        USER_WORKSPACE_NAME,
        find_or_create_label_ids,
        handle_compute_env_error,
        output_response,
        wait_for_compute_env_status,
    )
    from seqera.exceptions import SeqeraError
    from seqera.main import get_client, get_output_format
    from seqera.responses.computeenvs import ComputeEnvAdded

    try:
        client = get_client()
        output_format = get_output_format()

        # Find credentials for google-lifesciences
        credentials_id = None
        if credentials:
            # Use provided credentials reference
            # First try to find by name
            creds_response = client.get(
                "/credentials", params={"platformId": "google-lifesciences"}
            )
            creds_list = creds_response.get("credentials", [])
            for cred in creds_list:
                if cred.get("name") == credentials or cred.get("id") == credentials:
                    credentials_id = cred.get("id")
                    break
            if not credentials_id:
                raise SeqeraError(f"Credentials '{credentials}' not found for google-lifesciences")
        else:
            # Find workspace credentials automatically
            creds_response = client.get(
                "/credentials", params={"platformId": "google-lifesciences"}
            )
            creds_list = creds_response.get("credentials", [])

            if not creds_list:
                raise SeqeraError(
                    "No valid credentials found at the workspace for google-lifesciences"
                )

            if len(creds_list) > 1:
                raise SeqeraError(
                    "Multiple credentials match this compute environment. Please provide the credentials identifier that you want to use"
                )

            credentials_id = creds_list[0].get("id")

        # Build config payload
        config = {
            "workDir": work_dir,
            "region": region,
        }

        # Add optional main parameters
        if zones:
            config["zones"] = zones
        if location:
            config["location"] = location
        if preemptible is not None:
            config["preemptible"] = preemptible

        # Add filestore options
        if nfs_target:
            config["nfsTarget"] = nfs_target
        if nfs_mount:
            config["nfsMount"] = nfs_mount

        # Add advanced options
        if use_private_address is not None:
            config["usePrivateAddress"] = use_private_address
        if boot_disk_size:
            config["bootDiskSizeGb"] = boot_disk_size
        if head_job_cpus:
            config["headJobCpus"] = head_job_cpus
        if head_job_memory:
            config["headJobMemoryMb"] = head_job_memory

        # Create compute environment
        compute_env_payload = {
            "name": name,
            "platform": "google-lifesciences",
            "credentialsId": credentials_id,
            "config": config,
        }

        # Add labels if specified
        if labels:
            label_ids = find_or_create_label_ids(client, labels, workspace)
            if label_ids:
                compute_env_payload["labelIds"] = label_ids

        payload = {"computeEnv": compute_env_payload}

        response = client.post("/compute-envs", json=payload)
        compute_env_id = response.get("computeEnvId", "")

        # Wait for status if requested
        if wait:
            typer.echo(f"Waiting for compute environment to reach '{wait}' status...")
            if wait_for_compute_env_status(client, compute_env_id, wait, workspace):
                typer.echo(f"Compute environment reached '{wait}' status")
            else:
                typer.echo(
                    f"Warning: Compute environment did not reach '{wait}' status within timeout"
                )

        # Output response
        result = ComputeEnvAdded(
            platform="google-lifesciences",
            compute_env_id=compute_env_id,
            name=name,
            workspace_id=None,
            workspace=USER_WORKSPACE_NAME,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_compute_env_error(e)
