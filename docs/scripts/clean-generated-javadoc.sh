#!/bin/bash
set -e

echo "🧹 Cleaning Generated Javadoc Files from Repository"
echo "=================================================="

# Navigate to project root
cd "$(dirname "$0")/../.."
PROJECT_ROOT=$(pwd)
echo "📁 Project root: $PROJECT_ROOT"

echo ""
echo "🗑️ Removing generated Javadoc files..."

# Remove generated attachments
if [ -d "docs/content/modules/ROOT/attachments/javadoc" ]; then
    echo "   Removing docs/content/modules/ROOT/attachments/javadoc/"
    rm -rf docs/content/modules/ROOT/attachments/javadoc/
    echo "   ✓ Removed attachments/javadoc/"
else
    echo "   ✓ No attachments/javadoc/ directory found"
fi

# Remove any old assets
if [ -d "docs/content/modules/ROOT/assets/javadoc" ]; then
    echo "   Removing docs/content/modules/ROOT/assets/javadoc/"
    rm -rf docs/content/modules/ROOT/assets/javadoc/
    echo "   ✓ Removed assets/javadoc/"
else
    echo "   ✓ No assets/javadoc/ directory found"
fi

# Remove build directory
if [ -d "docs/build" ]; then
    echo "   Removing docs/build/"
    rm -rf docs/build/
    echo "   ✓ Removed build directory"
else
    echo "   ✓ No build directory found"
fi

# Remove root build directory  
if [ -d "build" ]; then
    echo "   Removing root build/"
    rm -rf build/
    echo "   ✓ Removed root build directory"
else
    echo "   ✓ No root build directory found"
fi

echo ""
echo "📋 Checking .gitignore status..."

# Check if .gitignore has the entries
if grep -q "docs/content/modules/ROOT/attachments/javadoc/" .gitignore; then
    echo "   ✓ .gitignore already includes attachments/javadoc/"
else
    echo "   ⚠️ .gitignore missing attachments/javadoc/ entry"
fi

if grep -q "docs/content/modules/ROOT/assets/javadoc/" .gitignore; then
    echo "   ✓ .gitignore already includes assets/javadoc/"
else
    echo "   ⚠️ .gitignore missing assets/javadoc/ entry"
fi

echo ""
echo "✅ Cleanup completed!"
echo ""
echo "🔄 What happens next:"
echo "   1. Generated Javadoc files are removed from the repository"
echo "   2. They are properly ignored by Git (.gitignore)"
echo "   3. They will be regenerated dynamically during build process"
echo "   4. GitHub Actions will generate them fresh for each deployment"
echo ""
echo "📝 To regenerate locally:"
echo "   ./gradlew :docs:build"
echo ""
echo "🌐 To test the full pipeline:"
echo "   cd docs && make test"