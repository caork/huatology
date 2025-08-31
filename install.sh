#!/bin/bash

# Digital Twin System Installation Script
# This script sets up the development environment for first-time users

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
BACKEND_DIR="backend"
FRONTEND_DIR="frontend/digital-twin-frontend"

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

# Function to check if command exists
check_command() {
    local cmd=$1
    local name=$2
    if command -v "$cmd" >/dev/null 2>&1; then
        print_success "$name is installed: $($cmd --version | head -n1)"
        return 0
    else
        print_error "$name is not installed or not in PATH"
        return 1
    fi
}

# Function to check Java version
check_java() {
    if command -v java >/dev/null 2>&1; then
        local version=$(java -version 2>&1 | head -n1 | cut -d'"' -f2 | cut -d'.' -f1)
        if [ "$version" -ge 17 ]; then
            print_success "Java $version is installed"
            return 0
        else
            print_error "Java $version found, but Java 17+ is required"
            return 1
        fi
    else
        print_error "Java is not installed"
        return 1
    fi
}

# Function to install Maven wrapper
install_maven_wrapper() {
    print_status "Installing Maven wrapper..."
    cd "$BACKEND_DIR"
    mvn wrapper:wrapper
    print_success "Maven wrapper installed"
    cd ..
}

# Function to setup backend
setup_backend() {
    print_status "Setting up backend..."

    if [ ! -d "$BACKEND_DIR" ]; then
        print_error "Backend directory '$BACKEND_DIR' not found"
        return 1
    fi

    cd "$BACKEND_DIR"

    # Check if Maven wrapper exists, if not install it
    if [ ! -f "mvnw" ]; then
        print_warning "Maven wrapper not found, installing..."
        if ! mvn wrapper:wrapper; then
            print_error "Failed to install Maven wrapper"
            cd ..
            return 1
        fi
    fi

    # Make mvnw executable
    chmod +x mvnw

    # Download dependencies
    print_status "Downloading backend dependencies..."
    if ./mvnw dependency:resolve; then
        print_success "Backend dependencies resolved"
    else
        print_error "Failed to resolve backend dependencies"
        cd ..
        return 1
    fi

    cd ..
    return 0
}

# Function to setup frontend
setup_frontend() {
    print_status "Setting up frontend..."

    if [ ! -d "$FRONTEND_DIR" ]; then
        print_error "Frontend directory '$FRONTEND_DIR' not found"
        return 1
    fi

    cd "$FRONTEND_DIR"

    # Install dependencies
    print_status "Installing frontend dependencies..."
    if npm install; then
        print_success "Frontend dependencies installed"
    else
        print_error "Failed to install frontend dependencies"
        cd ../..
        return 1
    fi

    cd ../..
    return 0
}

# Function to make scripts executable
make_scripts_executable() {
    print_status "Making scripts executable..."
    chmod +x start.sh
    chmod +x stop.sh
    chmod +x install.sh
    print_success "Scripts are now executable"
}

# Main execution
print_status "=== Digital Twin System Installation ==="

# Check prerequisites
print_status "Checking prerequisites..."

prerequisites_ok=true

if ! check_java; then
    prerequisites_ok=false
fi

if ! check_command "mvn" "Maven"; then
    prerequisites_ok=false
fi

if ! check_command "node" "Node.js"; then
    prerequisites_ok=false
fi

if ! check_command "npm" "NPM"; then
    prerequisites_ok=false
fi

if ! check_command "curl" "curl"; then
    prerequisites_ok=false
fi

if ! check_command "lsof" "lsof"; then
    prerequisites_ok=false
fi

if [ "$prerequisites_ok" = false ]; then
    print_error "Please install missing prerequisites and run this script again"
    exit 1
fi

print_success "All prerequisites are installed"

# Make scripts executable
make_scripts_executable

# Setup backend
if setup_backend; then
    print_success "Backend setup completed"
else
    print_error "Backend setup failed"
    exit 1
fi

# Setup frontend
if setup_frontend; then
    print_success "Frontend setup completed"
else
    print_error "Frontend setup failed"
    exit 1
fi

print_success "=== Installation completed successfully! ==="
echo ""
print_status "You can now run the application with:"
print_status "  ./start.sh"
echo ""
print_status "To stop the services:"
print_status "  ./stop.sh"