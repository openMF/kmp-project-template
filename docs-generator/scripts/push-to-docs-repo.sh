#!/bin/bash

################################################################################
# Push Documentation to Separate Repository Script
#
# This script pushes generated documentation to a separate documentation
# repository, which can be hosted on GitHub Pages or any other static hosting.
#
# Usage:
#   ./docs/scripts/push-to-docs-repo.sh [options]
#
# Options:
#   --repo <url>       Documentation repository URL (required if DOCS_REPO not set)
#   --branch <name>    Target branch (default: main)
#   --message <msg>    Custom commit message
#   --force            Force push (use with caution)
#   --dry-run          Show what would be done without making changes
#   --help             Show this help message
#
# Environment Variables:
#   DOCS_REPO          Documentation repository URL
#   DOCS_BRANCH        Target branch (default: main)
#   DOCS_TOKEN         GitHub token for authentication (optional)
#   SOURCE_DIR         Source directory with docs (default: build/docs-output)
#
# Prerequisites:
#   - Documentation must be generated first (run generate-docs.sh)
#   - Git must be installed and configured
#   - Appropriate push permissions for the docs repository
#
# Examples:
#   ./docs/scripts/push-to-docs-repo.sh --repo https://github.com/user/docs
#   DOCS_REPO=git@github.com:user/docs.git ./docs/scripts/push-to-docs-repo.sh
#   ./docs/scripts/push-to-docs-repo.sh --dry-run
#
################################################################################

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default options
DOCS_REPO="${DOCS_REPO:-}"
DOCS_BRANCH="${DOCS_BRANCH:-main}"
DOCS_TOKEN="${DOCS_TOKEN:-}"
SOURCE_DIR="${SOURCE_DIR:-build/docs-output}"
COMMIT_MESSAGE=""
FORCE_PUSH=false
DRY_RUN=false

# Script directory and project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
TEMP_DIR=$(mktemp -d)

################################################################################
# Functions
################################################################################

cleanup() {
    if [ -d "$TEMP_DIR" ]; then
        rm -rf "$TEMP_DIR"
    fi
}

trap cleanup EXIT

print_header() {
    echo -e "${BLUE}================================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}================================================${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ $1${NC}"
}

show_help() {
    sed -n '/^################################################################################/,/^################################################################################/p' "$0" | 
    grep -E '^#' | 
    sed 's/^# \?//'
    exit 0
}

check_requirements() {
    print_header "Checking Requirements"
    
    # Check if git is installed
    if ! command -v git &> /dev/null; then
        print_error "Git is not installed"
        exit 1
    fi
    print_success "Git is installed"
    
    # Check if docs repo is set
    if [ -z "$DOCS_REPO" ]; then
        print_error "Documentation repository not specified"
        echo "Set DOCS_REPO environment variable or use --repo option"
        exit 1
    fi
    print_success "Documentation repository: $DOCS_REPO"
    
    # Check if source directory exists
    if [ ! -d "$PROJECT_ROOT/$SOURCE_DIR" ]; then
        print_error "Source directory not found: $SOURCE_DIR"
        echo "Generate documentation first using generate-docs.sh"
        exit 1
    fi
    print_success "Source directory found: $SOURCE_DIR"
    
    # Check if git is configured
    if ! git config user.name &> /dev/null || ! git config user.email &> /dev/null; then
        print_warning "Git user not configured, setting defaults"
        git config user.name "Documentation Bot"
        git config user.email "docs-bot@users.noreply.github.com"
    fi
    
    echo ""
}

prepare_commit_message() {
    if [ -z "$COMMIT_MESSAGE" ]; then
        # Get current commit info
        CURRENT_COMMIT=$(git -C "$PROJECT_ROOT" rev-parse --short HEAD 2>/dev/null || echo "unknown")
        CURRENT_BRANCH=$(git -C "$PROJECT_ROOT" rev-parse --abbrev-ref HEAD 2>/dev/null || echo "unknown")
        TIMESTAMP=$(date -u +"%Y-%m-%d %H:%M:%S UTC")
        
        COMMIT_MESSAGE="📚 Update documentation

Generated from: $CURRENT_BRANCH@$CURRENT_COMMIT
Timestamp: $TIMESTAMP
Source: $(git -C "$PROJECT_ROOT" config --get remote.origin.url 2>/dev/null || echo "local")"
    fi
}

clone_docs_repo() {
    print_header "Cloning Documentation Repository"
    
    cd "$TEMP_DIR"
    
    # Construct clone URL with token if provided
    CLONE_URL="$DOCS_REPO"
    if [ -n "$DOCS_TOKEN" ]; then
        # Replace https:// with https://token@
        CLONE_URL=$(echo "$DOCS_REPO" | sed "s|https://|https://x-access-token:${DOCS_TOKEN}@|")
    fi
    
    if [ "$DRY_RUN" = true ]; then
        print_info "[DRY RUN] Would clone: $DOCS_REPO"
        mkdir -p docs-repo
        cd docs-repo
        git init
        return
    fi
    
    # Clone the repository
    if git clone --depth 1 --branch "$DOCS_BRANCH" "$CLONE_URL" docs-repo 2>/dev/null; then
        print_success "Cloned existing branch: $DOCS_BRANCH"
    else
        print_warning "Branch $DOCS_BRANCH not found, will create new branch"
        git clone --depth 1 "$CLONE_URL" docs-repo || {
            print_error "Failed to clone repository"
            exit 1
        }
        cd docs-repo
        git checkout -b "$DOCS_BRANCH"
        cd ..
    fi
    
    cd docs-repo
    echo ""
}

