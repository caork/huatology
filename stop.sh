#!/bin/bash

# Digital Twin System Stop Script
# This script stops both the backend and frontend services

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
BACKEND_PID_FILE="backend.pid"
FRONTEND_PID_FILE="frontend.pid"
BACKEND_LOG_FILE="backend.log"
FRONTEND_LOG_FILE="frontend.log"

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

# Function to check if process is running
is_process_running() {
    local pid=$1
    if ps -p $pid > /dev/null 2>&1; then
        return 0  # Process is running
    else
        return 1  # Process is not running
    fi
}

# Function to stop process gracefully
stop_process() {
    local pid=$1
    local service_name=$2
    local max_attempts=10
    local attempt=1

    if ! is_process_running $pid; then
        print_warning "$service_name process (PID: $pid) is not running"
        return 0
    fi

    print_status "Stopping $service_name (PID: $pid)..."

    # Send SIGTERM first (graceful shutdown)
    kill -TERM $pid 2>/dev/null || true

    # Wait for process to stop gracefully
    while [ $attempt -le $max_attempts ]; do
        if ! is_process_running $pid; then
            print_success "$service_name stopped gracefully"
            return 0
        fi

        echo -n "."
        sleep 1
        ((attempt++))
    done

    # If still running, force kill
    print_warning "$service_name didn't stop gracefully, force killing..."
    kill -KILL $pid 2>/dev/null || true

    # Final check
    if ! is_process_running $pid; then
        print_success "$service_name force killed"
        return 0
    else
        print_error "Failed to stop $service_name"
        return 1
    fi
}

# Function to stop backend
stop_backend() {
    if [ ! -f "$BACKEND_PID_FILE" ]; then
        print_warning "Backend PID file not found. Checking for running Spring Boot processes..."

        # Try to find and kill any Spring Boot processes
        local spring_pids=$(pgrep -f "spring-boot:run" || true)
        if [ -n "$spring_pids" ]; then
            print_status "Found Spring Boot processes: $spring_pids"
            for pid in $spring_pids; do
                stop_process $pid "Spring Boot (PID: $pid)"
            done
        else
            print_warning "No Spring Boot processes found"
        fi
        return 0
    fi

    local backend_pid=$(cat "$BACKEND_PID_FILE")
    if stop_process $backend_pid "Backend"; then
        rm -f "$BACKEND_PID_FILE"
        return 0
    else
        return 1
    fi
}

# Function to stop frontend
stop_frontend() {
    if [ ! -f "$FRONTEND_PID_FILE" ]; then
        print_warning "Frontend PID file not found. Checking for running Vite processes..."

        # Try to find and kill any Vite processes
        local vite_pids=$(pgrep -f "vite" || true)
        if [ -n "$vite_pids" ]; then
            print_status "Found Vite processes: $vite_pids"
            for pid in $vite_pids; do
                stop_process $pid "Vite (PID: $pid)"
            done
        else
            print_warning "No Vite processes found"
        fi
        return 0
    fi

    local frontend_pid=$(cat "$FRONTEND_PID_FILE")
    if stop_process $frontend_pid "Frontend"; then
        rm -f "$FRONTEND_PID_FILE"
        return 0
    else
        return 1
    fi
}

# Function to cleanup files
cleanup_files() {
    print_status "Cleaning up files..."

    # Remove PID files
    rm -f "$BACKEND_PID_FILE" "$FRONTEND_PID_FILE"

    # Ask about log files
    if [ -f "$BACKEND_LOG_FILE" ] || [ -f "$FRONTEND_LOG_FILE" ]; then
        echo ""
        read -p "Remove log files? (y/N): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            rm -f "$BACKEND_LOG_FILE" "$FRONTEND_LOG_FILE"
            print_success "Log files removed"
        else
            print_status "Log files kept"
        fi
    fi
}

# Function to show status
show_status() {
    echo ""
    print_status "=== System Status ==="

    # Check backend
    if [ -f "$BACKEND_PID_FILE" ]; then
        local backend_pid=$(cat "$BACKEND_PID_FILE")
        if is_process_running $backend_pid; then
            print_status "Backend: Running (PID: $backend_pid)"
        else
            print_warning "Backend: Stopped (PID file exists but process not running)"
        fi
    else
        print_status "Backend: Stopped"
    fi

    # Check frontend
    if [ -f "$FRONTEND_PID_FILE" ]; then
        local frontend_pid=$(cat "$FRONTEND_PID_FILE")
        if is_process_running $frontend_pid; then
            print_status "Frontend: Running (PID: $frontend_pid)"
        else
            print_warning "Frontend: Stopped (PID file exists but process not running)"
        fi
    else
        print_status "Frontend: Stopped"
    fi
}

# Main execution
print_status "=== Digital Twin System Shutdown ==="

# Show current status
show_status

echo ""
print_status "Stopping services..."

# Stop services
backend_stopped=true
frontend_stopped=true

if ! stop_backend; then
    backend_stopped=false
fi

if ! stop_frontend; then
    frontend_stopped=false
fi

# Cleanup
cleanup_files

# Final status
echo ""
if $backend_stopped && $frontend_stopped; then
    print_success "=== All services stopped successfully! ==="
elif $backend_stopped || $frontend_stopped; then
    print_warning "=== Some services stopped, others may still be running ==="
else
    print_error "=== Failed to stop services ==="
    exit 1
fi

# Show final status
show_status