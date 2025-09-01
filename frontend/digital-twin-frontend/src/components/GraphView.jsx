import React, { useState, useEffect, useCallback } from 'react';
import {
  ReactFlow,
  Controls,
  MiniMap,
  Background,
  useNodesState,
  useEdgesState,
  addEdge,
} from '@xyflow/react';
import '@xyflow/react/dist/style.css';
import {
  Box,
  Paper,
  Typography,
  Chip,
  IconButton,
  Tooltip,
  useTheme,
} from '@mui/material';
import {
  Fullscreen as FullscreenIcon,
  FullscreenExit as FullscreenExitIcon,
} from '@mui/icons-material';
import { motion } from 'framer-motion';

const GraphView = ({ objects, onNodeClick, selectedNode }) => {
  const theme = useTheme();
  const [isFullscreen, setIsFullscreen] = useState(false);
  const [nodes, setNodes, onNodesChange] = useNodesState([]);
  const [edges, setEdges, onEdgesChange] = useEdgesState([]);

  useEffect(() => {
    if (!objects || objects.length === 0) return;

    const initialNodes = objects.map((obj, index) => ({
      id: obj.id,
      type: 'default',
      position: {
        x: Math.random() * 800,
        y: Math.random() * 600,
      },
      data: {
        label: obj.type,
        properties: obj.properties,
        type: obj.type,
        object: obj,
      },
      style: {
        backgroundColor: getNodeColor(obj.type),
        borderColor: theme.palette.primary.main,
        borderWidth: 2,
        color: theme.palette.getContrastText(getNodeColor(obj.type)),
        fontSize: '12px',
        fontWeight: 'bold',
        width: 60,
        height: 60,
        borderRadius: '8px',
        boxShadow: '0 2px 8px rgba(0,0,0,0.3)',
      },
    }));

    const initialEdges = [];
    objects.forEach((obj) => {
      if (obj.outgoingLinks) {
        obj.outgoingLinks.forEach((link) => {
          initialEdges.push({
            id: `link-${link.id}`,
            source: obj.id,
            target: link.target.id,
            label: link.type,
            type: 'default',
            style: {
              stroke: theme.palette.mode === 'dark' ? '#ffffff' : '#666666',
              strokeWidth: 3,
            },
            labelStyle: {
              fontSize: '10px',
              fill: theme.palette.text.primary,
            },
          });
        });
      }
    });

    setNodes(initialNodes);
    setEdges(initialEdges);
  }, [objects, theme.palette.mode]);

  const onNodeClickHandler = useCallback((event, node) => {
    const clickedObject = objects.find((obj) => obj.id === node.id);
    onNodeClick(clickedObject);
  }, [objects, onNodeClick]);

  const onPaneClick = useCallback(() => {
    onNodeClick(null);
  }, [onNodeClick]);

  const getNodeColor = (type) => {
    const colors = {
      Person: '#2196f3',
      Organization: '#4caf50',
      Product: '#ff9800',
      Project: '#9c27b0',
      Location: '#f44336',
      default: '#607d8b',
    };
    return colors[type] || colors.default;
  };

  const toggleFullscreen = () => {
    setIsFullscreen(!isFullscreen);
  };

  return (
    <motion.div
      initial={{ opacity: 0, scale: 0.95 }}
      animate={{ opacity: 1, scale: 1 }}
      transition={{ duration: 0.5 }}
    >
      <Paper
        elevation={3}
        sx={{
          height: isFullscreen ? '100vh' : 'calc(100vh - 140px)',
          position: isFullscreen ? 'fixed' : 'relative',
          top: isFullscreen ? 0 : 'auto',
          left: isFullscreen ? 0 : 'auto',
          zIndex: isFullscreen ? 1300 : 'auto',
          borderRadius: isFullscreen ? 0 : 2,
          overflow: 'hidden',
        }}
      >
        {/* Graph Controls */}
        <Box
          sx={{
            position: 'absolute',
            top: 16,
            right: 16,
            zIndex: 10,
            display: 'flex',
            gap: 1,
          }}
        >
          <Tooltip title={isFullscreen ? 'Exit Fullscreen' : 'Fullscreen'}>
            <IconButton
              onClick={toggleFullscreen}
              sx={{
                backgroundColor: 'rgba(255,255,255,0.9)',
                '&:hover': { backgroundColor: 'rgba(255,255,255,1)' },
              }}
            >
              {isFullscreen ? <FullscreenExitIcon /> : <FullscreenIcon />}
            </IconButton>
          </Tooltip>
        </Box>

        {/* Graph Container */}
        <Box
          sx={{
            width: '100%',
            height: '100%',
            background: `radial-gradient(circle at center, ${theme.palette.background.default} 0%, ${theme.palette.background.paper} 100%)`,
          }}
        >
          <ReactFlow
            nodes={nodes}
            edges={edges}
            onNodesChange={onNodesChange}
            onEdgesChange={onEdgesChange}
            onNodeClick={onNodeClickHandler}
            onPaneClick={onPaneClick}
            fitView
            style={{ background: 'transparent' }}
          >
            <Controls />
            <MiniMap />
            <Background variant="dots" gap={12} size={1} />
          </ReactFlow>
        </Box>

        {/* Legend */}
        <Box
          sx={{
            position: 'absolute',
            bottom: 16,
            left: 16,
            backgroundColor: 'rgba(255,255,255,0.9)',
            borderRadius: 2,
            p: 2,
            maxWidth: 300,
          }}
        >
          <Typography variant="h6" gutterBottom>
            Node Types
          </Typography>
          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
            {['Person', 'Organization', 'Product', 'Project', 'Location'].map((type) => (
              <Chip
                key={type}
                label={type}
                size="small"
                sx={{
                  backgroundColor: getNodeColor(type),
                  color: 'white',
                  '&:hover': { opacity: 0.8 },
                }}
              />
            ))}
          </Box>
        </Box>
      </Paper>
    </motion.div>
  );
};

export default GraphView;