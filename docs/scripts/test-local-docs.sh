#!/bin/bash
set -e

echo "🚀 Testing Redis OM Spring Documentation with Javadoc Integration"
echo "=================================================================="

# Check prerequisites
echo ""
echo "📋 Checking prerequisites..."

# Check Java
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
    echo "✓ Java $JAVA_VERSION detected"
else
    echo "❌ Java not found. Please install Java 17+"
    exit 1
fi

# Check Docker
if command -v docker &> /dev/null; then
    echo "✓ Docker detected"
else
    echo "❌ Docker not found. Please install Docker"
    exit 1
fi

# Check Docker Compose
if command -v docker-compose &> /dev/null || docker compose version &> /dev/null 2>&1; then
    echo "✓ Docker Compose detected"
else
    echo "❌ Docker Compose not found. Please install Docker Compose"
    exit 1
fi

# Navigate to project root
cd "$(dirname "$0")/../.."
PROJECT_ROOT=$(pwd)
echo "📁 Project root: $PROJECT_ROOT"

# Clean previous builds
echo ""
echo "🧹 Cleaning previous builds..."
./gradlew clean
rm -rf docs/build/site
rm -rf docs/content/modules/ROOT/assets/javadoc

# Build with Javadoc integration
echo ""
echo "🔨 Building documentation with Javadoc integration..."
echo "This may take a few minutes on first run..."
./gradlew :docs:build

# Validate build
echo ""
echo "✅ Validating build output..."

if [ ! -d "docs/build/site" ]; then
    echo "❌ Build failed: docs/build/site directory not found"
    exit 1
fi

if [ ! -f "docs/build/site/index.html" ]; then
    echo "❌ Build failed: No index.html found in build output"
    exit 1
fi

echo "✓ Documentation site built successfully"

# Check for Javadoc assets
JAVADOC_COUNT=$(find docs/build/site -path "*/_attachments/javadoc/*" -name "*.html" | wc -l)
if [ "$JAVADOC_COUNT" -eq 0 ]; then
    echo "❌ No Javadoc assets found in built site"
    echo "   Running validation script for more details..."
    ./docs/scripts/validate-javadoc.sh
    exit 1
else
    echo "✓ Found $JAVADOC_COUNT Javadoc HTML files in built site"
fi

# Check API reference page
if [ ! -f "docs/build/site/redis-om-spring/current/api-reference.html" ]; then
    echo "❌ API Reference page not found in built site"
    exit 1
else
    echo "✓ API Reference page found"
fi

# Check key Javadoc entry points
JAVADOC_ENTRIES=(
    "docs/build/site/redis-om-spring/current/_attachments/javadoc/aggregate/index.html"
    "docs/build/site/redis-om-spring/current/_attachments/javadoc/modules/redis-om-spring/index.html"
    "docs/build/site/redis-om-spring/current/_attachments/javadoc/modules/redis-om-spring-ai/index.html"
)

echo ""
echo "🔍 Checking key Javadoc entry points..."
for entry in "${JAVADOC_ENTRIES[@]}"; do
    if [ -f "$entry" ]; then
        echo "✓ Found: $(basename $(dirname $entry))/$(basename $entry)"
    else
        echo "❌ Missing: $entry"
        exit 1
    fi
done

# Start Docker container
echo ""
echo "🐳 Starting Docker container..."
cd docs

# Stop any existing container
docker-compose down 2>/dev/null || true

# Start the container
docker-compose up -d

# Wait for container to be ready
echo "⏳ Waiting for container to be ready..."
sleep 3

# Test if site is accessible
MAX_RETRIES=10
RETRY_COUNT=0
while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    if curl -f -s http://localhost:8000 > /dev/null; then
        echo "✓ Documentation site is accessible at http://localhost:8000"
        break
    else
        echo "   Waiting for site to be ready... (attempt $((RETRY_COUNT+1))/$MAX_RETRIES)"
        sleep 2
        RETRY_COUNT=$((RETRY_COUNT+1))
    fi
done

if [ $RETRY_COUNT -eq $MAX_RETRIES ]; then
    echo "❌ Site not accessible after $MAX_RETRIES attempts"
    echo "   Checking Docker logs..."
    docker-compose logs
    exit 1
fi

# Test key pages
echo ""
echo "🌐 Testing key documentation pages..."

PAGES_TO_TEST=(
    "http://localhost:8000"
    "http://localhost:8000/redis-om-spring/current/"
    "http://localhost:8000/redis-om-spring/current/api-reference.html"
    "http://localhost:8000/redis-om-spring/current/_attachments/javadoc/aggregate/"
    "http://localhost:8000/redis-om-spring/current/_attachments/javadoc/modules/redis-om-spring/"
    "http://localhost:8000/redis-om-spring/current/_attachments/javadoc/modules/redis-om-spring-ai/"
)

for page in "${PAGES_TO_TEST[@]}"; do
    if curl -f -s "$page" > /dev/null; then
        echo "✓ Accessible: $page"
    else
        echo "❌ Failed to access: $page"
        # Don't exit here, just warn - some redirects might be expected
    fi
done

# Success message
echo ""
echo "🎉 SUCCESS! Documentation with Javadoc integration is running locally"
echo ""
echo "📖 Access the documentation at:"
echo "   📄 Main Site: http://localhost:8000"
echo "   📄 Current Docs: http://localhost:8000/redis-om-spring/current/"
echo "   📄 API Reference: http://localhost:8000/redis-om-spring/current/api-reference.html"
echo ""
echo "🔧 API Documentation Direct Access:"
echo "   📚 Complete API: http://localhost:8000/redis-om-spring/current/_attachments/javadoc/aggregate/"
echo "   📚 Core Module: http://localhost:8000/redis-om-spring/current/_attachments/javadoc/modules/redis-om-spring/"
echo "   📚 AI Module: http://localhost:8000/redis-om-spring/current/_attachments/javadoc/modules/redis-om-spring-ai/"
echo ""
echo "⚡ To stop the container run: docker-compose down (from docs directory)"
echo ""
echo "✨ Test the navigation from the main docs to the API reference!"
echo "   1. Go to http://localhost:8000/redis-om-spring/current/"
echo "   2. Click 'API Reference' in the left navigation"
echo "   3. Click any of the Javadoc links to test integration"