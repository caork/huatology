#!/bin/bash

# Digital Twin System Status Script
# This script checks the current status of backend and frontend services

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
BACKEND_PID_FILE="backend.pid"
FRONTEND_PID_FILE="frontend.pid"
BACKEND_PORT=8091
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

# Function to check if process is running
is_process_running() {
    local pid=$1
    if ps -p $pid > /dev/null 2>&1; then
        return 0  # Process is running
    else
        return 1  # Process is not running
    fi
}

# Function to check if port is accessible
check_service_health() {
    local url=$1
    local service_name=$2

    if curl -s --max-time 5 "$url" > /dev/null 2>&1; then
        print_success "$service_name is accessible at $url"
        return 0
    else
        print_error "$service_name is not accessible at $url"
        return 1
    fi
}

# Function to get process info
get_process_info() {
    local pid=$1
    local service_name=$2

    if is_process_running $pid; then
        local cmdline=$(ps -p $pid -o cmd= | head -1)
        local memory=$(ps -p $pid -o rss= | awk '{print $1/1024 "MB"}')
        local cpu=$(ps -p $pid -o pcpu= | awk '{print $1"%"}')

        print_success "$service_name is running (PID: $pid)"
        echo -e "  ${BLUE}Command:${NC} $cmdline"
        echo -e "  ${BLUE}Memory:${NC} $memory"
        echo -e "  ${BLUE}CPU:${NC} $cpu"
    else
        print_error "$service_name process (PID: $pid) is not running"
    fi
}

# Main status check
echo ""
print_status "=== Digital Twin System Status ==="
echo ""

# Check backend
print_status "Backend Service (Port: $BACKEND_PORT):"
if [ -f "$BACKEND_PID_FILE" ]; then
    local backend_pid=$(cat "$BACKEND_PID_FILE")
    get_process_info $backend_pid "Backend"
    check_service_health "http://localhost:$BACKEND_PORT/actuator/health" "Backend health check"
else
    print_warning "Backend PID file not found"

    # Check if any Spring Boot processes are running
    local spring_pids=$(pgrep -f "spring-boot:run" || true)
    if [ -n "$spring_pids" ]; then
        print_warning "Found running Spring Boot processes: $spring_pids"
        for pid in $spring_pids; do
            get_process_info $pid "Spring Boot"
        done
    else
        print_error "No backend processes found"
    fi
fi

echo ""

# Check frontend
print_status "Frontend Service (Port: $FRONTEND_PORT):"
if [ -f "$FRONTEND_PID_FILE" ]; then
    local frontend_pid=$(cat "$FRONTEND_PID_FILE")
    get_process_info $frontend_pid "Frontend"
    check_service_health "http://localhost:$FRONTEND_PORT" "Frontend"
else
    print_warning "Frontend PID file not found"

    # Check if any Vite processes are running
    local vite_pids=$(pgrep -f "vite" || true)
    if [ -n "$vite_pids" ]; then
        print_warning "Found running Vite processes: $vite_pids"
        for pid in $vite_pids; do
            get_process_info $pid "Vite"
        done
    else
        print_error "No frontend processes found"
    fi
fi

echo ""

# Check log files
print_status "Log Files:"
if [ -f "backend.log" ]; then
    local backend_log_size=$(du -h "backend.log" | cut -f1)
    print_success "Backend log exists ($backend_log_size)"
else
    print_warning "Backend log not found"
fi

if [ -f "frontend.log" ]; then
    local frontend_log_size=$(du -h "frontend.log" | cut -f1)
    print_success "Frontend log exists ($frontend_log_size)"
else
    print_warning "Frontend log not found"
fi

echo ""

# Summary
print_status "Quick Actions:"
echo -e "  ${GREEN}Start services:${NC} ./start.sh"
echo -e "  ${RED}Stop services:${NC} ./stop.sh"
echo -e "  ${BLUE}Check status:${NC} ./status.sh"

if [ -f "$BACKEND_PID_FILE" ] && [ -f "$FRONTEND_PID_FILE" ]; then
    local backend_pid=$(cat "$BACKEND_PID_FILE")
    local frontend_pid=$(cat "$FRONTEND_PID_FILE")

    if is_process_running $backend_pid && is_process_running $frontend_pid; then
        echo ""
        print_success "🎉 All services are running!"
        echo -e "  ${BLUE}Frontend:${NC} http://localhost:$FRONTEND_PORT"
        echo -e "  ${BLUE}Backend:${NC} http://localhost:$BACKEND_PORT"
    fi
fi