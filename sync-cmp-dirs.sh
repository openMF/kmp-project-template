#!/bin/bash

# sync-cmp-dirs.sh
# Script to sync specific CMP directories from upstream repository

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

# Simple progress indicator function
show_progress() {
    echo -ne "${BLUE}[                    ]${NC}\r"
    echo -ne "${BLUE}[=====               ]${NC}\r"
    sleep 0.1
    echo -ne "${BLUE}[==========          ]${NC}\r"
    sleep 0.1
    echo -ne "${BLUE}[===============     ]${NC}\r"
    sleep 0.1
    echo -ne "${BLUE}[====================]${NC}"
    echo
}

# Fancy banner
print_banner() {
    echo -e "${BLUE}╔════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║${BOLD}           CMP Directory Sync Tool           ${NC}${BLUE}║${NC}"
    echo -e "${BLUE}╚════════════════════════════════════════════╝${NC}"
    echo
}

# Print step with color and symbol
print_step() {
    echo -e "${GREEN}${CHECKMARK} $1${NC}"
}

# Print warning with color
print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

# Print error with color
print_error() {
    echo -e "${RED}${CROSS} $1${NC}"
}

# Function to generate unique branch name
get_unique_branch_name() {
    local base_name="temp-sync-branch"
    local counter=0
    local branch_name="$base_name"

    while git show-ref --verify --quiet "refs/heads/$branch_name"; do
        counter=$((counter + 1))
        branch_name="${base_name}-${counter}"
    done

    echo "$branch_name"
}

# Print directories to be synced
print_dirs() {
    echo -e "${BLUE}Directories to sync:${NC}"
    for dir in "${CMP_DIRS[@]}"; do
        echo -e "  ${BOLD}→${NC} $dir"
    done
    echo
}

# Main script
clear
print_banner

# Directories to sync
CMP_DIRS=(
    "cmp-android"
    "cmp-desktop"
    "cmp-ios"
    "cmp-web"
)

print_dirs

# Check if upstream remote exists
if ! git remote | grep -q '^upstream$'; then
    print_warning "Upstream remote not found."
    echo -e "${YELLOW}Default upstream URL:${NC} ${BOLD}$DEFAULT_UPSTREAM_URL${NC}"
    echo -e "${YELLOW}Press Enter to use default URL or input a different one:${NC}"
    read -r custom_url

    upstream_url=${custom_url:-$DEFAULT_UPSTREAM_URL}
    print_step "Adding upstream remote: ${BOLD}$upstream_url${NC}"
    git remote add upstream "$upstream_url"
    show_progress
fi

# Get unique temporary branch name
TEMP_BRANCH=$(get_unique_branch_name)
print_step "Using temporary branch: ${BOLD}$TEMP_BRANCH${NC}"

# Main sync process
echo -e "\n${BLUE}${BOLD}Starting sync process...${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}\n"

print_step "Fetching from upstream..."
git fetch upstream
show_progress

print_step "Creating temporary branch..."
git checkout -b "$TEMP_BRANCH" upstream/dev
show_progress

print_step "Switching back to dev branch..."
git checkout dev
show_progress

echo -e "\n${BLUE}${BOLD}Syncing directories...${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}\n"

for dir in "${CMP_DIRS[@]}"; do
    if [ -d "$dir" ]; then
        print_step "Syncing ${BOLD}$dir${NC}..."
        git checkout "$TEMP_BRANCH" -- "$dir"
    else
        print_warning "Directory ${BOLD}$dir${NC} not found. Creating it..."
        mkdir -p "$dir"
        git checkout "$TEMP_BRANCH" -- "$dir"
    fi
    show_progress
done

print_step "Cleaning up temporary branch..."
git branch -D "$TEMP_BRANCH"
show_progress

print_step "Committing changes..."
git add "${CMP_DIRS[@]}"
git commit -m "sync: Update CMP directories from upstream"
show_progress

echo -e "\n${YELLOW}${BOLD}Would you like to push the changes? (y/n)${NC}"
read -r response
if [[ "$response" =~ ^[Yy]$ ]]; then
    print_step "Pushing to origin..."
    git push origin dev
    show_progress
    echo -e "\n${GREEN}${BOLD}✨ Sync completed successfully! ✨${NC}\n"
else
    echo -e "\n${YELLOW}Changes committed but not pushed. You can push later with:${NC}"
    echo -e "${BOLD}git push origin dev${NC}\n"
fi