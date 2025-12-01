"""
AWS Batch Forge platform implementation for compute environments.

Forge mode: Seqera automatically creates and manages AWS Batch resources.
"""

import sys
from pathlib import Path
from typing import Annotated

import typer

from seqera.exceptions import SeqeraError
from seqera.main import get_client, get_output_format
from seqera.responses.computeenvs import ComputeEnvAdded
from seqera.utils.output import output_error


def read_file_content(file_path: Path | None) -> str | None:
    """Read and return the content of a file."""
    if file_path is None:
        return None
    try:
        with open(file_path) as f:
            return f.read()
    except Exception as e:
        output_error(f"Failed to read file {file_path}: {e}")
        sys.exit(1)


def parse_environment_variables(env_vars: list[str] | None) -> list[dict] | None:
    """Parse environment variables from key=value format with optional scope prefix.

    Format: [scope:]key=value
    Scopes: head, compute, both (default is head)

    Examples:
    - FOO=bar -> head only
    - compute:FOO=bar -> compute only
    - both:FOO=bar -> both head and compute
    """
    if not env_vars:
        return None

    result = []
    for env in env_vars:
        if "=" not in env:
            output_error(f"Invalid environment variable format: {env}. Expected [scope:]key=value")
            sys.exit(1)

        # Check for scope prefix
        head = True
        compute = False
        value_part = env

        if ":" in env:
            parts = env.split(":", 1)
            scope = parts[0].lower()
            if scope in ["head", "compute", "both"]:
                value_part = parts[1]
                if scope == "head":
                    head = True
                    compute = False
                elif scope == "compute":
                    head = False
                    compute = True
                elif scope == "both":
                    head = True
                    compute = True

        if "=" not in value_part:
            output_error(f"Invalid environment variable format: {env}. Expected [scope:]key=value")
            sys.exit(1)

        key, value = value_part.split("=", 1)
        result.append(
            {
                "name": key,
                "value": value,
                "head": head,
                "compute": compute,
            }
        )
    return result


