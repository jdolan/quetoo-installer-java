# Copilot Instructions

## Prerequisites

Requires **Java 21+** and **Apache Maven 3.9+**.

If Maven is not installed, download and extract it (no admin required):

```powershell
# Download Maven 3.9.8
Invoke-WebRequest -Uri "https://archive.apache.org/dist/maven/maven-3/3.9.8/binaries/apache-maven-3.9.8-bin.zip" -OutFile "$env:TEMP\maven.zip" -UseBasicParsing

# Extract to .tools/ inside the repo (gitignored)
Expand-Archive -Path "$env:TEMP\maven.zip" -DestinationPath ".tools" -Force

# Add to PATH for the current session
$env:PATH = "$PWD\.tools\apache-maven-3.9.8\bin;$env:PATH"
```

Verify with `mvn -version`.

Note: `.mvn/jvm.config` includes `--add-opens` to suppress Maven reflection warnings. On Java 22+, additional harmless warnings from Maven's jansi/guava/sisu libraries may appear — these will be resolved in Maven 4.0.

When running Maven in PowerShell, pipe through `Out-Host` to avoid stderr warnings showing as red errors:

```powershell
mvn package -DskipTests 2>&1 | Out-Host
```

## Build & Test

```bash
# Build (skip tests)
mvn package -DskipTests

# Build with ProGuard minification (used in CI)
mvn -Pproguard package

# Run tests
mvn test

# Run a single test class
mvn test -Dtest=S3SyncTest

# Run a single test method
mvn test -Dtest=S3SyncTest#testQuetoo
```

Requires Java 21+. The Shade plugin produces an uber JAR with all dependencies bundled.

## Architecture

This is a cross-platform installer/updater for the Quetoo game. It synchronizes local files with two AWS S3 buckets using intelligent delta syncing (MD5 hash comparison).

### Reactive Pipeline

The sync pipeline is built on **RxJava 2** Observables, composed in four stages:

```
index() → delta() → sync() → prune()
```

- `index()` — Paginated HTTP listing of an S3 bucket (1000 items/page, marker-based continuation)
- `delta()` — Filters to assets needing download by comparing MD5 ETags against local files
- `sync()` — Downloads changed files with `flatMap(..., 8)` for 8-thread parallelism
- `prune()` — Optionally deletes local files absent from the remote index

`Manager` orchestrates two `S3Sync` instances (one per bucket), merging their observables. The `quetoo` bucket is filtered by platform build and its keys are remapped to local paths; `quetoo-data` syncs everything into `share/`.

### S3 Access

S3 buckets are accessed via **plain HTTP GET** to `https://{bucket}.s3.amazonaws.com/` — there is no AWS SDK dependency. Responses are parsed as XML using a hardened `DocumentBuilder` (external entities disabled). Apache HttpClient 4.x provides a pooled connection manager (12 connections max).

### Interface Hierarchy

Core domain contracts are defined as interfaces (`Sync`, `Asset`, `Index`, `Delta`) with S3-specific implementations (`S3Sync`, `S3Object`, `S3Bucket`, `S3Delta`). This separation keeps the sync pipeline agnostic of the storage backend.

### Dual UI Modes

Both `Console` (headless) and `Panel` (Swing GUI) subscribe to the same `Manager` observables:
- **Console** uses `blockingSubscribe()` for linear execution, printing to stdout/stderr
- **Panel** uses `Schedulers.from(SwingUtilities::invokeLater)` to observe on the Swing thread, updating a progress bar and status labels

## Conventions

- **Builder pattern**: `S3Sync.Builder` for fluent configuration of sync instances
- **Error resilience**: `onErrorResumeNext()` in sync observables logs failures to stderr but continues processing remaining assets
- **ETag validation**: MD5 ETags verify file integrity; multipart upload ETags (containing `-`) fall back to size comparison
- **Platform detection**: `Build` enum resolves the host platform via Apache Commons `SystemUtils`; `Config` resolves the install directory by searching up from the JAR location for app bundle names, falling back to OS-specific defaults (`~/Library/Application Support/Quetoo`, `~/.quetoo`, `%APPDATA%/Quetoo`)
- **Static XML utilities**: The `S3` class provides stateless DOM parsing helpers with security hardening (DOCTYPE disallowed)
