#!/bin/bash
# Script to prepare a new release candidate for redis-om-spring

set -e

if [ "$#" -ne 1 ]; then
  echo "Usage: $0 <new_version>"
  echo "Example: $0 0.6.0"
  exit 1
fi

NEW_VERSION=$1

# Ensure we're in the repository root
cd "$(dirname "$0")/.."

echo "Preparing release v$NEW_VERSION..."

# Update version in all pom.xml files
mvn versions:set -DnewVersion="$NEW_VERSION" -DgenerateBackupPoms=false

echo "Version in POMs has been updated to $NEW_VERSION"
echo ""
echo "Next steps:"
echo "1. Review the changes (git diff)"
echo "2. Run tests: mvn clean verify"
echo "3. Commit the changes: git commit -a -m \"chore: prepare release v$NEW_VERSION\""
echo "4. Create a GitHub release with tag v$NEW_VERSION"
echo "   This will trigger the release workflow."
echo ""