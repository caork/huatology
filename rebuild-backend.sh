#!/bin/bash

# Digital Twin Backend Rebuild Script
# This script rebuilds the backend with dependency refresh

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
BACKEND_DIR="backend"

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Main execution
print_status "=== Digital Twin Backend Rebuild ==="

# Check if backend directory exists
if [ ! -d "$BACKEND_DIR" ]; then
    print_error "Backend directory '$BACKEND_DIR' not found"
    exit 1
fi

# Navigate to backend directory
cd "$BACKEND_DIR"

print_status "Refreshing Maven dependencies and rebuilding backend..."

# Clean and install with dependency update
if ./mvnw clean install -U; then
    print_success "Backend rebuilt successfully!"
    print_status "You can now run ./start.sh to start the services"
else
    print_error "Backend rebuild failed"
    exit 1
fi

cd ..