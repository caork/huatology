import { useState, useEffect, useRef } from 'react'
import cytoscape from 'cytoscape'
import coseBilkent from 'cytoscape-cose-bilkent'
import { ApolloClient, InMemoryCache, gql, useQuery, createHttpLink, ApolloProvider } from '@apollo/client'
import { setContext } from '@apollo/client/link/context'
import { useAuth } from './context/AuthContext'
import { AuthProvider } from './context/AuthContext'
import ProtectedRoute from './components/ProtectedRoute'
import './App.css'

cytoscape.use(coseBilkent)

// GraphQL Queries
const GET_OBJECTS = gql`
  query GetObjects($type: String, $limit: Int) {
    objects(type: $type, limit: $limit) {
      id
      type
      properties
      outgoingLinks {
        id
        type
        target {
          id
          type
        }
      }
    }
  }
`

function AppContent() {
  const { user, logout, getAccessToken } = useAuth()
  const [selectedType, setSelectedType] = useState('')
  const [limit, setLimit] = useState(50)
  const [selectedNode, setSelectedNode] = useState(null)
  const cyRef = useRef(null)

  // Create Apollo Client with auth link
  const httpLink = createHttpLink({
    uri: 'http://localhost:8080/graphql',
  })

  const authLink = setContext((_, { headers }) => {
    const token = getAccessToken()
    return {
      headers: {
        ...headers,
        authorization: token ? `Bearer ${token}` : "",
      }
    }
  })

  const client = new ApolloClient({
    link: authLink.concat(httpLink),
    cache: new InMemoryCache()
  })

  const { loading, error, data, refetch } = useQuery(GET_OBJECTS, {
    variables: { type: selectedType || null, limit },
    client
  })

  useEffect(() => {
    if (cyRef.current && data?.objects) {
      renderGraph(data.objects)
    }
  }, [data])

  const fetchData = () => {
    refetch()
  }

  const renderGraph = (objects) => {
    if (!cyRef.current || !objects) return

    const elements = []

    // Add nodes
    objects.forEach(obj => {
      elements.push({
        data: {
          id: obj.id,
          label: obj.type,
          properties: obj.properties
        }
      })
    })

    // Add edges from relationships
    objects.forEach(obj => {
      if (obj.outgoingLinks) {
        obj.outgoingLinks.forEach(link => {
          elements.push({
            data: {
              id: `link-${link.id}`,
              source: obj.id,
              target: link.target.id,
              label: link.type
            }
          })
        })
      }
    })

    const cy = cytoscape({
      container: cyRef.current,
      elements: elements,
      style: [
        {
          selector: 'node',
          style: {
            'background-color': '#666',
            'label': 'data(label)',
            'text-valign': 'center',
            'text-halign': 'center',
            'color': '#fff',
            'font-size': '12px'
          }
        },
        {
          selector: 'edge',
          style: {
            'width': 2,
            'line-color': '#ccc',
            'target-arrow-color': '#ccc',
            'target-arrow-shape': 'triangle',
            'curve-style': 'bezier',
            'label': 'data(label)',
            'font-size': '10px',
            'text-background-color': '#fff',
            'text-background-opacity': 0.8
          }
        }
      ],
      layout: {
        name: 'cose-bilkent',
        animate: true,
        animationDuration: 1000
      }
    })

    // Add click event
    cy.on('tap', 'node', function(evt) {
      const node = evt.target
      const nodeData = node.data()
      const clickedObject = objects.find(obj => obj.id === nodeData.id)
      setSelectedNode(clickedObject)
      console.log('Node clicked:', nodeData, 'Properties:', nodeData.properties)
    })

    // Clear selection when clicking on background
    cy.on('tap', function(evt) {
      if (evt.target === cy) {
        setSelectedNode(null)
      }
    })
  }

  if (error) return <p>Error: {error.message}</p>

  const objects = data?.objects || []

  return (
    <ApolloProvider client={client}>
      <div className="app">
        <header className="app-header">
          <h1>Digital Twin Ontology System</h1>
          <div className="controls">
            <select
              value={selectedType}
              onChange={(e) => setSelectedType(e.target.value)}
            >
              <option value="">All Types</option>
              {[...new Set(objects.map(obj => obj.type))].map(type => (
                <option key={type} value={type}>{type}</option>
              ))}
            </select>
            <input
              type="number"
              value={limit}
              onChange={(e) => setLimit(parseInt(e.target.value))}
              min="1"
              max="1000"
              placeholder="Limit"
            />
            <button onClick={fetchData} disabled={loading}>
              {loading ? 'Loading...' : 'Refresh Data'}
            </button>
          </div>
          <div className="user-info">
            {user && (
              <div className="user-menu">
                <span className="user-greeting">Welcome, {user.username}</span>
                <button
                  onClick={logout}
                  className="logout-button"
                >
                  Logout
                </button>
              </div>
            )}
          </div>
        </header>
      <main className="app-main">
        <div className="graph-container">
          <div ref={cyRef} className="cytoscape-container"></div>
        </div>
        <div className="info-panel">
          {selectedNode ? (
            <div className="node-details">
              <h2>Node Details</h2>
              <div className="detail-item">
                <strong>ID:</strong> {selectedNode.id}
              </div>
              <div className="detail-item">
                <strong>Type:</strong> {selectedNode.type}
              </div>
              <div className="properties">
                <h3>Properties:</h3>
                {selectedNode.properties && Object.entries(selectedNode.properties).map(([key, value]) => (
                  <div key={key} className="property-item">
                    <strong>{key}:</strong> {String(value)}
                  </div>
                ))}
              </div>
              <div className="relationships">
                <h3>Outgoing Links: {selectedNode.outgoingLinks?.length || 0}</h3>
                {selectedNode.outgoingLinks?.map(link => (
                  <div key={link.id} className="link-item">
                    <span className="link-type">{link.type}</span> → {link.target.type} ({link.target.id})
                  </div>
                ))}
              </div>
            </div>
          ) : (
            <>
              <h2>Ontology Information</h2>
              <p>Objects: {objects.length}</p>
              <p>Links: {objects.reduce((acc, obj) => acc + (obj.outgoingLinks?.length || 0), 0)}</p>
              <div className="object-types">
                <h3>Object Types:</h3>
                {[...new Set(objects.map(obj => obj.type))].map(type => (
                  <span key={type} className="type-badge">{type}</span>
                ))}
              </div>
            </>
          )}
        </div>
      </main>
    </div>
    </ApolloProvider>
  )
}

function App() {
  return (
    <AuthProvider>
      <ProtectedRoute>
        <AppContent />
      </ProtectedRoute>
    </AuthProvider>
  )
}

export default App
