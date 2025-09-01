#!/bin/bash

# Digital Twin System Startup Script
# This script starts both the backend and frontend services

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
BACKEND_PORT=8080
FRONTEND_PORT=5173

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

# Function to check if port is in use
check_port() {
    local port=$1
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null ; then
        return 0  # Port is in use
    else
        return 1  # Port is free
    fi
}

# Function to wait for service to be ready
wait_for_service() {
    local url=$1
    local service_name=$2
    local max_attempts=30
    local attempt=1

    print_status "Waiting for $service_name to be ready..."

    while [ $attempt -le $max_attempts ]; do
        if curl -s --max-time 5 "$url" > /dev/null 2>&1; then
            print_success "$service_name is ready!"
            return 0
        fi

        echo -n "."
        sleep 2
        ((attempt++))
    done

    print_error "$service_name failed to start within expected time"
    return 1
}

# Function to start backend
start_backend() {
    print_status "Starting backend service..."

    # Check if backend port is already in use
    if check_port $BACKEND_PORT; then
        print_warning "Port $BACKEND_PORT is already in use. Checking if it's our backend..."
        if pgrep -f "spring-boot:run" > /dev/null; then
            print_warning "Backend appears to be already running"
            return 0
        else
            print_error "Port $BACKEND_PORT is in use by another process"
            return 1
        fi
    fi

    # Navigate to backend directory
    if [ ! -d "$BACKEND_DIR" ]; then
        print_error "Backend directory '$BACKEND_DIR' not found"
        return 1
    fi

    cd "$BACKEND_DIR"

    # Start backend in background
    print_status "Building and starting Spring Boot application..."
    nohup ./mvnw spring-boot:run > ../backend.log 2>&1 &
    BACKEND_PID=$!

    # Save PID for later cleanup
    echo $BACKEND_PID > ../backend.pid

    cd ..

    # Wait for backend to be ready
    if wait_for_service "http://localhost:$BACKEND_PORT/actuator/health" "Backend"; then
        print_success "Backend started successfully (PID: $BACKEND_PID)"
        return 0
    else
        print_error "Backend failed to start"
        return 1
    fi
}

# Function to start frontend
start_frontend() {
    print_status "Starting frontend service..."

    # Check if frontend port is already in use
    if check_port $FRONTEND_PORT; then
        print_warning "Port $FRONTEND_PORT is already in use. Checking if it's our frontend..."
        if pgrep -f "vite" > /dev/null; then
            print_warning "Frontend appears to be already running"
            return 0
        else
            print_error "Port $FRONTEND_PORT is in use by another process"
            return 1
        fi
    fi

    # Navigate to frontend directory
    if [ ! -d "$FRONTEND_DIR" ]; then
        print_error "Frontend directory '$FRONTEND_DIR' not found"
        return 1
    fi

    cd "$FRONTEND_DIR"

    # Install dependencies if node_modules doesn't exist
    if [ ! -d "node_modules" ]; then
        print_status "Installing frontend dependencies..."
        npm install
    fi

    # Start frontend in background
    print_status "Starting Vite development server..."
    nohup npm run dev > ../../frontend.log 2>&1 &
    FRONTEND_PID=$!

    # Save PID for later cleanup
    echo $FRONTEND_PID > ../../frontend.pid

    cd ../..

    # Wait for frontend to be ready
    if wait_for_service "http://localhost:$FRONTEND_PORT" "Frontend"; then
        print_success "Frontend started successfully (PID: $FRONTEND_PID)"
        return 0
    else
        print_error "Frontend failed to start"
        return 1
    fi
}

# Main execution
print_status "=== Digital Twin System Startup ==="
print_status "Starting services..."

# Start backend first
if start_backend; then
    # Start frontend after backend is ready
    if start_frontend; then
        print_success "=== All services started successfully! ==="
        echo ""
        print_status "Access your application at:"
        print_status "  Frontend: http://localhost:$FRONTEND_PORT"
        print_status "  Backend:  http://localhost:$BACKEND_PORT"
        echo ""
        print_status "To stop the services, run: ./stop.sh"
        print_status "Log files: backend.log, frontend.log"
        print_status "PID files: backend.pid, frontend.pid"
    else
        print_error "Failed to start frontend"
        exit 1
    fi
else
    print_error "Failed to start backend"
    exit 1
fi