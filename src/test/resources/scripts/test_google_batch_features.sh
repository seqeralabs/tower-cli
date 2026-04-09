#!/usr/bin/env bash
#
# Integration test script for COMP-1463: new Google Batch CE and credential features.
#
# Prerequisites:
#   - TOWER_ACCESS_TOKEN is set
#   - TOWER_API_ENDPOINT is set (e.g., https://api.cloud.seqera.io)
#   - TOWER_WORKSPACE_ID is set (target workspace)
#   - tw CLI is available on PATH (or set TW_CMD to the path)
#   - A Google credential named $GOOGLE_CRED_NAME exists or will be created
#
# Usage:
#   export TOWER_ACCESS_TOKEN=<token>
#   export TOWER_API_ENDPOINT=<url>
#   export TOWER_WORKSPACE_ID=<workspace-id>
#   bash test_google_batch_features.sh
#

set -euo pipefail

# ---------------------------------------------------------------------------
# Configuration — override via environment variables
# ---------------------------------------------------------------------------
TW="${TW_CMD:-tw}"
WORKSPACE_FLAG="--workspace=${TOWER_WORKSPACE_ID:?TOWER_WORKSPACE_ID is required}"

# Google Batch CE defaults
GCP_LOCATION="${GCP_LOCATION:-europe-west1}"
GCP_WORK_DIR="${GCP_WORK_DIR:?GCP_WORK_DIR is required (e.g., gs://your-bucket/work)}"
GCP_NETWORK="${GCP_NETWORK:-default}"

# WIF credential test values
WIF_SA_EMAIL="${WIF_SA_EMAIL:-my-sa@my-project.iam.gserviceaccount.com}"
WIF_PROVIDER="${WIF_PROVIDER:-projects/123456/locations/global/workloadIdentityPools/my-pool/providers/my-provider}"

# Naming
PREFIX="comp1463-test-$(date +%s)"
CRED_SA_NAME="${PREFIX}-google-sa"
CRED_WIF_NAME="${PREFIX}-google-wif"
CE_BASE_NAME="${PREFIX}-ce"

# Counters
PASSED=0
FAILED=0
SKIPPED=0
ERRORS=()

# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------
log()  { echo "--- $*"; }
pass() { ((PASSED++)); echo "  PASS: $1"; }
fail() { ((FAILED++)); ERRORS+=("$1"); echo "  FAIL: $1"; }
skip() { ((SKIPPED++)); echo "  SKIP: $1"; }

# Run a tw command and capture exit code + stderr
run_tw() {
    local description="$1"; shift
    local stderr_file
    stderr_file=$(mktemp)
    local exit_code=0
    "$TW" "$@" 2>"$stderr_file" || exit_code=$?
    LAST_STDERR=$(cat "$stderr_file")
    rm -f "$stderr_file"
    LAST_EXIT=$exit_code
    return 0
}

# Expect success (exit 0)
expect_success() {
    local desc="$1"; shift
    run_tw "$desc" "$@"
    if [[ $LAST_EXIT -eq 0 ]]; then
        pass "$desc"
    else
        fail "$desc (exit=$LAST_EXIT, stderr=$LAST_STDERR)"
    fi
}

# Expect failure (exit != 0) with a specific error substring
expect_failure() {
    local desc="$1"; shift
    local expected_error="$1"; shift
    run_tw "$desc" "$@"
    if [[ $LAST_EXIT -ne 0 ]]; then
        if echo "$LAST_STDERR" | grep -q "$expected_error"; then
            pass "$desc"
        else
            fail "$desc (expected error containing '$expected_error', got: $LAST_STDERR)"
        fi
    else
        fail "$desc (expected failure but got exit 0)"
    fi
}

# Cleanup helper — delete resource, ignore failures
cleanup_cred() {
    "$TW" credentials delete -n "$1" "$WORKSPACE_FLAG" 2>/dev/null || true
}

cleanup_ce() {
    "$TW" compute-envs delete -n "$1" "$WORKSPACE_FLAG" 2>/dev/null || true
}

# ---------------------------------------------------------------------------
# Cleanup trap
# ---------------------------------------------------------------------------
cleanup() {
    log "Cleaning up test resources..."
    cleanup_cred "$CRED_SA_NAME"
    cleanup_cred "$CRED_WIF_NAME"
    # CEs created during tests
    for suffix in base nettags nettags-subnet machine-types machine-wildcard \
                  boot-image fusion-snap all-features; do
        cleanup_ce "${CE_BASE_NAME}-${suffix}"
    done
    log "Cleanup complete."
}
trap cleanup EXIT

# ===========================================================================
# TEST SUITE
# ===========================================================================

