#!/bin/bash
set -e

echo "Validating Javadoc integration..."

# Check if Javadoc files exist (they should be generated during build)
JAVADOC_DIR="docs/content/modules/ROOT/attachments/javadoc"

if [ ! -d "$JAVADOC_DIR" ]; then
    echo "⚠️ WARNING: Javadoc directory not found at $JAVADOC_DIR"
    echo "This is expected if you haven't run the build yet."
    echo "Javadoc files are generated dynamically during build and should not be committed to the repository."
    echo ""
    echo "To generate Javadoc files, run:"
    echo "  ./gradlew :docs:build"
    echo ""
    echo "Skipping validation checks that require generated files..."
    SKIP_FILE_CHECKS=true
else
    echo "✓ Found Javadoc directory at $JAVADOC_DIR"
    SKIP_FILE_CHECKS=false
fi

# Check for required index files (only if Javadoc exists)
if [ "$SKIP_FILE_CHECKS" = false ]; then
    required_files=(
        "$JAVADOC_DIR/aggregate/index.html"
        "$JAVADOC_DIR/modules/redis-om-spring/index.html"
        "$JAVADOC_DIR/modules/redis-om-spring-ai/index.html"
    )

    for file in "${required_files[@]}"; do
        if [ ! -f "$file" ]; then
            echo "ERROR: Required Javadoc file not found: $file"
            exit 1
        else
            echo "✓ Found: $file"
        fi
    done
fi

# Check for important package documentation (only if Javadoc exists)
if [ "$SKIP_FILE_CHECKS" = false ]; then
    important_packages=(
        "$JAVADOC_DIR/modules/redis-om-spring/com/redis/om/spring/annotations/package-summary.html"
        "$JAVADOC_DIR/modules/redis-om-spring/com/redis/om/spring/repository/package-summary.html"
        "$JAVADOC_DIR/modules/redis-om-spring-ai/com/redis/om/spring/annotations/package-summary.html"
        "$JAVADOC_DIR/modules/redis-om-spring-ai/com/redis/om/spring/vectorize/package-summary.html"
    )

    echo "Checking for key package documentation..."
    for package in "${important_packages[@]}"; do
        if [ ! -f "$package" ]; then
            echo "⚠ Warning: Package documentation not found: $package"
        else
            echo "✓ Found package docs: $package"
        fi
    done
fi

# Validate that important classes have documentation (only if Javadoc exists)
if [ "$SKIP_FILE_CHECKS" = false ]; then
    important_classes=(
        "$JAVADOC_DIR/modules/redis-om-spring/com/redis/om/spring/annotations/Document.html"
        "$JAVADOC_DIR/modules/redis-om-spring/com/redis/om/spring/repository/RedisDocumentRepository.html"
        "$JAVADOC_DIR/modules/redis-om-spring-ai/com/redis/om/spring/annotations/Vectorize.html"
        "$JAVADOC_DIR/modules/redis-om-spring-ai/com/redis/om/spring/vectorize/DefaultEmbedder.html"
    )

    echo "Checking for key class documentation..."
    for class in "${important_classes[@]}"; do
        if [ ! -f "$class" ]; then
            echo "⚠ Warning: Class documentation not found: $class"
        else
            echo "✓ Found class docs: $class"
        fi
    done
fi

# Check that generated site includes Javadoc assets
SITE_DIR="docs/build/site"
if [ -d "$SITE_DIR" ]; then
    echo "Checking site build includes Javadoc assets..."
    javadoc_assets=$(find "$SITE_DIR" -path "*/_attachments/javadoc/*" -name "*.html" | wc -l)
    echo "Found $javadoc_assets Javadoc HTML files in built site"
    
    if [ "$javadoc_assets" -eq 0 ]; then
        echo "⚠ Warning: No Javadoc assets found in built site"
    else
        echo "✓ Javadoc assets included in site build"
    fi
else
    echo "⚠ Warning: Site build directory not found, skipping site validation"
fi

echo "✅ Javadoc validation completed!"
echo ""
echo "Summary:"
echo "- Required index files: $(echo "${required_files[@]}" | wc -w) checked"
echo "- Package documentation: $(echo "${important_packages[@]}" | wc -w) checked"  
echo "- Class documentation: $(echo "${important_classes[@]}" | wc -w) checked"
echo ""
echo "To view the Javadoc locally:"
echo "1. Run: ./gradlew aggregateJavadoc generateModuleJavadocs"
echo "2. Open: build/docs/javadoc/aggregate/index.html"
echo "3. Or run docs build: ./gradlew :docs:generateSite"
echo "4. Open: docs/build/site/redis-om-spring/current/_attachments/javadoc/aggregate/index.html"