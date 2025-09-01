import React from 'react';
import {
  AppBar,
  Toolbar,
  Typography,
  IconButton,
  Box,
  Chip,
  useTheme,
  useMediaQuery,
} from '@mui/material';
import {
  Refresh as RefreshIcon,
  Person as PersonIcon,
  ExitToApp as LogoutIcon,
} from '@mui/icons-material';
import { useAuth } from '../context/AuthContext';
import { motion } from 'framer-motion';
import Sidebar from './Sidebar';

const Header = ({
  activeView,
  setActiveView,
  mobileOpen,
  setMobileOpen,
  onRefresh,
  isLoading,
  stats
}) => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const { user, logout } = useAuth();

  return (
    <AppBar
      position="fixed"
      sx={{
        zIndex: theme.zIndex.drawer + 1,
        background: `linear-gradient(135deg, ${theme.palette.primary.main} 0%, ${theme.palette.primary.dark} 100%)`,
        boxShadow: '0 4px 20px rgba(0,0,0,0.15)',
      }}
    >
      <Toolbar>
        {/* Mobile Sidebar Toggle */}
        <Sidebar
          activeView={activeView}
          setActiveView={setActiveView}
          mobileOpen={mobileOpen}
          setMobileOpen={setMobileOpen}
        />

        {/* Title */}
        <motion.div
          initial={{ opacity: 0, x: -20 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ duration: 0.5 }}
        >
          <Typography
            variant="h6"
            component="div"
            sx={{
              flexGrow: 1,
              fontWeight: 600,
              display: { xs: 'none', sm: 'block' }
            }}
          >
            Digital Twin Ontology System
          </Typography>
        </motion.div>

        {/* Stats */}
        {!isMobile && (
          <Box sx={{ display: 'flex', gap: 2, mr: 2 }}>
            <motion.div
              initial={{ opacity: 0, y: -10 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5, delay: 0.1 }}
            >
              <Chip
                label={`Objects: ${stats.objects}`}
                size="small"
                sx={{
                  backgroundColor: 'rgba(255,255,255,0.2)',
                  color: 'white',
                  '&:hover': { backgroundColor: 'rgba(255,255,255,0.3)' }
                }}
              />
            </motion.div>
            <motion.div
              initial={{ opacity: 0, y: -10 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5, delay: 0.2 }}
            >
              <Chip
                label={`Links: ${stats.links}`}
                size="small"
                sx={{
                  backgroundColor: 'rgba(255,255,255,0.2)',
                  color: 'white',
                  '&:hover': { backgroundColor: 'rgba(255,255,255,0.3)' }
                }}
              />
            </motion.div>
          </Box>
        )}

        {/* Actions */}
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <motion.div
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.95 }}
          >
            <IconButton
              color="inherit"
              onClick={onRefresh}
              disabled={isLoading}
              sx={{
                '&:hover': {
                  backgroundColor: 'rgba(255,255,255,0.1)',
                }
              }}
            >
              <RefreshIcon />
            </IconButton>
          </motion.div>

          {/* User Info */}
          {user && (
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                <PersonIcon fontSize="small" />
                <Typography
                  variant="body2"
                  sx={{
                    display: { xs: 'none', md: 'block' },
                    maxWidth: 120,
                    overflow: 'hidden',
                    textOverflow: 'ellipsis'
                  }}
                >
                  {user.username}
                </Typography>
              </Box>

              <motion.div
                whileHover={{ scale: 1.05 }}
                whileTap={{ scale: 0.95 }}
              >
                <IconButton
                  color="inherit"
                  onClick={logout}
                  sx={{
                    '&:hover': {
                      backgroundColor: 'rgba(255,255,255,0.1)',
                    }
                  }}
                >
                  <LogoutIcon />
                </IconButton>
              </motion.div>
            </Box>
          )}
        </Box>
      </Toolbar>
    </AppBar>
  );
};

export default Header;