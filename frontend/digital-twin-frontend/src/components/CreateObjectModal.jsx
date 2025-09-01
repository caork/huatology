import React, { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
  Box,
  Typography,
  IconButton,
  Chip,
  Grid,
} from '@mui/material';
import {
  Add as AddIcon,
  Remove as RemoveIcon,
  Close as CloseIcon,
} from '@mui/icons-material';
import { motion, AnimatePresence } from 'framer-motion';

const CreateObjectModal = ({ open, onClose, onSubmit, loading }) => {
  const [objectType, setObjectType] = useState('');
  const [properties, setProperties] = useState([{ key: '', value: '' }]);

  const handleSubmit = (e) => {
    e.preventDefault();
    const props = {};
    properties.forEach((prop) => {
      if (prop.key.trim() && prop.value.trim()) {
        props[prop.key.trim()] = prop.value.trim();
      }
    });

    onSubmit({
      type: objectType.trim(),
      properties: props,
    });

    // Reset form
    setObjectType('');
    setProperties([{ key: '', value: '' }]);
  };

  const addProperty = () => {
    setProperties([...properties, { key: '', value: '' }]);
  };

  const removeProperty = (index) => {
    if (properties.length > 1) {
      setProperties(properties.filter((_, i) => i !== index));
    }
  };

  const updateProperty = (index, field, value) => {
    const updated = [...properties];
    updated[index][field] = value;
    setProperties(updated);
  };

  const handleClose = () => {
    setObjectType('');
    setProperties([{ key: '', value: '' }]);
    onClose();
  };

  return (
    <Dialog
      open={open}
      onClose={handleClose}
      maxWidth="md"
      fullWidth
      PaperProps={{
        component: motion.div,
        initial: { opacity: 0, scale: 0.9, y: 20 },
        animate: { opacity: 1, scale: 1, y: 0 },
        exit: { opacity: 0, scale: 0.9, y: 20 },
        transition: { duration: 0.3 },
      }}
    >
      <DialogTitle sx={{ pb: 1 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <Typography variant="h6" component="div" sx={{ fontWeight: 600 }}>
            Create New Object
          </Typography>
          <IconButton onClick={handleClose} size="small">
            <CloseIcon />
          </IconButton>
        </Box>
      </DialogTitle>

      <form onSubmit={handleSubmit}>
        <DialogContent>
          <Box sx={{ mb: 3 }}>
            <TextField
              fullWidth
              label="Object Type"
              value={objectType}
              onChange={(e) => setObjectType(e.target.value)}
              placeholder="e.g., Person, Product, Organization"
              required
              variant="outlined"
              sx={{ mb: 2 }}
            />
          </Box>

          <Typography variant="h6" gutterBottom sx={{ color: 'text.secondary' }}>
            Properties
          </Typography>

          <AnimatePresence>
            {properties.map((property, index) => (
              <motion.div
                key={index}
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: 20 }}
                transition={{ duration: 0.2 }}
              >
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
                  <TextField
                    label="Key"
                    value={property.key}
                    onChange={(e) => updateProperty(index, 'key', e.target.value)}
                    placeholder="Property name"
                    size="small"
                    sx={{ flex: 1 }}
                  />
                  <TextField
                    label="Value"
                    value={property.value}
                    onChange={(e) => updateProperty(index, 'value', e.target.value)}
                    placeholder="Property value"
                    size="small"
                    sx={{ flex: 1 }}
                  />
                  <IconButton
                    onClick={() => removeProperty(index)}
                    disabled={properties.length === 1}
                    color="error"
                    size="small"
                  >
                    <RemoveIcon />
                  </IconButton>
                </Box>
              </motion.div>
            ))}
          </AnimatePresence>

          <Button
            onClick={addProperty}
            startIcon={<AddIcon />}
            variant="outlined"
            size="small"
            sx={{ mt: 1 }}
          >
            Add Property
          </Button>
        </DialogContent>

        <DialogActions sx={{ px: 3, pb: 3 }}>
          <Button onClick={handleClose} variant="outlined">
            Cancel
          </Button>
          <Button
            type="submit"
            variant="contained"
            disabled={loading || !objectType.trim()}
            sx={{
              minWidth: 120,
              background: 'linear-gradient(45deg, #2196F3 30%, #21CBF3 90%)',
              '&:hover': {
                background: 'linear-gradient(45deg, #1976D2 30%, #1CB5E0 90%)',
              },
            }}
          >
            {loading ? 'Creating...' : 'Create Object'}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
};

export default CreateObjectModal;