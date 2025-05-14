# Release Process

This document outlines the release process for Redis OM Spring using JReleaser.

## Overview

Redis OM Spring uses [JReleaser](https://jreleaser.org/) to automate the publication of artifacts to Maven Central. The process is triggered by creating a GitHub release, which initiates a GitHub Actions workflow.

## Key Files

The release process involves these key files:

1. `.github/workflows/version-and-release.yml` - GitHub Actions workflow triggered by GitHub releases
2. `jreleaser.yml` - JReleaser configuration defining how artifacts are published
3. `scripts/prepare-release.sh` - Helper script to prepare a new release version

## Release Steps

To release a new version:

1. **Prepare the release**:
   ```bash
   ./scripts/prepare-release.sh <version>
   ```
   This script sets the version in all POM files.

2. **Verify and test**:
   ```bash
   mvn clean verify
   ```
   Ensure all tests pass.

3. **Commit version changes**:
   ```bash
   git commit -a -m "chore: prepare release v<version>"
   git push
   ```

4. **Create a GitHub Release**:
   - Go to GitHub and create a new release
   - Set the tag to `v<version>` (e.g., `v0.6.0`)
   - Provide release notes
   - Publish the release

5. **Workflow Execution**:
   - When the release is published, the GitHub workflow will:
     - Check out the repository
     - Set the version from the release tag
     - Build the artifacts with the publication profile
     - Stage the artifacts for publication (excluding the parent POM)
     - Use JReleaser to sign and publish the artifacts to Maven Central
   
6. **Monitoring**:
   - Monitor the workflow execution in the "Actions" tab
   - Check the JReleaser output artifact for detailed logs

## Published Artifacts

The process publishes two main artifacts to Maven Central:

- `redis-om-spring` - Core Redis OM Spring library
- `redis-om-spring-ai` - AI extension for Redis OM Spring

Each artifact includes:
- Main JAR
- Sources JAR
- Javadoc JAR
- POM file

The parent POM (`redis-om-spring-parent`) is intentionally excluded from publication by only staging the specific artifacts we want to publish.

## Troubleshooting

If the release fails:

1. Check the JReleaser output artifact in the GitHub Actions run
2. Verify that all required environment secrets are configured:
   - `GIT_ACCESS_TOKEN` and `GIT_USER` - For GitHub access
   - `GPG_PASSPHRASE`, `GPG_PUBLIC_KEY`, and `GPG_SECRET_KEY` - For signing artifacts
   - `SONATYPE_USERNAME` and `SONATYPE_PASSWORD` - For Maven Central access
3. Review the staging directory output to ensure the correct artifacts are being included
4. If the release fails due to validation issues, fix them and create a new patch release