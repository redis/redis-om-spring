# Redis OM Spring Documentation

Documentation site for Redis OM Spring, based on [Antora](https://antora.org/).

## Building the Documentation

### Prerequisites

- Node.js 16+
- npm
- Java 17+

### Installation

```bash
npm install
```

### Building the Site

#### Using Gradle (Recommended)

```bash
# From the project root - includes Javadoc generation
./gradlew :docs:build

# From the docs directory
../gradlew build
```

This builds both the documentation site and integrates the API documentation (Javadoc) automatically. The documentation will be built in the `build/site` directory.

#### Building with Javadoc Integration

To explicitly build with all API documentation:

```bash
# Generate Javadoc first, then build docs
./gradlew aggregateJavadoc generateModuleJavadocs :docs:build

# Or build everything in one command (same as above)
./gradlew :docs:build
```

**Important**: Javadoc files are generated dynamically during the build process and are **NOT** checked into the repository. They are created fresh each time you build and are properly ignored by Git.

#### Using npm directly (without Javadoc)

```bash
npx antora antora-playbook.yml --to-dir=build/site
```

**Note**: Building with npm directly will not include API documentation (Javadoc). Use the Gradle method for complete documentation including API references.

### Viewing the Documentation

After building, you can view the documentation by opening any of these files in your browser:

- `build/site/index.html` - Documentation homepage
- `build/site/redis-om-spring/current/index.html` - Current version documentation
- `build/site/redis-om-spring/current/overview.html` - Overview page
- `build/site/redis-om-spring/current/api-reference.html` - API Reference page (includes Javadoc links)

#### API Documentation Access

When built with Gradle (including Javadoc), the API documentation is available at:

- **Complete API**: `build/site/redis-om-spring/current/_attachments/javadoc/aggregate/index.html`
- **Core Module**: `build/site/redis-om-spring/current/_attachments/javadoc/modules/redis-om-spring/index.html`  
- **AI Module**: `build/site/redis-om-spring/current/_attachments/javadoc/modules/redis-om-spring-ai/index.html`

## Content Structure

The documentation content is organized as follows:

- `content/` - Source content
  - `antora.yml` - Content manifest
  - `modules/` - Documentation modules
    - `ROOT/` - Main module
      - `nav.adoc` - Navigation sidebar
      - `pages/` - Documentation pages
      - `images/` - Images used in documentation

## Development

### Docker Setup

The repository includes a Docker setup that uses Nginx to serve the documentation site. This approach provides a more production-like environment for testing the site.

#### Prerequisites for Docker Setup

- Docker and Docker Compose installed
- Documentation site built (see Building the Site section above)

#### Serving with Docker

```bash
# Option 1: Using Makefile (recommended)
cd docs
make serve

# Option 2: Manual steps
./gradlew :docs:build                # Build with Javadoc integration
cd docs
docker compose up -d                 # Start container
```

The documentation will be available at http://localhost:8000.

**Important**: Use `./gradlew :docs:build` to ensure API documentation is included. Using `npx antora` directly will serve the site without Javadoc integration.

#### Quick Testing

For comprehensive local testing including Javadoc validation:

```bash
cd docs
make test
# or manually:
./scripts/test-local-docs.sh
```

This script will:
- Build the complete documentation with Javadoc
- Validate all API documentation is present
- Start the Docker container
- Test all key URLs
- Provide direct links for testing

#### Docker Configuration

The Docker setup uses:
- A lightweight Nginx Alpine image
- Container name: `roms-documentation`
- Custom Nginx configuration with optimized settings for static sites
- Volume mounting of the `build/site` directory
- Port 8000 exposed to the host

#### Stopping the Docker Container

```bash
docker-compose down
```

#### Troubleshooting Docker Setup

If you encounter issues with the Docker setup:

1. Ensure the `build/site` directory exists and contains the built documentation
2. Check if port 8000 is already in use on your machine
3. Verify Docker and Docker Compose are installed correctly
4. Look at the Docker logs with `docker-compose logs` or `docker logs roms-documentation`
5. For missing API documentation, ensure you built with `./gradlew :docs:build` (not npm directly)
6. Validate Javadoc integration with `./docs/scripts/validate-javadoc.sh`

### Adding New Content

To add a new page:

1. Create a new AsciiDoc file in `content/modules/ROOT/pages/`
2. Add the page to the navigation in `content/modules/ROOT/nav.adoc`
3. Rebuild the site

## Current Content Status

- Overview and general information pages are complete
- AI and Vector Search pages are well-developed
- Many other sections require completion

## Feature Documentation Status

- [x] Overview/Introduction
- [x] Why Redis OM Spring
- [x] Setup/Configuration
- [x] Vector Search (including Azure OpenAI)
- [x] AI Integration
- [x] Quick Start Example
- [ ] Core Concepts (partially complete)
- [ ] Redis Hashes (partially complete)
- [ ] Redis JSON (partially complete)
- [ ] Search & Indexing (partially complete)
- [ ] Entity Streams (partially complete)
- [ ] Aggregations (partially complete)
- [ ] Probabilistic Data Structures
- [ ] Testing & Development

## Future Improvements

- Complete all reference documentation pages
- Add diagrams to explain complex concepts
- Include more examples from demo applications
- Standardize page structure and formatting
- Add navigation breadcrumbs
- ~~Integrate automated API documentation~~ ✅ **Complete** (Javadoc integration implemented)

## Javadoc Integration

The documentation site includes automated API reference generation:

### ✅ **Build Process**
- Javadoc files are **generated dynamically** during build (NOT checked into repository)
- Integrated into Antora as attachments for proper URL handling
- Automatic generation in GitHub Actions workflow
- Local builds regenerate fresh Javadoc

### 📁 **Repository Management**  
- Generated files are properly ignored in `.gitignore`
- Source code comments are the source of truth
- No manual maintenance of API documentation required
- Fresh generation ensures docs stay synchronized with code

### 🔧 **Local Development**
- Use `make clean && make serve` for full rebuild with Javadoc
- Use validation script: `./scripts/validate-javadoc.sh`
- Clean generated files: `./scripts/clean-generated-javadoc.sh`

## Versioning

The documentation shows the current Redis OM Spring release version. The versioning approach works as follows:

### Version Configuration

- The documentation uses `current` as the URL path segment in `content/antora.yml`
- The displayed version (`display_version`) in the UI is set to match the actual release (e.g., "1.0.0-RC2")
- This approach ensures stable URLs while still showing the accurate version number to users

### Multiple Version Support

For future implementation of multiple versions:

1. The recommendation is to use Git branches for versioning (one branch per major/minor version)
2. Each branch would have its own `antora.yml` file with the appropriate version information
3. The playbook configuration would need to be updated to include all version branches

### Implementation Notes

- Currently, the documentation is accessed at `/redis-om-spring/current/`
- The display version in the UI shows the actual release version
- The `redis-om-version` attribute in the asciidoc configuration is set to match the current release version

## Troubleshooting

If you encounter any issues:

1. Make sure you have the correct Node.js version (16+)
2. Clear the cache with `rm -rf .cache/`
3. For Gradle builds, run `./gradlew :docs:clean` first
4. Ensure the output directory exists before serving with Docker
5. Check the antora-playbook.yml for site configuration settings