#!/usr/bin/env bash
#
# Integration test script for Azure Batch CLI features (COMP-1464)
#
# Prerequisites:
#   - TOWER_ACCESS_TOKEN set
#   - TOWER_API_ENDPOINT set (or defaults to api.cloud.seqera.io)
#   - tw CLI binary on PATH
#   - A workspace with Azure Entra credentials configured
#
# Usage:
#   export TOWER_ACCESS_TOKEN=<token>
#   export TOWER_API_ENDPOINT=<endpoint>
#   ./test-azure-batch-features.sh [--workspace <workspace>]
#

set -uo pipefail

# ---------------------------------------------------------------------------
# Configuration - override via environment variables
# ---------------------------------------------------------------------------
WORKSPACE="${WORKSPACE:-}"
TW="${TW:-tw}"

# Azure resource placeholders - override with real values for live testing
BATCH_NAME="${BATCH_NAME:-my-batch-account}"
STORAGE_NAME="${STORAGE_NAME:-my-storage-account}"
TENANT_ID="${TENANT_ID:-00000000-0000-0000-0000-000000000001}"
CLIENT_ID="${CLIENT_ID:-00000000-0000-0000-0000-000000000002}"
CLIENT_SECRET="${CLIENT_SECRET:-test-client-secret}"
WORK_DIR="${WORK_DIR:-az://container/work}"
LOCATION="${LOCATION:-eastus}"