copy_documentation() {
    print_header "Copying Documentation"
    
    # Remove all files except .git
    find . -mindepth 1 -maxdepth 1 ! -name '.git' -exec rm -rf {} +
    
    # Copy new documentation
    cp -r "$PROJECT_ROOT/$SOURCE_DIR/"* .
    
    # Create .nojekyll for GitHub Pages
    touch .nojekyll
    
    # Create/update README
    cat > README.md << EOF
# Project Documentation

This repository contains the generated documentation for the project.

## Contents

- **Kotlin API Reference (Dokka)**: Complete API documentation for all modules, generated from source code

## Viewing Documentation

Visit the [documentation site](https://YOUR_USERNAME.github.io/YOUR_DOCS_REPO/) or open \`index.html\` locally.

## Generation

This documentation is automatically generated and published via GitHub Actions.
See the main repository for source code and generation scripts.

**Last Updated**: $(date -u +"%Y-%m-%d %H:%M:%S UTC")
EOF
    
    print_success "Documentation copied"
    echo ""
}

commit_and_push() {
    print_header "Committing and Pushing Changes"
    
    # Stage all changes
    git add .
    
    # Check if there are changes
    if git diff --staged --quiet; then
        print_warning "No changes to commit"
        return 0
    fi
    
    # Show stats
    STATS=$(git diff --staged --stat)
    print_info "Changes to commit:"
    echo "$STATS"
    echo ""
    
    if [ "$DRY_RUN" = true ]; then
        print_info "[DRY RUN] Would commit with message:"
        echo "$COMMIT_MESSAGE"
        echo ""
        print_info "[DRY RUN] Would push to: $DOCS_REPO ($DOCS_BRANCH)"
        return 0
    fi
    
    # Commit changes
    git commit -m "$COMMIT_MESSAGE"
    print_success "Changes committed"
    
    # Push changes
    PUSH_FLAGS=""
    if [ "$FORCE_PUSH" = true ]; then
        PUSH_FLAGS="--force"
        print_warning "Force pushing changes"
    fi
    
    if git push $PUSH_FLAGS origin "$DOCS_BRANCH"; then
        print_success "Changes pushed successfully"
    else
        print_error "Failed to push changes"
        exit 1
    fi
    
    echo ""
}

print_summary() {
    print_header "Documentation Publishing Complete"
    
    if [ "$DRY_RUN" = true ]; then
        echo -e "${YELLOW}DRY RUN COMPLETED - No changes were made${NC}"
    else
        echo -e "${GREEN}Documentation published successfully!${NC}"
    fi
    
    echo ""
    echo "Repository: ${BLUE}$DOCS_REPO${NC}"
    echo "Branch: ${BLUE}$DOCS_BRANCH${NC}"
    echo ""
    
    # Try to extract GitHub Pages URL
    if [[ "$DOCS_REPO" =~ github\.com[:/]([^/]+)/([^/\.]+) ]]; then
        GITHUB_USER="${BASH_REMATCH[1]}"
        GITHUB_REPO="${BASH_REMATCH[2]}"
        echo "If GitHub Pages is enabled, documentation will be available at:"
        echo "  ${BLUE}https://${GITHUB_USER}.github.io/${GITHUB_REPO}/${NC}"
        echo ""
        echo "To enable GitHub Pages:"
        echo "  1. Go to repository Settings → Pages"
        echo "  2. Select branch: ${BLUE}$DOCS_BRANCH${NC}"
        echo "  3. Select folder: ${BLUE}/ (root)${NC}"
        echo "  4. Click Save"
    fi
    
    echo ""
}

################################################################################
# Parse arguments
################################################################################

while [[ $# -gt 0 ]]; do
    case $1 in
        --repo)
            DOCS_REPO="$2"
            shift 2
            ;;
        --branch)
            DOCS_BRANCH="$2"
            shift 2
            ;;
        --message)
            COMMIT_MESSAGE="$2"
            shift 2
            ;;
        --force)
            FORCE_PUSH=true
            shift
            ;;
        --dry-run)
            DRY_RUN=true
            shift
            ;;
        --help|-h)
            show_help
            ;;
        *)
            print_error "Unknown option: $1"
            echo "Use --help to see available options"
            exit 1
            ;;
    esac
done

################################################################################
# Main execution
################################################################################

print_header "Documentation Publishing Script"
echo ""

check_requirements
prepare_commit_message
clone_docs_repo
copy_documentation
commit_and_push
print_summary

exit 0
