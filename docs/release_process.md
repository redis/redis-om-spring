# Release Process

This document outlines the release process for Redis OM Spring using JReleaser.

## Overview

Redis OM Spring uses [JReleaser](https://jreleaser.org/) to automate the publication of artifacts to Maven Central. The process is triggered by creating a GitHub release, which initiates a GitHub Actions workflow.

## Key Files

The release process involves these key files:

1. `.github/workflows/release.yml` - GitHub Actions workflow triggered by GitHub releases
2. `jreleaser.yml` - JReleaser configuration defining how artifacts are released and/or published

## Release Steps

To release a new version, go to the GitHub repo *Actions* section and click on the [Release workflow](https://github.com/redis/redis-om-spring/actions/workflows/release.yml).

Then click on **Run worflow** and specify the version to release (for example `1.1.0`).

Finally, click on *Run workflow*.
This will start the release process which consists of the following steps:

* Update `gradle.properties` with the specified version.
* Build the project using the Gradle wrapper: `./gradlew build test aggregateTestReport publish`
* Call the JReleaser action which performs the following tasks:
  * Publish the artifacts to Maven Central
  * Tag the repository with the specified version, generate the changelog using [conventional commits](https://www.conventionalcommits.org/en/v1.0.0/), and create a GitHub release
  * If enabled, announce the release to Slack and other channels

## Published Artifacts

The process publishes two main artifacts to Maven Central:

- `redis-om-spring` - Core Redis OM Spring library
- `redis-om-spring-ai` - AI extension for Redis OM Spring

Each artifact includes:
- Main JAR
- Sources JAR
- Javadoc JAR
- POM file

## Troubleshooting

### Check the GitHub actions logs

1. Check the JReleaser output artifact in the GitHub Actions run
2. Verify that all required environment secrets are configured:
   - `GIT_ACCESS_TOKEN` and `GIT_USER` - For GitHub access
   - `GPG_PASSPHRASE`, `GPG_PUBLIC_KEY`, and `GPG_SECRET_KEY` - For signing artifacts
   - `SONATYPE_USERNAME` and `SONATYPE_PASSWORD` - For Maven Central access
3. Review the staging directory output to ensure the correct artifacts are being included
4. If the release fails due to validation issues, fix them and create a new patch release

### Run locally

To troubleshoot the release process locally, run the Gradle command specified above under *Release Steps* and make sure the proper artifacts are created under `build/staging-deploy`.

Then [install JReleaser](https://jreleaser.org/guide/latest/index.html) and set the following environment variables:
* `JRELEASER_PROJECT_VERSION`: Should match version in `gradle.properties` (e.g. `1.0.1`)
* `JRELEASER_GPG_SECRET_KEY`, `JRELEASER_GPG_PUBLIC_KEY`, `JRELEASER_GPG_PASSPHRASE`
* `JRELEASER_GITHUB_TOKEN`
* `JRELEASER_MAVENCENTRAL_USERNAME` and `JRELEASER_MAVENCENTRAL_PASSWORD`

Run jreleaser with the `dry-run` option:

```shell
jreleaser full-release --dry-run
```

