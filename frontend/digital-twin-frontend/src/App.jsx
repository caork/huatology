import { useState, useEffect, useRef, useMemo } from 'react'
import cytoscape from 'cytoscape'
import coseBilkent from 'cytoscape-cose-bilkent'
import { ApolloClient, InMemoryCache, gql, useQuery, useMutation, createHttpLink, ApolloProvider } from '@apollo/client'
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

const CREATE_OBJECT = gql`
  mutation CreateObject($input: ObjectInput!) {
    createObject(input: $input) {
      id
      type
      properties
    }
  }
`

const CREATE_LINK = gql`
  mutation CreateLink($input: LinkInput!) {
    createLink(input: $input) {
      id
      type
      source {
        id
        type
      }
      target {
        id
        type
      }
      properties
    }
  }
`

function AppContent() {
  const { user, logout, getAccessToken } = useAuth()
  const [selectedType, setSelectedType] = useState('')
  const [limit, setLimit] = useState(50)
  const [selectedNode, setSelectedNode] = useState(null)
  const [showCreateForm, setShowCreateForm] = useState(false)
  const [newObjectType, setNewObjectType] = useState('')
  const [newObjectProperties, setNewObjectProperties] = useState([{ key: '', value: '' }])
  const [showCreateLinkForm, setShowCreateLinkForm] = useState(false)
  const [newLinkType, setNewLinkType] = useState('')
  const [selectedSourceId, setSelectedSourceId] = useState('')
  const [selectedTargetId, setSelectedTargetId] = useState('')
  const [newLinkProperties, setNewLinkProperties] = useState([{ key: '', value: '' }])
  const cyRef = useRef(null)

  // Create Apollo Client with auth link
  const client = useMemo(() => {
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

    return new ApolloClient({
      link: authLink.concat(httpLink),
      cache: new InMemoryCache()
    })
  }, [getAccessToken()])

  const { loading, error, data, refetch } = useQuery(GET_OBJECTS, {
    variables: { type: selectedType || null, limit },
    client
  })

  const [createObject, { loading: creating }] = useMutation(CREATE_OBJECT, {
    client,
    onCompleted: () => {
      refetch()
      setShowCreateForm(false)
      setNewObjectType('')
      setNewObjectProperties([{ key: '', value: '' }])
    },
    onError: (error) => {
      console.error('Error creating object:', error)
      alert('Error creating object: ' + error.message)
    }
  })

  const [createLink, { loading: creatingLink }] = useMutation(CREATE_LINK, {
    client,
    onCompleted: () => {
      refetch()
      setShowCreateLinkForm(false)
      setNewLinkType('')
      setSelectedSourceId('')
      setSelectedTargetId('')
      setNewLinkProperties([{ key: '', value: '' }])
    },
    onError: (error) => {
      console.error('Error creating link:', error)
      alert('Error creating link: ' + error.message)
    }
  })

  useEffect(() => {
    if (cyRef.current && data?.objects) {
      renderGraph(data.objects)
    }
  }, [data])

  const fetchData = () => {
    refetch()
  }

  const handleCreateObject = () => {
    const properties = {}
    newObjectProperties.forEach(prop => {
      if (prop.key && prop.value) {
        properties[prop.key] = prop.value
      }
    })

    createObject({
      variables: {
        input: {
          type: newObjectType,
          properties
        }
      }
    })
  }

  const handleCreateLink = () => {
    const properties = {}
    newLinkProperties.forEach(prop => {
      if (prop.key && prop.value) {
        properties[prop.key] = prop.value
      }
    })

    createLink({
      variables: {
        input: {
          type: newLinkType,
          sourceId: selectedSourceId,
          targetId: selectedTargetId,
          properties
        }
      }
    })
  }

  const addLinkProperty = () => {
    setNewLinkProperties([...newLinkProperties, { key: '', value: '' }])
  }

  const updateLinkProperty = (index, field, value) => {
    const updated = [...newLinkProperties]
    updated[index][field] = value
    setNewLinkProperties(updated)
  }

  const removeLinkProperty = (index) => {
    if (newLinkProperties.length > 1) {
      setNewLinkProperties(newLinkProperties.filter((_, i) => i !== index))
    }
  }

  const addProperty = () => {
    setNewObjectProperties([...newObjectProperties, { key: '', value: '' }])
  }

  const updateProperty = (index, field, value) => {
    const updated = [...newObjectProperties]
    updated[index][field] = value
    setNewObjectProperties(updated)
  }

  const removeProperty = (index) => {
    if (newObjectProperties.length > 1) {
      setNewObjectProperties(newObjectProperties.filter((_, i) => i !== index))
    }
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
            <button onClick={() => setShowCreateForm(!showCreateForm)}>
              {showCreateForm ? 'Cancel' : 'Create Object'}
            </button>
            <button onClick={() => setShowCreateLinkForm(!showCreateLinkForm)}>
              {showCreateLinkForm ? 'Cancel Link' : 'Create Link'}
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
        {showCreateForm && (
          <div className="create-form-panel">
            <h3>Create New Object</h3>
            <div className="form-group">
              <label>Type:</label>
              <input
                type="text"
                value={newObjectType}
                onChange={(e) => setNewObjectType(e.target.value)}
                placeholder="e.g., Person, Product, Organization"
              />
            </div>
            <div className="properties-section">
              <h4>Properties:</h4>
              {newObjectProperties.map((prop, index) => (
                <div key={index} className="property-row">
                  <input
                    type="text"
                    placeholder="Key"
                    value={prop.key}
                    onChange={(e) => updateProperty(index, 'key', e.target.value)}
                  />
                  <input
                    type="text"
                    placeholder="Value"
                    value={prop.value}
                    onChange={(e) => updateProperty(index, 'value', e.target.value)}
                  />
                  <button
                    type="button"
                    onClick={() => removeProperty(index)}
                    disabled={newObjectProperties.length === 1}
                  >
                    Remove
                  </button>
                </div>
              ))}
              <button type="button" onClick={addProperty}>Add Property</button>
            </div>
            <div className="form-actions">
              <button
                onClick={handleCreateObject}
                disabled={creating || !newObjectType.trim()}
              >
                {creating ? 'Creating...' : 'Create Object'}
              </button>
              <button onClick={() => setShowCreateForm(false)}>Cancel</button>
            </div>
          </div>
        )}
        {showCreateLinkForm && (
          <div className="create-form-panel">
            <h3>Create New Link</h3>
            <div className="form-group">
              <label>Link Type:</label>
              <input
                type="text"
                value={newLinkType}
                onChange={(e) => setNewLinkType(e.target.value)}
                placeholder="e.g., WORKS_FOR, OWNS, CONTAINS"
              />
            </div>
            <div className="form-group">
              <label>Source Object:</label>
              <select
                value={selectedSourceId}
                onChange={(e) => setSelectedSourceId(e.target.value)}
              >
                <option value="">Select source object...</option>
                {objects.map(obj => (
                  <option key={obj.id} value={obj.id}>
                    {obj.type} ({obj.id})
                  </option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <label>Target Object:</label>
              <select
                value={selectedTargetId}
                onChange={(e) => setSelectedTargetId(e.target.value)}
              >
                <option value="">Select target object...</option>
                {objects.map(obj => (
                  <option key={obj.id} value={obj.id}>
                    {obj.type} ({obj.id})
                  </option>
                ))}
              </select>
            </div>
            <div className="properties-section">
              <h4>Link Properties:</h4>
              {newLinkProperties.map((prop, index) => (
                <div key={index} className="property-row">
                  <input
                    type="text"
                    placeholder="Key"
                    value={prop.key}
                    onChange={(e) => updateLinkProperty(index, 'key', e.target.value)}
                  />
                  <input
                    type="text"
                    placeholder="Value"
                    value={prop.value}
                    onChange={(e) => updateLinkProperty(index, 'value', e.target.value)}
                  />
                  <button
                    type="button"
                    onClick={() => removeLinkProperty(index)}
                    disabled={newLinkProperties.length === 1}
                  >
                    Remove
                  </button>
                </div>
              ))}
              <button type="button" onClick={addLinkProperty}>Add Property</button>
            </div>
            <div className="form-actions">
              <button
                onClick={handleCreateLink}
                disabled={creatingLink || !newLinkType.trim() || !selectedSourceId || !selectedTargetId}
              >
                {creatingLink ? 'Creating...' : 'Create Link'}
              </button>
              <button onClick={() => setShowCreateLinkForm(false)}>Cancel</button>
            </div>
          </div>
        )}
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
