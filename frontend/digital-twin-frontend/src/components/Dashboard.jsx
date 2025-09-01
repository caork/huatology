import React from 'react';
import {
  Box,
  Grid,
  Paper,
  Typography,
  Card,
  CardContent,
  LinearProgress,
  Chip,
} from '@mui/material';
import {
  AccountTree as GraphIcon,
  Link as LinkIcon,
  Category as CategoryIcon,
  TrendingUp as TrendingIcon,
} from '@mui/icons-material';
import { motion } from 'framer-motion';

const Dashboard = ({ objects, links }) => {
  const totalObjects = objects.length;
  const totalLinks = links;
  const objectTypes = [...new Set(objects.map(obj => obj.type))];
  const typeDistribution = objectTypes.map(type => ({
    type,
    count: objects.filter(obj => obj.type === type).length,
    percentage: Math.round((objects.filter(obj => obj.type === type).length / totalObjects) * 100),
  }));

  const stats = [
    {
      title: 'Total Objects',
      value: totalObjects,
      icon: <GraphIcon sx={{ fontSize: 40, color: 'primary.main' }} />,
      color: 'primary.main',
    },
    {
      title: 'Total Links',
      value: totalLinks,
      icon: <LinkIcon sx={{ fontSize: 40, color: 'secondary.main' }} />,
      color: 'secondary.main',
    },
    {
      title: 'Object Types',
      value: objectTypes.length,
      icon: <CategoryIcon sx={{ fontSize: 40, color: 'success.main' }} />,
      color: 'success.main',
    },
    {
      title: 'Connectivity',
      value: totalObjects > 0 ? Math.round((totalLinks / totalObjects) * 100) / 100 : 0,
      icon: <TrendingIcon sx={{ fontSize: 40, color: 'warning.main' }} />,
      color: 'warning.main',
      suffix: ' links/object',
    },
  ];

  return (
    <Box sx={{ p: 3, height: '100%', overflow: 'auto' }}>
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
      >
        <Typography variant="h4" gutterBottom sx={{ fontWeight: 600, mb: 3 }}>
          Dashboard
        </Typography>
      </motion.div>

      {/* Stats Cards */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        {stats.map((stat, index) => (
          <Grid item xs={12} sm={6} md={3} key={stat.title}>
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5, delay: index * 0.1 }}
            >
              <Card
                sx={{
                  height: '100%',
                  background: `linear-gradient(135deg, ${stat.color}15 0%, ${stat.color}05 100%)`,
                  border: `1px solid ${stat.color}20`,
                  '&:hover': {
                    transform: 'translateY(-4px)',
                    boxShadow: 4,
                    transition: 'all 0.3s ease',
                  },
                }}
              >
                <CardContent sx={{ p: 3 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                    {stat.icon}
                    <Box sx={{ ml: 2 }}>
                      <Typography variant="h4" sx={{ fontWeight: 600, color: stat.color }}>
                        {stat.value}{stat.suffix || ''}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        {stat.title}
                      </Typography>
                    </Box>
                  </Box>
                </CardContent>
              </Card>
            </motion.div>
          </Grid>
        ))}
      </Grid>

      {/* Type Distribution */}
      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <motion.div
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.5, delay: 0.4 }}
          >
            <Paper sx={{ p: 3, height: '100%' }}>
              <Typography variant="h6" gutterBottom sx={{ fontWeight: 600 }}>
                Object Type Distribution
              </Typography>
              <Box sx={{ mt: 2 }}>
                {typeDistribution.map((item) => (
                  <Box key={item.type} sx={{ mb: 2 }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                      <Typography variant="body2">{item.type}</Typography>
                      <Typography variant="body2" color="text.secondary">
                        {item.count} ({item.percentage}%)
                      </Typography>
                    </Box>
                    <LinearProgress
                      variant="determinate"
                      value={item.percentage}
                      sx={{
                        height: 8,
                        borderRadius: 4,
                        backgroundColor: 'grey.200',
                        '& .MuiLinearProgress-bar': {
                          borderRadius: 4,
                          backgroundColor: 'primary.main',
                        },
                      }}
                    />
                  </Box>
                ))}
              </Box>
            </Paper>
          </motion.div>
        </Grid>

        <Grid item xs={12} md={6}>
          <motion.div
            initial={{ opacity: 0, x: 20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.5, delay: 0.5 }}
          >
            <Paper sx={{ p: 3, height: '100%' }}>
              <Typography variant="h6" gutterBottom sx={{ fontWeight: 600 }}>
                Recent Activity
              </Typography>
              <Box sx={{ mt: 2 }}>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                  Activity tracking will be implemented in future updates.
                </Typography>
                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                  {objectTypes.slice(0, 5).map((type) => (
                    <Chip
                      key={type}
                      label={type}
                      size="small"
                      variant="outlined"
                      sx={{ mb: 1 }}
                    />
                  ))}
                </Box>
              </Box>
            </Paper>
          </motion.div>
        </Grid>
      </Grid>

      {/* Quick Actions */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5, delay: 0.6 }}
      >
        <Paper sx={{ p: 3, mt: 3 }}>
          <Typography variant="h6" gutterBottom sx={{ fontWeight: 600 }}>
            Quick Actions
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Use the sidebar to navigate to different sections of the application.
            You can create new objects and links, search through existing data,
            and explore the graph visualization.
          </Typography>
        </Paper>
      </motion.div>
    </Box>
  );
};

export default Dashboard;