def add_aws_forge(
    name: Annotated[
        str,
        typer.Option("-n", "--name", help="Compute environment name"),
    ],
    work_dir: Annotated[
        str,
        typer.Option("--work-dir", help="Work directory (e.g., s3://bucket/path)"),
    ],
    region: Annotated[
        str,
        typer.Option("-r", "--region", help="AWS region"),
    ],
    max_cpus: Annotated[
        int,
        typer.Option("--max-cpus", help="Maximum number of CPUs to provision"),
    ],
    # Provisioning options
    provisioning_model: Annotated[
        str,
        typer.Option("--provisioning-model", help="Provisioning model (SPOT or EC2)"),
    ] = "SPOT",
    no_ebs_auto_scale: Annotated[
        bool,
        typer.Option("--no-ebs-auto-scale", help="Disable EBS auto-expandable disk provisioning"),
    ] = False,
    # Feature flags
    fusion: Annotated[
        bool,
        typer.Option("--fusion", help="DEPRECATED - Use '--fusion-v2' instead"),
    ] = False,
    fusion_v2: Annotated[
        bool,
        typer.Option("--fusion-v2", help="Enable Fusion v2 for S3 bucket access (requires Wave)"),
    ] = False,
    wave: Annotated[
        bool,
        typer.Option("--wave", help="Enable Wave containers service for private repositories"),
    ] = False,
    fast_storage: Annotated[
        bool,
        typer.Option(
            "--fast-storage",
            help="Enable NVMe instance storage for faster I/O (requires Fusion v2)",
        ),
    ] = False,
    snapshots: Annotated[
        bool,
        typer.Option("--snapshots", help="Enable Fusion snapshots for spot reclamation recovery"),
    ] = False,
    fargate: Annotated[
        bool,
        typer.Option("--fargate", help="Run head job on Fargate (requires Fusion v2 and Spot)"),
    ] = False,
    gpu: Annotated[
        bool,
        typer.Option("--gpu", help="Deploy GPU-enabled EC2 instances"),
    ] = False,
    allow_buckets: Annotated[
        str | None,
        typer.Option(
            "--allow-buckets", help="Comma-separated S3 buckets/paths for read-write access"
        ),
    ] = None,
    preserve_resources: Annotated[
        bool,
        typer.Option(
            "--preserve-resources",
            help="Preserve Batch resources after compute environment deletion",
        ),
    ] = False,
    # EFS options
    create_efs: Annotated[
        bool,
        typer.Option("--create-efs", help="Create OneZone EFS without backup"),
    ] = False,
    efs_id: Annotated[
        str | None,
        typer.Option("--efs-id", help="Existing EFS file system ID (e.g., fs-0123456789)"),
    ] = None,
    efs_mount: Annotated[
        str | None,
        typer.Option("--efs-mount", help="EFS mount path (default: pipeline work directory)"),
    ] = None,
    # FSX options
    fsx_size: Annotated[
        int | None,
        typer.Option(
            "--fsx-size", help="FSx storage capacity in GB (min 1,200 or increments of 2,400)"
        ),
    ] = None,
    fsx_dns: Annotated[
        str | None,
        typer.Option(
            "--fsx-dns", help="FSx file system DNS name (e.g., fs-xxx.fsx.region.amazonaws.com)"
        ),
    ] = None,
    fsx_mount: Annotated[
        str | None,
        typer.Option("--fsx-mount", help="FSx mount path (default: pipeline work directory)"),
    ] = None,
    # Common platform options
    pre_run: Annotated[
        Path | None,
        typer.Option("--pre-run", help="Pre-run script file"),
    ] = None,
    post_run: Annotated[
        Path | None,
        typer.Option("--post-run", help="Post-run script file"),
    ] = None,
    environment: Annotated[
        list[str] | None,
        typer.Option(
            "--environment",
            "-e",
            help="Environment variables ([scope:]key=value, scope: head|compute|both)",
        ),
    ] = None,
    nextflow_config: Annotated[
        Path | None,
        typer.Option("--nextflow-config", help="Nextflow config file"),
    ] = None,
    # Advanced options
    instance_types: Annotated[
        str | None,
        typer.Option(
            "--instance-types", help="Comma-separated instance types (e.g., m4,c4,optimal)"
        ),
    ] = None,
    alloc_strategy: Annotated[
        str | None,
        typer.Option(
            "--alloc-strategy",
            help="Allocation strategy (BEST_FIT_PROGRESSIVE, SPOT_CAPACITY_OPTIMIZED)",
        ),
    ] = None,
    vpc_id: Annotated[
        str | None,
        typer.Option("--vpc-id", help="VPC identifier"),
    ] = None,
    subnets: Annotated[
        str | None,
        typer.Option("--subnets", help="Comma-separated subnet IDs"),
    ] = None,
    security_groups: Annotated[
        str | None,
        typer.Option("--security-groups", help="Comma-separated security group IDs"),
    ] = None,
    ami_id: Annotated[
        str | None,
        typer.Option("--ami-id", help="Custom AMI ID (must be AWS Linux-2 ECS-optimized)"),
    ] = None,
    key_pair: Annotated[
        str | None,
        typer.Option("--key-pair", help="EC2 key pair for SSH access"),
    ] = None,
    min_cpus: Annotated[
        int | None,
        typer.Option("--min-cpus", help="Minimum CPUs to keep provisioned (always billed)"),
    ] = None,
    head_job_cpus: Annotated[
        int | None,
        typer.Option("--head-job-cpus", help="Number of CPUs for Nextflow head job"),
    ] = None,
    head_job_memory: Annotated[
        int | None,
        typer.Option("--head-job-memory", help="Memory in MiB for Nextflow head job"),
    ] = None,
    head_job_role: Annotated[
        str | None,
        typer.Option("--head-job-role", help="IAM role for Nextflow head job"),
    ] = None,
    compute_job_role: Annotated[
        str | None,
        typer.Option("--compute-job-role", help="IAM role for compute jobs"),
    ] = None,
    batch_execution_role: Annotated[
        str | None,
        typer.Option("--batch-execution-role", help="Execution role for ECS container"),
    ] = None,
    ebs_blocksize: Annotated[
        int | None,
        typer.Option(
            "--ebs-blocksize", help="EBS auto-expandable volume initial size in GB (default: 50)"
        ),
    ] = None,
    bid_percentage: Annotated[
        int | None,
        typer.Option(
            "--bid-percentage", help="Max Spot price as percentage of On-Demand (default: 100)"
        ),
    ] = None,
    cli_path: Annotated[
        str | None,
        typer.Option("--cli-path", help="AWS CLI path in EC2 instances"),
    ] = None,
    # Credentials option
    credentials_id: Annotated[
        str | None,
        typer.Option("-c", "--credentials", help="Credentials identifier"),
    ] = None,
    # Workspace option
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
) -> None:
    """Add new AWS Batch compute environment with automatic resource provisioning."""
    # Import here to avoid circular dependency
    from seqera.commands.computeenvs import (
        USER_WORKSPACE_NAME,
        handle_compute_env_error,
        output_response,
    )

    try:
        client = get_client()
        output_format = get_output_format()

        # Validate deprecated fusion flag
        if fusion:
            raise SeqeraError("Fusion v1 is deprecated, please use '--fusion-v2' instead")

        # Validate Fusion v2 requires Wave
        if fusion_v2 and not wave:
            raise SeqeraError("Fusion v2 requires Wave service")

        # Resolve credentials ID if not provided
        if not credentials_id:
            # Get credentials for this platform
            creds_response = client.get("/credentials", params={"platformId": "aws-batch"})
            credentials = creds_response.get("credentials", [])

            if not credentials:
                output_error("No AWS Batch credentials found. Please create AWS credentials first.")
                sys.exit(1)

            # Use the first matching credential with the same name as compute env
            matching_cred = None
            for cred in credentials:
                if cred.get("name") == name:
                    matching_cred = cred
                    break

            if matching_cred:
                credentials_id = matching_cred.get("id")
            else:
                # Use first available credential
                credentials_id = credentials[0].get("id")

        # Read file contents
        pre_run_content = read_file_content(pre_run)
        post_run_content = read_file_content(post_run)
        nextflow_config_content = read_file_content(nextflow_config)

        # Parse environment variables
        env_vars = parse_environment_variables(environment)

        # Parse comma-separated lists
        allow_buckets_list = allow_buckets.split(",") if allow_buckets else None
        instance_types_list = instance_types.split(",") if instance_types else None
        subnets_list = subnets.split(",") if subnets else None
        security_groups_list = security_groups.split(",") if security_groups else None

        # Build forge configuration
        forge_config = {
            "type": provisioning_model,
            "minCpus": min_cpus if min_cpus is not None else 0,
            "maxCpus": max_cpus,
            "gpuEnabled": gpu,
            "ebsAutoScale": not no_ebs_auto_scale,
            "disposeOnDeletion": not preserve_resources,
            "fargateHeadEnabled": fargate,
        }

        # Add optional forge fields
        if allow_buckets_list:
            forge_config["allowBuckets"] = allow_buckets_list
        if instance_types_list:
            forge_config["instanceTypes"] = instance_types_list
        if alloc_strategy:
            forge_config["allocStrategy"] = alloc_strategy
        if vpc_id:
            forge_config["vpcId"] = vpc_id
        if subnets_list:
            forge_config["subnets"] = subnets_list
        if security_groups_list:
            forge_config["securityGroups"] = security_groups_list
        if ami_id:
            forge_config["imageId"] = ami_id
        if key_pair:
            forge_config["ec2KeyPair"] = key_pair
        if ebs_blocksize:
            forge_config["ebsBlockSize"] = ebs_blocksize
        if bid_percentage:
            forge_config["bidPercentage"] = bid_percentage

        # Add EFS options to forge config
        if create_efs:
            forge_config["efsCreate"] = True
        if efs_id:
            forge_config["efsId"] = efs_id
        if efs_mount:
            forge_config["efsMount"] = efs_mount

        # Add FSX options to forge config
        if fsx_size:
            forge_config["fsxSize"] = fsx_size
        if fsx_dns:
            forge_config["fsxName"] = fsx_dns
        if fsx_mount:
            forge_config["fsxMount"] = fsx_mount

        # Build config payload
        config = {
            "workDir": work_dir,
            "region": region,
            "fusion2Enabled": fusion_v2,
            "waveEnabled": wave,
            "forge": forge_config,
        }

        # Add optional fields
        if pre_run_content:
            config["preRunScript"] = pre_run_content
        if post_run_content:
            config["postRunScript"] = post_run_content
        if nextflow_config_content:
            config["nextflowConfig"] = nextflow_config_content
        if env_vars:
            config["environment"] = env_vars
        if fast_storage:
            config["nvnmeStorageEnabled"] = fast_storage
        if snapshots:
            config["fusionSnapshots"] = snapshots
        if cli_path:
            config["cliPath"] = cli_path
        if batch_execution_role:
            config["executionRole"] = batch_execution_role
        if compute_job_role:
            config["computeJobRole"] = compute_job_role
        if head_job_cpus:
            config["headJobCpus"] = head_job_cpus
        if head_job_memory:
            config["headJobMemoryMb"] = head_job_memory
        if head_job_role:
            config["headJobRole"] = head_job_role

        # Build full payload
        payload = {
            "computeEnv": {
                "name": name,
                "platform": "aws-batch",
                "credentialsId": credentials_id,
                "config": config,
            }
        }

        # Create compute environment
        response = client.post("/compute-envs", json=payload)

        # Output response
        result = ComputeEnvAdded(
            platform="aws-batch",
            compute_env_id=response.get("computeEnvId", ""),
            name=name,
            workspace_id=None,
            workspace=USER_WORKSPACE_NAME,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_compute_env_error(e)