log "=== COMP-1463 Integration Tests ==="
log "Endpoint: ${TOWER_API_ENDPOINT}"
log "Workspace: ${TOWER_WORKSPACE_ID}"
log ""

# ---------------------------------------------------------------------------
# 1. GOOGLE CREDENTIALS — WIF MODE
# ---------------------------------------------------------------------------
log "=== 1. Google Credentials — WIF Mode ==="

# 1.1 Add WIF credentials (happy path)
expect_success "Add WIF credentials" \
    credentials add google -n "$CRED_WIF_NAME" "$WORKSPACE_FLAG" \
    --mode=workload-identity \
    --service-account-email="$WIF_SA_EMAIL" \
    --workload-identity-provider="$WIF_PROVIDER"

# 1.2 Add WIF credentials with token audience
cleanup_cred "$CRED_WIF_NAME"
expect_success "Add WIF credentials with token audience" \
    credentials add google -n "$CRED_WIF_NAME" "$WORKSPACE_FLAG" \
    --mode=workload-identity \
    --service-account-email="$WIF_SA_EMAIL" \
    --workload-identity-provider="$WIF_PROVIDER" \
    --token-audience="https://custom-audience.example.com"

# 1.3 Validation: WIF mode without service-account-email
expect_failure "WIF mode rejects missing service-account-email" \
    "'--service-account-email' is required" \
    credentials add google -n "${CRED_WIF_NAME}-bad" "$WORKSPACE_FLAG" \
    --mode=workload-identity \
    --workload-identity-provider="$WIF_PROVIDER"

# 1.4 Validation: WIF mode without workload-identity-provider
expect_failure "WIF mode rejects missing workload-identity-provider" \
    "'--workload-identity-provider' is required" \
    credentials add google -n "${CRED_WIF_NAME}-bad" "$WORKSPACE_FLAG" \
    --mode=workload-identity \
    --service-account-email="$WIF_SA_EMAIL"

# 1.5 Validation: invalid service account email format
expect_failure "WIF mode rejects invalid email format" \
    "Invalid service account email format" \
    credentials add google -n "${CRED_WIF_NAME}-bad" "$WORKSPACE_FLAG" \
    --mode=workload-identity \
    --service-account-email="bad-email@gmail.com" \
    --workload-identity-provider="$WIF_PROVIDER"

# 1.6 Validation: invalid provider format
expect_failure "WIF mode rejects invalid provider format" \
    "Invalid Workload Identity Provider format" \
    credentials add google -n "${CRED_WIF_NAME}-bad" "$WORKSPACE_FLAG" \
    --mode=workload-identity \
    --service-account-email="$WIF_SA_EMAIL" \
    --workload-identity-provider="invalid/path"

# 1.7 Validation: WIF options in SA key mode
expect_failure "SA key mode rejects WIF options" \
    "can only be used with '--mode=workload-identity'" \
    credentials add google -n "${CRED_WIF_NAME}-bad" "$WORKSPACE_FLAG" \
    --service-account-email="$WIF_SA_EMAIL"

# 1.8 Validation: key file in WIF mode
expect_failure "WIF mode rejects --key option" \
    "'--key' cannot be used with '--mode=workload-identity'" \
    credentials add google -n "${CRED_WIF_NAME}-bad" "$WORKSPACE_FLAG" \
    --mode=workload-identity \
    -k /dev/null \
    --service-account-email="$WIF_SA_EMAIL" \
    --workload-identity-provider="$WIF_PROVIDER"

# 1.9 Validation: invalid mode
expect_failure "Rejects invalid mode" \
    "Invalid Google credential mode" \
    credentials add google -n "${CRED_WIF_NAME}-bad" "$WORKSPACE_FLAG" \
    --mode=invalid

# 1.10 Validation: SA key mode requires --key
expect_failure "SA key mode requires --key" \
    "'--key' is required" \
    credentials add google -n "${CRED_WIF_NAME}-bad" "$WORKSPACE_FLAG"

echo ""

# ---------------------------------------------------------------------------
# 2. GOOGLE BATCH CE — NETWORK TAGS
# ---------------------------------------------------------------------------
log "=== 2. Google Batch CE — Network Tags ==="

# 2.1 CE with network tags (happy path)
expect_success "Add CE with network tags" \
    compute-envs add google-batch -n "${CE_BASE_NAME}-nettags" "$WORKSPACE_FLAG" \
    --work-dir="$GCP_WORK_DIR" -l "$GCP_LOCATION" \
    --network="$GCP_NETWORK" \
    --network-tags="allow-ssh,web-tier"

