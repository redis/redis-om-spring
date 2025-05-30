# Redis OM Spring Documentation Makefile
# Provides convenient commands for local development

.PHONY: help build build-fast serve test validate clean stop

# Default target
help:
	@echo "Redis OM Spring Documentation Commands"
	@echo "======================================"
	@echo ""
	@echo "Development Commands:"
	@echo "  make build      - Build complete documentation with Javadoc (recommended)"
	@echo "  make build-fast - Build documentation without Javadoc (faster)"
	@echo "  make serve      - Build and serve documentation locally at http://localhost:8000"
	@echo "  make test       - Run comprehensive local testing"
	@echo "  make validate   - Validate Javadoc integration"
	@echo ""
	@echo "Maintenance Commands:"
	@echo "  make clean      - Clean all build artifacts"
	@echo "  make stop       - Stop Docker container"
	@echo ""
	@echo "Quick Start:"
	@echo "  make serve      # Build everything and start local server"
	@echo ""

# Build complete documentation with Javadoc integration
build:
	@echo "🔨 Building complete documentation with Javadoc integration..."
	cd .. && ./gradlew :docs:build
	@echo "✅ Build complete! Documentation is in build/site/"

# Build documentation without Javadoc (faster for content development)
build-fast:
	@echo "🔨 Building documentation without Javadoc (faster)..."
	@echo "⚠️  Note: API Reference links will be broken without Javadoc"
	npx antora antora-playbook.yml --to-dir=build/site
	@echo "✅ Fast build complete! Documentation is in build/site/"

# Build and serve with Docker
serve: build
	@echo "🐳 Starting local documentation server..."
	@echo "📖 Documentation will be available at http://localhost:8000"
	@echo ""
	@echo "📄 Key URLs:"
	@echo "   Main Site: http://localhost:8000"
	@echo "   Current Docs: http://localhost:8000/redis-om-spring/current/"
	@echo "   API Reference: http://localhost:8000/redis-om-spring/current/api-reference.html"
	@echo ""
	@echo "Press Ctrl+C to stop the server"
	docker-compose up

# Run comprehensive testing
test:
	@echo "🧪 Running comprehensive local testing..."
	./scripts/test-local-docs.sh

# Validate Javadoc integration
validate:
	@echo "✅ Validating Javadoc integration..."
	./scripts/validate-javadoc.sh

# Clean all build artifacts
clean:
	@echo "🧹 Cleaning build artifacts..."
	cd .. && ./gradlew clean
	rm -rf build/site
	rm -rf content/modules/ROOT/assets/javadoc
	rm -rf node_modules/.cache
	@echo "✅ Clean complete!"

# Stop Docker container
stop:
	@echo "🛑 Stopping documentation server..."
	docker-compose down
	@echo "✅ Server stopped!"

# Development workflow targets
dev-setup: clean build
	@echo "🚀 Development environment ready!"
	@echo "   Run 'make serve' to start the server"

# Quick validation for CI/development
quick-test: build validate
	@echo "✅ Quick validation complete!"