#!/bin/bash

# sync-dirs.sh
# Script to sync directories and files from upstream repository

# Colors and formatting
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BOLD='\033[1m'
NC='\033[0m'    # No Color
CHECKMARK='\xE2\x9C\x94'
CROSS='\xE2\x9C\x98'

# Default upstream URL
DEFAULT_UPSTREAM_URL="https://github.com/openMF/kmp-project-template.git"

# Script options
DRY_RUN=false
FORCE=false
LOG_FILE="sync-$(date +%Y%m%d-%H%M%S).log"

# Logging function
log_message() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" >> "$LOG_FILE"
    echo -e "$1"
}

# Error handling function
handle_error() {
    log_message "${RED}${CROSS} Error: $1${NC}"
    exit 1
}

# Simple progress indicator function
show_progress() {
    if [ "$DRY_RUN" = false ]; then
        echo -ne "${BLUE}[                    ]${NC}\r"
        echo -ne "${BLUE}[=====               ]${NC}\r"
        sleep 0.1
        echo -ne "${BLUE}[==========          ]${NC}\r"
        sleep 0.1
        echo -ne "${BLUE}[===============     ]${NC}\r"
        sleep 0.1
        echo -ne "${BLUE}[====================]${NC}"
        echo
    fi
}

# Fancy banner
print_banner() {
    echo -e "${BLUE}╔════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║${BOLD}        Project Directory Sync Tool         ${NC}${BLUE}║${NC}"
    echo -e "${BLUE}╚════════════════════════════════════════════╝${NC}"
    echo
}

# Print step with color and symbol
print_step() {
    log_message "${GREEN}${CHECKMARK} $1${NC}"
}

# Print warning with color
print_warning() {
    log_message "${YELLOW}⚠ $1${NC}"
}

# Function to generate unique branch name
get_sync_branch_name() {
    local date_suffix=$(date +%Y%m%d-%H%M%S)
    echo "sync/upstream-${date_suffix}"
}

# Print directories and files to be synced
print_items() {
    echo -e "${BLUE}Items to sync:${NC}"
    echo -e "${BOLD}Directories:${NC}"
    for dir in "${SYNC_DIRS[@]}"; do
        echo -e "  ${BOLD}→${NC} $dir"
    done

    echo -e "\n${BOLD}Files:${NC}"
    for file in "${SYNC_FILES[@]}"; do
        echo -e "  ${BOLD}→${NC} $file"
    done
    echo
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --dry-run)
            DRY_RUN=true
            shift
            ;;
        -f|--force)
            FORCE=true
            shift
            ;;
        *)
            handle_error "Unknown option: $1"
            ;;
    esac
done

# Check git configuration
if ! git config user.name > /dev/null || ! git config user.email > /dev/null; then
    handle_error "Git user.name or user.email not configured"
fi

# Main script
clear
print_banner

# Directories and files to sync
SYNC_DIRS=(
    "cmp-android"
    "cmp-desktop"
    "cmp-ios"
    "cmp-web"
    "cmp-shared"
    "build-logic"
    "fastlane"
    "scripts"
    "config"
    ".github"
    ".run"
)

SYNC_FILES=(
    "Gemfile"
    "Gemfile.lock"
    "ci-prepush.bat"
    "ci-prepush.sh"
)

print_items

# Check if upstream remote exists
if ! git remote | grep -q '^upstream$'; then
    print_warning "Upstream remote not found."
    if [ "$DRY_RUN" = false ]; then
        echo -e "${YELLOW}Default upstream URL:${NC} ${BOLD}$DEFAULT_UPSTREAM_URL${NC}"
        if [ "$FORCE" = false ]; then
            echo -e "${YELLOW}Press Enter to use default URL or input a different one:${NC}"
            read -r custom_url
        else
            custom_url=""
        fi

        upstream_url=${custom_url:-$DEFAULT_UPSTREAM_URL}
        print_step "Adding upstream remote: ${BOLD}$upstream_url${NC}"
        git remote add upstream "$upstream_url" || handle_error "Failed to add upstream remote"
        show_progress
    fi
fi

# Fetch from upstream
print_step "Fetching from upstream..."
if ! git fetch upstream; then
    handle_error "Failed to fetch from upstream"
fi
show_progress

# Check if upstream/dev exists
if ! git rev-parse --verify upstream/dev >/dev/null 2>&1; then
    handle_error "upstream/dev branch does not exist"
fi

# Create sync branch from current dev
SYNC_BRANCH=$(get_sync_branch_name)
print_step "Creating sync branch: ${BOLD}$SYNC_BRANCH${NC}"