# 2.2 CE with network + subnetwork
expect_success "Add CE with network and subnetwork" \
    compute-envs add google-batch -n "${CE_BASE_NAME}-nettags-subnet" "$WORKSPACE_FLAG" \
    --work-dir="$GCP_WORK_DIR" -l "$GCP_LOCATION" \
    --network="$GCP_NETWORK" \
    --subnetwork="default"

# 2.3 Validation: tags without VPC
expect_failure "Network tags require VPC" \
    "Network tags require VPC configuration" \
    compute-envs add google-batch -n "${CE_BASE_NAME}-bad" "$WORKSPACE_FLAG" \
    --work-dir="$GCP_WORK_DIR" -l "$GCP_LOCATION" \
    --network-tags="allow-ssh"

# 2.4 Validation: invalid tag format (uppercase)
expect_failure "Rejects uppercase network tag" \
    "Invalid network tag" \
    compute-envs add google-batch -n "${CE_BASE_NAME}-bad" "$WORKSPACE_FLAG" \
    --work-dir="$GCP_WORK_DIR" -l "$GCP_LOCATION" \
    --network="$GCP_NETWORK" \
    --network-tags="Allow-SSH"

# 2.5 Validation: tag ending with hyphen
expect_failure "Rejects tag ending with hyphen" \
    "Invalid network tag" \
    compute-envs add google-batch -n "${CE_BASE_NAME}-bad" "$WORKSPACE_FLAG" \
    --work-dir="$GCP_WORK_DIR" -l "$GCP_LOCATION" \
    --network="$GCP_NETWORK" \
    --network-tags="a-"

# 2.6 Single lowercase letter tag (valid)
expect_success "Accepts single letter tag" \
    compute-envs add google-batch -n "${CE_BASE_NAME}-nettags-single" "$WORKSPACE_FLAG" \
    --work-dir="$GCP_WORK_DIR" -l "$GCP_LOCATION" \
    --network="$GCP_NETWORK" \
    --network-tags="a"
cleanup_ce "${CE_BASE_NAME}-nettags-single"

echo ""

# ---------------------------------------------------------------------------
# 3. GOOGLE BATCH CE — MACHINE TYPES
# ---------------------------------------------------------------------------
log "=== 3. Google Batch CE — Machine Types ==="

# 3.1 Head job machine type
expect_success "Add CE with head job machine type" \
    compute-envs add google-batch -n "${CE_BASE_NAME}-machine-types" "$WORKSPACE_FLAG" \
    --work-dir="$GCP_WORK_DIR" -l "$GCP_LOCATION" \
    --head-job-machine-type="n2-standard-4"

# 3.2 Compute jobs machine types (multiple)
expect_success "Add CE with compute jobs machine types" \
    compute-envs add google-batch -n "${CE_BASE_NAME}-machine-multi" "$WORKSPACE_FLAG" \
    --work-dir="$GCP_WORK_DIR" -l "$GCP_LOCATION" \
    --compute-jobs-machine-type="n2-standard-8,c2-standard-4"
cleanup_ce "${CE_BASE_NAME}-machine-multi"

# 3.3 Wildcard machine type for compute jobs
expect_success "Add CE with wildcard compute machine type" \
    compute-envs add google-batch -n "${CE_BASE_NAME}-machine-wildcard" "$WORKSPACE_FLAG" \
    --work-dir="$GCP_WORK_DIR" -l "$GCP_LOCATION" \
    --compute-jobs-machine-type="n2-*"

# 3.4 Validation: head machine type + template mutually exclusive
expect_failure "Head machine type and template are mutually exclusive" \
    "mutually exclusive" \
    compute-envs add google-batch -n "${CE_BASE_NAME}-bad" "$WORKSPACE_FLAG" \
    --work-dir="$GCP_WORK_DIR" -l "$GCP_LOCATION" \
    --head-job-machine-type="n2-standard-4" \
    --head-job-template="projects/p/global/instanceTemplates/t"

# 3.5 Validation: compute machine type + template mutually exclusive
expect_failure "Compute machine type and template are mutually exclusive" \
    "mutually exclusive" \
    compute-envs add google-batch -n "${CE_BASE_NAME}-bad" "$WORKSPACE_FLAG" \
    --work-dir="$GCP_WORK_DIR" -l "$GCP_LOCATION" \
    --compute-jobs-machine-type="n2-standard-8" \
    --compute-job-template="projects/p/global/instanceTemplates/t"

# 3.6 Validation: wildcard rejected for head job
expect_failure "Head job rejects wildcard machine type" \
    "Wildcard machine type families are not supported for the head job" \
    compute-envs add google-batch -n "${CE_BASE_NAME}-bad" "$WORKSPACE_FLAG" \
    --work-dir="$GCP_WORK_DIR" -l "$GCP_LOCATION" \
    --head-job-machine-type="n2-*"