MI_HEAD_CLIENT_ID="${MI_HEAD_CLIENT_ID:-11111111-1111-1111-1111-111111111111}"
MI_POOL_CLIENT_ID="${MI_POOL_CLIENT_ID:-22222222-2222-2222-2222-222222222222}"
MI_HEAD_RESOURCE_ID="${MI_HEAD_RESOURCE_ID:-/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myRg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/headIdentity}"
MI_POOL_RESOURCE_ID="${MI_POOL_RESOURCE_ID:-/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myRg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/poolIdentity}"
# SUBNET_ID must be a full subnet resource ID (including /subnets/{name}).
# If the env var only contains a VNet ID, append /subnets/default.
SUBNET_ID="${SUBNET_ID:-/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myRg/providers/Microsoft.Network/virtualNetworks/myVnet/subnets/mySubnet}"
if [[ "$SUBNET_ID" != */subnets/* ]]; then
    SUBNET_ID="$SUBNET_ID/subnets/default"
fi

# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------
PASS=0
FAIL=0

ws_flag() {
    if [[ -n "$WORKSPACE" ]]; then
        echo "--workspace=$WORKSPACE"
    fi
}

run_tw() {
    local description="$1"
    shift
    echo -n "  TEST: $description ... "
    if output=$($TW "$@" $(ws_flag) 2>&1); then
        echo "PASS"
        ((PASS++))
        return 0
    else
        echo "FAIL"
        echo "    Command: $TW $* $(ws_flag)"
        echo "    Output: $output"
        ((FAIL++))
        return 1
    fi
}

run_tw_expect_fail() {
    local description="$1"
    shift
    echo -n "  TEST: $description ... "
    if output=$($TW "$@" $(ws_flag) 2>&1); then
        echo "FAIL (expected failure but succeeded)"
        echo "    Command: $TW $* $(ws_flag)"
        echo "    Output: $output"
        ((FAIL++))
        return 1
    else
        echo "PASS (failed as expected)"
        ((PASS++))
        return 0
    fi
}

cleanup_credential() {
    local name="$1"
    $TW credentials delete --name="$name" $(ws_flag) 2>/dev/null || true
}

cleanup_ce() {
    local name="$1"
    $TW compute-envs delete --name="$name" $(ws_flag) 2>/dev/null || true
}

# Parse arguments
while [[ $# -gt 0 ]]; do
    case "$1" in
        --workspace)
            WORKSPACE="$2"
            shift 2
            ;;
        --workspace=*)
            WORKSPACE="${1#*=}"
            shift
            ;;
        *)
            echo "Unknown option: $1"
            exit 1
            ;;
    esac
done

echo "============================================================"
echo " Azure Batch CLI Features Test Suite (COMP-1464)"
echo "============================================================"
echo ""
echo "Endpoint:  ${TOWER_API_ENDPOINT:-api.cloud.seqera.io}"
echo "Workspace: ${WORKSPACE:-<user default>}"
echo ""

# ===================================================================
# 1. Azure Entra Credentials
# ===================================================================
echo "--- 1. Azure Entra Credentials ---"

CRED_NAME="test-entra-cred-$$"

run_tw "Add Entra credentials" \
    credentials add azure-entra \
    --name="$CRED_NAME" \
    --batch-name="$BATCH_NAME" \
    --storage-name="$STORAGE_NAME" \
    --tenant-id="$TENANT_ID" \
    --client-id="$CLIENT_ID" \
    --client-secret="$CLIENT_SECRET"

run_tw "Update Entra credentials" \
    credentials update azure-entra \
    --name="$CRED_NAME" \
    --batch-name="$BATCH_NAME" \
    --storage-name="$STORAGE_NAME" \
    --tenant-id="$TENANT_ID" \
    --client-id="$CLIENT_ID" \
    --client-secret="$CLIENT_SECRET"

run_tw_expect_fail "Add Entra credentials missing required --tenant-id" \
    credentials add azure-entra \
    --name="should-fail-$$" \
    --batch-name="$BATCH_NAME" \
    --storage-name="$STORAGE_NAME" \
    --client-id="$CLIENT_ID" \
    --client-secret="$CLIENT_SECRET"

echo ""

# ===================================================================
# 2. Forge CE - Single Pool (backward compat)
# ===================================================================
echo "--- 2. Forge CE - Single Pool ---"

CE_FORGE_SINGLE="test-forge-single-$$"

run_tw "Create Forge CE (single pool, basic)" \
    compute-envs add azure-batch forge \
    -n "$CE_FORGE_SINGLE" \
    --credentials="$CRED_NAME" \
    -l "$LOCATION" \
    --work-dir="$WORK_DIR" \
    --vm-count=5

cleanup_ce "$CE_FORGE_SINGLE"
echo ""

# ===================================================================
# 3. Forge CE - Single Pool with all new options
# ===================================================================
echo "--- 3. Forge CE - Single Pool with new options ---"

CE_FORGE_FULL="test-forge-full-$$"

run_tw "Create Forge CE (single pool, all options)" \
    compute-envs add azure-batch forge \
    -n "$CE_FORGE_FULL" \
    --credentials="$CRED_NAME" \
    -l "$LOCATION" \
    --work-dir="$WORK_DIR" \
    --vm-type=Standard_D4s_v3 \
    --vm-count=10 \
    --no-auto-scale \
    --fusion-v2 \
    --wave \
    --subnet-id="$SUBNET_ID" \
    --managed-identity-client-id="$MI_HEAD_CLIENT_ID" \
    --managed-identity-pool-client-id="$MI_POOL_CLIENT_ID" \
    --managed-identity-head-resource-id="$MI_HEAD_RESOURCE_ID" \
    --managed-identity-pool-resource-id="$MI_POOL_RESOURCE_ID" \
    --delete-jobs-on-completion=true \
    --delete-tasks-on-completion=true \
    --terminate-jobs-on-completion=true \
    --token-duration=24h \
    --job-max-wall-clock-time=7d

cleanup_ce "$CE_FORGE_FULL"
echo ""

# ===================================================================
# 4. Forge CE - Dual Pool
# ===================================================================
echo "--- 4. Forge CE - Dual Pool ---"

CE_FORGE_DUAL="test-forge-dual-$$"

run_tw "Create Forge CE (dual pool, defaults)" \
    compute-envs add azure-batch forge \
    -n "$CE_FORGE_DUAL" \
    --credentials="$CRED_NAME" \
    -l "$LOCATION" \
    --work-dir="$WORK_DIR" \
    --dual-pool \
    --head-vm-count=1 \
    --worker-vm-count=8

cleanup_ce "$CE_FORGE_DUAL"

CE_FORGE_DUAL_FULL="test-forge-dual-full-$$"

run_tw "Create Forge CE (dual pool, all options)" \
    compute-envs add azure-batch forge \
    -n "$CE_FORGE_DUAL_FULL" \
    --credentials="$CRED_NAME" \
    -l "$LOCATION" \
    --work-dir="$WORK_DIR" \
    --dual-pool \
    --head-vm-type=Standard_D2s_v3 \
    --head-vm-count=1 \
    --head-no-auto-scale=true \
    --worker-vm-type=Standard_D8s_v3 \
    --worker-vm-count=16 \
    --worker-no-auto-scale=false \
    --subnet-id="$SUBNET_ID" \
    --delete-tasks-on-completion=false \
    --terminate-jobs-on-completion=false \
    --job-max-wall-clock-time=14d

cleanup_ce "$CE_FORGE_DUAL_FULL"
echo ""

# ===================================================================
# 5. Cleanup toggles combinations
# ===================================================================
echo "--- 5. Cleanup toggle combinations ---"

CE_CLEANUP="test-cleanup-$$"

run_tw "Create CE with all cleanup toggles off" \
    compute-envs add azure-batch forge \
    -n "$CE_CLEANUP" \
    --credentials="$CRED_NAME" \
    -l "$LOCATION" \
    --work-dir="$WORK_DIR" \
    --vm-count=1 \
    --delete-jobs-on-completion=false \
    --delete-tasks-on-completion=false \
    --terminate-jobs-on-completion=false

cleanup_ce "$CE_CLEANUP"
echo ""

# ===================================================================
# 6. Job max wall clock time edge cases
# ===================================================================
echo "--- 6. Job max wall clock time ---"

CE_WALLCLOCK="test-wallclock-$$"

run_tw "Create CE with 1d wall clock time" \
    compute-envs add azure-batch forge \
    -n "$CE_WALLCLOCK" \
    --credentials="$CRED_NAME" \
    -l "$LOCATION" \
    --work-dir="$WORK_DIR" \
    --vm-count=1 \
    --job-max-wall-clock-time=1d

cleanup_ce "$CE_WALLCLOCK"

run_tw "Create CE with 180d wall clock time (max)" \
    compute-envs add azure-batch forge \
    -n "$CE_WALLCLOCK" \
    --credentials="$CRED_NAME" \
    -l "$LOCATION" \
    --work-dir="$WORK_DIR" \
    --vm-count=1 \
    --job-max-wall-clock-time=180d

cleanup_ce "$CE_WALLCLOCK"

run_tw_expect_fail "Reject wall clock time > 180d" \
    compute-envs add azure-batch forge \
    -n "$CE_WALLCLOCK" \
    --credentials="$CRED_NAME" \
    -l "$LOCATION" \
    --work-dir="$WORK_DIR" \
    --vm-count=1 \
    --job-max-wall-clock-time=181d

run_tw_expect_fail "Reject invalid wall clock time format" \
    compute-envs add azure-batch forge \
    -n "$CE_WALLCLOCK" \
    --credentials="$CRED_NAME" \
    -l "$LOCATION" \
    --work-dir="$WORK_DIR" \
    --vm-count=1 \
    --job-max-wall-clock-time=abc

echo ""

# ===================================================================
# 7. Subnet validation
# ===================================================================
echo "--- 7. Subnet validation ---"

CE_SUBNET="test-subnet-$$"

run_tw_expect_fail "Reject invalid subnet ID format" \
    compute-envs add azure-batch forge \
    -n "$CE_SUBNET" \
    --credentials="$CRED_NAME" \
    -l "$LOCATION" \
    --work-dir="$WORK_DIR" \
    --vm-count=1 \
    --subnet-id="invalid-subnet-id"

echo ""

# ===================================================================
# Cleanup
# ===================================================================
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
bash "$SCRIPT_DIR/clean-azure-batch-test-resources.sh"
echo ""

# ===================================================================
# Summary
# ===================================================================
echo "============================================================"
echo " Results: $PASS passed, $FAIL failed"
echo "============================================================"

if [[ $FAIL -gt 0 ]]; then
    exit 1
fi