if [ "$DRY_RUN" = false ]; then
    # Create sync branch from current dev
    if ! git checkout -b "$SYNC_BRANCH" dev; then
        handle_error "Failed to create sync branch"
    fi
    show_progress

    # Create temporary branch for upstream changes
    TEMP_BRANCH="temp-${SYNC_BRANCH}"
    print_step "Creating temporary branch: ${BOLD}$TEMP_BRANCH${NC}"
    if ! git checkout -b "$TEMP_BRANCH" upstream/dev; then
        handle_error "Failed to create temporary branch"
    fi
    show_progress

    # Switch back to sync branch
    print_step "Switching back to sync branch..."
    if ! git checkout "$SYNC_BRANCH"; then
        handle_error "Failed to switch to sync branch"
    fi
    show_progress
fi

echo -e "\n${BLUE}${BOLD}Syncing directories...${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}\n"

# Sync directories
for dir in "${SYNC_DIRS[@]}"; do
    if [ -d "$dir" ]; then
        print_step "Syncing ${BOLD}$dir${NC}..."
        if [ "$DRY_RUN" = false ]; then
            if [ -d "$dir" ]; then
                cp -r "$dir" "$dir.backup"
            fi
            if ! git checkout "$TEMP_BRANCH" -- "$dir"; then
                print_error "Failed to sync $dir"
                [ -d "$dir.backup" ] && mv "$dir.backup" "$dir"
                continue
            fi
            rm -rf "$dir.backup"
        fi
    else
        print_warning "Directory ${BOLD}$dir${NC} not found. Creating it..."
        if [ "$DRY_RUN" = false ]; then
            mkdir -p "$dir"
            git checkout "$TEMP_BRANCH" -- "$dir" || handle_error "Failed to sync $dir"
        fi
    fi
    show_progress
done

echo -e "\n${BLUE}${BOLD}Syncing files...${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}\n"

# Sync individual files
for file in "${SYNC_FILES[@]}"; do
    print_step "Syncing ${BOLD}$file${NC}..."
    if [ "$DRY_RUN" = false ]; then
        if [ -f "$file" ]; then
            cp "$file" "$file.backup"
        fi
        if ! git checkout "$TEMP_BRANCH" -- "$file"; then
            print_error "Failed to sync $file"
            [ -f "$file.backup" ] && mv "$file.backup" "$file"
            continue
        fi
        rm -f "$file.backup"
    fi
    show_progress
done

if [ "$DRY_RUN" = false ]; then
    # Cleanup temporary branch
    print_step "Cleaning up temporary branch..."
    git branch -D "$TEMP_BRANCH" || handle_error "Failed to delete temporary branch"
    show_progress

    # Check for changes
    if ! git diff --quiet --exit-code --cached; then
        print_step "Committing changes..."
        git add "${SYNC_DIRS[@]}" "${SYNC_FILES[@]}"
        git commit -m "sync: Update directories and files from upstream

This PR syncs the following items with upstream:
- Directories: ${SYNC_DIRS[*]}
- Files: ${SYNC_FILES[*]}" || handle_error "Failed to commit changes"
        show_progress

        if [ "$FORCE" = false ]; then
            echo -e "\n${YELLOW}${BOLD}Would you like to push the sync branch? (y/n)${NC}"
            read -r response
            if [[ "$response" =~ ^[Yy]$ ]]; then
                print_step "Pushing sync branch..."
                git push -u origin "$SYNC_BRANCH" || handle_error "Failed to push sync branch"
                show_progress
                echo -e "\n${GREEN}${BOLD}✨ Sync branch pushed successfully! ✨${NC}"
                echo -e "${YELLOW}Please create a pull request from branch ${BOLD}$SYNC_BRANCH${NC}${YELLOW} to ${BOLD}dev${NC}${YELLOW} in your repository.${NC}\n"
            else
                echo -e "\n${YELLOW}Changes committed but not pushed. You can push later with:${NC}"
                echo -e "${BOLD}git push -u origin $SYNC_BRANCH${NC}"
                echo -e "${YELLOW}Then create a pull request from ${BOLD}$SYNC_BRANCH${NC}${YELLOW} to ${BOLD}dev${NC}\n"
            fi
        else
            print_step "Pushing sync branch..."
            git push -u origin "$SYNC_BRANCH" || handle_error "Failed to push sync branch"
            show_progress
            echo -e "\n${GREEN}${BOLD}✨ Sync branch pushed successfully! ✨${NC}"
            echo -e "${YELLOW}Please create a pull request from branch ${BOLD}$SYNC_BRANCH${NC}${YELLOW} to ${BOLD}dev${NC}${YELLOW} in your repository.${NC}\n"
        fi
    else
        print_warning "No changes to commit"
        git checkout dev
        git branch -D "$SYNC_BRANCH"
    fi
else
    echo -e "\n${YELLOW}${BOLD}Dry run completed. No changes were made.${NC}\n"
fi