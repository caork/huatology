import { useState, useEffect, useRef, useMemo } from 'react'
import { ApolloClient, InMemoryCache, gql, useQuery, useMutation, createHttpLink, ApolloProvider } from '@apollo/client'
import { setContext } from '@apollo/client/link/context'
import { Box, CssBaseline, useTheme, useMediaQuery, Button, Typography } from '@mui/material'
import { Add as AddIcon, Link as LinkIcon } from '@mui/icons-material'
import { ThemeProvider } from './context/ThemeContext'
import { useAuth } from './context/AuthContext'
import { AuthProvider } from './context/AuthContext'
import ProtectedRoute from './components/ProtectedRoute'
import Header from './components/Header'
import Sidebar from './components/Sidebar'
import GraphView from './components/GraphView'
import Dashboard from './components/Dashboard'
import CreateObjectModal from './components/CreateObjectModal'
import CreateLinkModal from './components/CreateLinkModal'
import { Toaster } from 'react-hot-toast'
import './App.css'

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
  const theme = useTheme()
  const isMobile = useMediaQuery(theme.breakpoints.down('md'))

  // Navigation state
  const [activeView, setActiveView] = useState('dashboard')
  const [mobileOpen, setMobileOpen] = useState(false)

  // Data state
  const [selectedType, setSelectedType] = useState('')
  const [limit, setLimit] = useState(50)
  const [selectedNode, setSelectedNode] = useState(null)

  // Modal states
  const [showCreateObjectModal, setShowCreateObjectModal] = useState(false)
  const [showCreateLinkModal, setShowCreateLinkModal] = useState(false)

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
      setShowCreateObjectModal(false)
    },
    onError: (error) => {
      console.error('Error creating object:', error)
      // TODO: Add toast notification
    }
  })

  const [createLink, { loading: creatingLink }] = useMutation(CREATE_LINK, {
    client,
    onCompleted: () => {
      refetch()
      setShowCreateLinkModal(false)
    },
    onError: (error) => {
      console.error('Error creating link:', error)
      // TODO: Add toast notification
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

  const handleCreateObject = (objectData) => {
    createObject({
      variables: {
        input: objectData
      }
    })
  }

  const handleCreateLink = (linkData) => {
    createLink({
      variables: {
        input: linkData
      }
    })
  }

  const handleRefresh = () => {
    refetch()
  }


  if (error) return <p>Error: {error.message}</p>

  const objects = data?.objects || []
  const totalLinks = objects.reduce((acc, obj) => acc + (obj.outgoingLinks?.length || 0), 0)

  const renderContent = () => {
    switch (activeView) {
      case 'dashboard':
        return <Dashboard objects={objects} links={totalLinks} />
      case 'graph':
        return (
          <GraphView
            objects={objects}
            onNodeClick={setSelectedNode}
            selectedNode={selectedNode}
          />
        )
      case 'search':
        return (
          <Box sx={{ p: 3 }}>
            <Typography variant="h4">Search & Filter</Typography>
            <Typography variant="body1" sx={{ mt: 2 }}>
              Search functionality will be implemented here.
            </Typography>
          </Box>
        )
      case 'create':
        return (
          <Box sx={{ p: 3 }}>
            <Typography variant="h4">Create</Typography>
            <Box sx={{ mt: 3, display: 'flex', gap: 2 }}>
              <Button
                variant="contained"
                onClick={() => setShowCreateObjectModal(true)}
                startIcon={<AddIcon />}
              >
                Create Object
              </Button>
              <Button
                variant="contained"
                onClick={() => setShowCreateLinkModal(true)}
                startIcon={<LinkIcon />}
              >
                Create Link
              </Button>
            </Box>
          </Box>
        )
      default:
        return <Dashboard objects={objects} links={totalLinks} />
    }
  }

  return (
    <ApolloProvider client={client}>
      <Box sx={{ display: 'flex', minHeight: '100vh' }}>
        <Header
          activeView={activeView}
          setActiveView={setActiveView}
          mobileOpen={mobileOpen}
          setMobileOpen={setMobileOpen}
          onRefresh={handleRefresh}
          isLoading={loading}
          stats={{ objects: objects.length, links: totalLinks }}
        />

        <Box
          component="main"
          sx={{
            flexGrow: 1,
            ml: isMobile ? 0 : '280px',
            mt: '64px',
            overflow: 'hidden',
          }}
        >
          {renderContent()}
        </Box>

        {/* Modals */}
        <CreateObjectModal
          open={showCreateObjectModal}
          onClose={() => setShowCreateObjectModal(false)}
          onSubmit={handleCreateObject}
          loading={creating}
        />

        <CreateLinkModal
          open={showCreateLinkModal}
          onClose={() => setShowCreateLinkModal(false)}
          onSubmit={handleCreateLink}
          loading={creatingLink}
          objects={objects}
        />

        <Toaster position="top-right" />
      </Box>
    </ApolloProvider>
  )
}

function App() {
  return (
    <ThemeProvider>
      <AuthProvider>
        <ProtectedRoute>
          <AppContent />
        </ProtectedRoute>
      </AuthProvider>
    </ThemeProvider>
  )
}

export default App
