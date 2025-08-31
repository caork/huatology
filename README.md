# Digital Twin Ontology System

A Java-based implementation of the Palantir Ontology concept, providing a scalable digital twin system with graph database backend, GraphQL API, and interactive visualization.

## Architecture Overview

This system implements the core principles of the Palantir Ontology:

### Backend Components
- **Graph Database**: Neo4j with Spring Data Neo4j
- **API Layer**: GraphQL with Spring GraphQL
- **Data Models**: Object Types, Properties, Links (Relationships), Actions
- **Ingestion Pipeline**: Support for structured and unstructured data ingestion
- **Services**: Object, Link, Action, and Ingestion services

### Frontend Components
- **React Application**: Interactive ontology visualization with authentication
- **Authentication System**: JWT-based login/register with protected routes
- **GraphQL Client**: Apollo Client with automatic auth headers
- **Visualization**: Cytoscape.js for graph rendering
- **Interactive Features**: Node details, filtering, and exploration
- **State Management**: React Context for authentication state

## Key Features

### Semantic Layer
- **Object Types**: Schema definitions for real-world entities
- **Properties**: Flexible attribute system for objects
- **Links**: Relationships between objects (implemented as Neo4j relationships)
- **Dynamic Schema**: Support for extensible object types and properties

### Kinetic Layer
- **Actions**: Auditable operations on objects
- **Decision Capture**: Historical logging of changes
- **Workflow Support**: Structured data modifications

### Data Ingestion
- **Structured Data**: Direct ingestion from APIs, databases
- **Unstructured Data**: LLM-powered entity extraction (placeholder)
- **Batch Processing**: Efficient bulk data loading
- **Relationship Discovery**: Automatic relationship creation

### API & Integration
- **GraphQL API**: Flexible querying and mutations
- **REST Endpoints**: Traditional CRUD operations
- **Real-time Updates**: Subscription support via GraphQL

## Technology Stack

### Backend
- Java 17
- Spring Boot 3.2.0
- Spring Data Neo4j
- Spring GraphQL
- Neo4j Graph Database

### Frontend
- React 19
- Apollo Client (with auth integration)
- React Router DOM
- Axios (for auth API calls)
- JWT Decode
- Cytoscape.js
- Vite

## 🚀 Quick Start (Automated)

The easiest way to run the system is using the provided scripts:

### Prerequisites
- Java 17 or higher
- Node.js 18 or higher
- Neo4j 5.x (running on localhost:7687)

### One-Command Startup
```bash
# Start both backend and frontend
./start.sh

# Check system status
./status.sh

# Stop all services
./stop.sh
```

### Manual Setup (Alternative)

#### Backend Setup
1. Navigate to the backend directory:
   ```bash
   cd backend
   ```

2. Configure Neo4j connection in `src/main/resources/application.properties`:
   ```properties
   spring.neo4j.uri=bolt://localhost:7687
   spring.neo4j.authentication.username=neo4j
   spring.neo4j.authentication.password=your_password
   ```

3. Build and run the backend:
   ```bash
   ./mvnw spring-boot:run
   ```

#### Frontend Setup
1. Navigate to the frontend directory:
   ```bash
   cd frontend/digital-twin-frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the development server:
   ```bash
   npm run dev
   ```

### Accessing the Application
- **Frontend**: http://localhost:5173 (requires authentication)
- **GraphQL Playground**: http://localhost:8080/graphiql
- **Backend API**: http://localhost:8080
- **Backend Health**: http://localhost:8080/actuator/health

### First Time Setup
1. Run `./start.sh` to start all services
2. Open http://localhost:5173 in your browser
3. Click "Get Started" to register your first account
4. Login with your credentials
5. Access the full Digital Twin interface

### System Management Scripts

The project includes automated scripts for easy system management:

#### `start.sh`
- Starts both backend and frontend services
- Automatically installs frontend dependencies if needed
- Waits for services to be ready before reporting success
- Creates PID files for process management
- Provides colored output and progress feedback

#### `stop.sh`
- Gracefully stops all running services
- Handles process cleanup and force-kill if needed
- Cleans up PID files and optionally log files
- Works even if services were started manually

#### `status.sh`
- Shows current status of all services
- Displays process information (PID, memory, CPU)
- Checks service health endpoints
- Provides quick action commands

#### Usage Examples
```bash
# Start the system
./start.sh

# Check status
./status.sh

# Stop the system
./stop.sh

# View logs
tail -f backend.log
tail -f frontend.log
```

## API Usage

### GraphQL Examples

#### Query Objects
```graphql
query {
  objects(type: "Person", limit: 10) {
    id
    type
    properties
    outgoingLinks {
      type
      target {
        id
        type
      }
    }
  }
}
```

#### Create Object
```graphql
mutation {
  createObject(input: {
    type: "Person"
    properties: {
      name: "John Doe"
      age: 30
    }
  }) {
    id
    type
    properties
  }
}
```

### REST API Endpoints

#### Data Ingestion
```bash
# Ingest structured data
POST /api/ingestion/structured?sourceType=Product
Content-Type: application/json
[
  {"name": "Laptop", "price": 999.99},
  {"name": "Mouse", "price": 29.99}
]

# Ingest unstructured text
POST /api/ingestion/unstructured?sourceType=Document
Content-Type: text/plain
"This document contains information about John Doe who works at ACME Corp..."
```

## Data Model

### Object Entity
- **id**: Unique identifier
- **type**: Object type (e.g., "Person", "Product", "Organization")
- **properties**: Flexible key-value properties
- **outgoingLinks**: Relationships to other objects
- **incomingLinks**: Incoming relationships

### Link (Relationship)
- **id**: Unique identifier
- **type**: Relationship type (e.g., "WORKS_FOR", "OWNS")
- **source**: Source object
- **target**: Target object
- **properties**: Relationship properties

### Action
- **id**: Unique identifier
- **type**: Action type (e.g., "CREATE", "UPDATE", "DELETE")
- **objectId**: Target object ID
- **changes**: Changes made
- **timestamp**: When the action occurred
- **user**: User who performed the action

## Development Roadmap

### Completed Features
- ✅ Graph database integration (Neo4j)
- ✅ GraphQL API implementation
- ✅ JWT-based authentication and authorization
- ✅ User registration and login system
- ✅ Protected routes and secure GraphQL endpoints
- ✅ Basic CRUD operations
- ✅ Interactive visualization
- ✅ Data ingestion pipeline
- ✅ Node details and filtering
- ✅ Relationship modeling
- ✅ Automated startup/stop scripts

### Future Enhancements
- 🔄 Action logging and audit trail (partially implemented)
- 🔄 Real LLM integration for entity extraction
- 🔄 Advanced visualization features
- 🔄 Performance optimizations
- 🔄 Multi-tenancy support
- 🔄 Role-based access control enhancements

## Architecture Principles

This implementation follows the Palantir Ontology principles:

1. **Semantic Foundation**: Clear separation between data structure (semantic) and operations (kinetic)
2. **Graph-Native**: Relationships as first-class citizens
3. **Flexible Schema**: Dynamic object types and properties
4. **Audit Trail**: Complete history of changes and decisions
5. **Scalable Architecture**: Microservices-ready design
6. **Developer-Friendly**: Modern APIs and tooling

## Contributing

This is a comprehensive implementation of enterprise-scale ontology management. Key areas for contribution:

- LLM integration for advanced entity extraction
- Authentication and authorization systems
- Performance optimization for large graphs
- Advanced visualization features
- Real-time collaboration features

## License

This project implements concepts from the Palantir Ontology system for educational and research purposes.