# 3.7 Validation: invalid machine type format
expect_failure "Rejects invalid machine type format" \
    "Invalid machine type" \
    compute-envs add google-batch -n "${CE_BASE_NAME}-bad" "$WORKSPACE_FLAG" \
    --work-dir="$GCP_WORK_DIR" -l "$GCP_LOCATION" \
    --head-job-machine-type="N2-Standard-4"

echo ""

# ---------------------------------------------------------------------------
# 4. GOOGLE BATCH CE — BOOT DISK IMAGE
# ---------------------------------------------------------------------------
log "=== 4. Google Batch CE — Boot Disk Image ==="

# 4.1 Full image path
expect_success "Add CE with boot disk image (full path)" \
    compute-envs add google-batch -n "${CE_BASE_NAME}-boot-image" "$WORKSPACE_FLAG" \
    --work-dir="$GCP_WORK_DIR" -l "$GCP_LOCATION" \
    --boot-disk-image="projects/ubuntu-os-cloud/global/images/ubuntu-2404-noble-amd64-v20250112"

# 4.2 Image family path
expect_success "Add CE with boot disk image (family)" \
    compute-envs add google-batch -n "${CE_BASE_NAME}-boot-family" "$WORKSPACE_FLAG" \
    --work-dir="$GCP_WORK_DIR" -l "$GCP_LOCATION" \
    --boot-disk-image="projects/ubuntu-os-cloud/global/images/family/ubuntu-2404-lts"
cleanup_ce "${CE_BASE_NAME}-boot-family"

# 4.3 Batch short name
expect_success "Add CE with boot disk image (batch short name)" \
    compute-envs add google-batch -n "${CE_BASE_NAME}-boot-batch" "$WORKSPACE_FLAG" \
    --work-dir="$GCP_WORK_DIR" -l "$GCP_LOCATION" \
    --boot-disk-image="batch-debian"
cleanup_ce "${CE_BASE_NAME}-boot-batch"

# 4.4 Validation: invalid image format
expect_failure "Rejects invalid boot disk image" \
    "Invalid boot disk image format" \
    compute-envs add google-batch -n "${CE_BASE_NAME}-bad" "$WORKSPACE_FLAG" \
    --work-dir="$GCP_WORK_DIR" -l "$GCP_LOCATION" \
    --boot-disk-image="not/a/valid/image"

echo ""

# ---------------------------------------------------------------------------
# 5. GOOGLE BATCH CE — FUSION SNAPSHOTS
# ---------------------------------------------------------------------------
log "=== 5. Google Batch CE — Fusion Snapshots ==="

# 5.1 Fusion snapshots with Fusion v2 + Wave
expect_success "Add CE with Fusion Snapshots" \
    compute-envs add google-batch -n "${CE_BASE_NAME}-fusion-snap" "$WORKSPACE_FLAG" \
    --work-dir="$GCP_WORK_DIR" -l "$GCP_LOCATION" \
    --fusion-v2 --wave --fusion-snapshots

# 5.2 Validation: snapshots without Fusion v2
expect_failure "Fusion Snapshots requires Fusion v2" \
    "Fusion Snapshots requires Fusion v2" \
    compute-envs add google-batch -n "${CE_BASE_NAME}-bad" "$WORKSPACE_FLAG" \
    --work-dir="$GCP_WORK_DIR" -l "$GCP_LOCATION" \
    --wave --fusion-snapshots

echo ""

# ---------------------------------------------------------------------------
# 6. COMBINED — ALL FEATURES TOGETHER
# ---------------------------------------------------------------------------
log "=== 6. Combined — All Features Together ==="

expect_success "Add CE with all new features combined" \
    compute-envs add google-batch -n "${CE_BASE_NAME}-all-features" "$WORKSPACE_FLAG" \
    --work-dir="$GCP_WORK_DIR" -l "$GCP_LOCATION" \
    --fusion-v2 --wave --fusion-snapshots \
    --network="$GCP_NETWORK" \
    --network-tags="allow-ssh,web-tier" \
    --compute-jobs-machine-type="n2-standard-8,c2-standard-4" \
    --head-job-machine-type="n2-standard-4" \
    --boot-disk-image="batch-debian"

echo ""

# ===========================================================================
# RESULTS
# ===========================================================================
log "=== Test Results ==="
echo "  Passed:  $PASSED"
echo "  Failed:  $FAILED"
echo "  Skipped: $SKIPPED"
echo ""

if [[ $FAILED -gt 0 ]]; then
    log "=== Failed Tests ==="
    for err in "${ERRORS[@]}"; do
        echo "  - $err"
    done
    echo ""
    exit 1
fi

log "All tests passed."
