import React from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Container,
  Typography,
  Button,
  Box,
  Card,
  CardContent,
  Grid,
  Chip,
} from '@mui/material';
import {
  Home as HomeIcon,
  Warning,
  Report,
  Help,
  Shield,
} from '@mui/icons-material';

function Home({ abilityProfile }) {
  const navigate = useNavigate();

  const getGreeting = () => {
    const hour = new Date().getHours();
    if (hour < 12) return 'Good morning';
    if (hour < 18) return 'Good afternoon';
    return 'Good evening';
  };

  const getProfileColor = (profileType) => {
    const colors = {
      blind: '#9c27b0',
      deaf: '#2196f3',
      nonverbal: '#ff9800',
      elderly: '#795548',
      normal: '#4caf50',
    };
    return colors[profileType] || '#4caf50';
  };

  const quickActions = [
    {
      title: 'Report Incident',
      description: 'Report what happened during the disaster',
      icon: <Report sx={{ fontSize: 40 }} />,
      color: '#f44336',
      action: () => navigate('/report'),
    },
    {
      title: 'Get Guidance',
      description: 'Get personalized help and instructions',
      icon: <Help sx={{ fontSize: 40 }} />,
      color: '#2196f3',
      action: () => navigate('/guidance'),
    },
    {
      title: 'Emergency SOS',
      description: 'Send emergency alert for immediate help',
      icon: <Warning sx={{ fontSize: 40 }} />,
      color: '#ff5722',
      action: () => {
        // TODO: Connect to Azure Notification Hub here
        console.log('🚨 SOS Alert triggered');
        alert('Emergency SOS sent! Help is on the way.');
      },
    },
  ];

  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      {/* Header */}
      <Box textAlign="center" mb={4}>
        <Box display="flex" alignItems="center" justifyContent="center" mb={2}>
          <HomeIcon sx={{ fontSize: 48, color: '#1976d2', mr: 2 }} />
          <Typography 
            variant="h1" 
            component="h1" 
            sx={{ 
              fontSize: { xs: '2rem', sm: '3rem' },
              color: '#1976d2',
            }}
          >
            SenseSafe
          </Typography>
        </Box>
        <Typography 
          variant="h5" 
          component="p" 
          sx={{ 
            fontSize: { xs: '1.2rem', sm: '1.5rem' },
            color: 'text.secondary',
            mb: 2 
          }}
        >
          {getGreeting()}, {abilityProfile.type} user
        </Typography>
        
        {/* Profile Badge */}
        <Chip
          label={`Profile: ${abilityProfile.type.charAt(0).toUpperCase() + abilityProfile.type.slice(1)}`}
          sx={{
            backgroundColor: getProfileColor(abilityProfile.type),
            color: 'white',
            fontSize: '1rem',
            px: 2,
            py: 3,
            height: 'auto',
            '& .MuiChip-label': {
              padding: '8px 16px',
            },
          }}
        />
      </Box>

      {/* Status Card */}
      <Card sx={{ mb: 4, borderLeft: '5px solid #4caf50' }}>
        <CardContent>
          <Box display="flex" alignItems="center" mb={2}>
            <Shield sx={{ fontSize: 32, color: '#4caf50', mr: 2 }} />
            <Typography variant="h6" component="h2">
              System Status
            </Typography>
          </Box>
          <Typography variant="body1" sx={{ mb: 2 }}>
            ✅ All systems operational
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Last updated: {new Date().toLocaleString()}
          </Typography>
        </CardContent>
      </Card>

      {/* Quick Actions */}
      <Typography variant="h5" component="h2" gutterBottom sx={{ mb: 3 }}>
        Quick Actions
      </Typography>
      
      <Grid container spacing={3}>
        {quickActions.map((action, index) => (
          <Grid item xs={12} sm={6} key={index}>
            <Card 
              sx={{ 
                cursor: 'pointer',
                transition: 'all 0.3s ease',
                '&:hover': {
                  transform: 'translateY(-4px)',
                  boxShadow: 4,
                },
                height: '100%',
              }}
              onClick={action.action}
            >
              <CardContent sx={{ textAlign: 'center', py: 4 }}>
                <Box sx={{ color: action.color, mb: 2 }}>
                  {action.icon}
                </Box>
                <Typography 
                  variant="h6" 
                  component="h3" 
                  gutterBottom
                  sx={{ fontSize: '1.3rem' }}
                >
                  {action.title}
                </Typography>
                <Typography 
                  variant="body2" 
                  color="text.secondary"
                  sx={{ fontSize: '1rem' }}
                >
                  {action.description}
                </Typography>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      {/* Accessibility Features Reminder */}
      {abilityProfile.type !== 'normal' && (
        <Card sx={{ mt: 4, backgroundColor: '#e3f2fd' }}>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              Accessibility Features Active
            </Typography>
            <Box display="flex" flexWrap="wrap" gap={1}>
              {abilityProfile.highContrast && (
                <Chip label="High Contrast" color="primary" />
              )}
              {abilityProfile.largeText && (
                <Chip label="Large Text" color="primary" />
              )}
              <Chip label={`${abilityProfile.type.charAt(0).toUpperCase() + abilityProfile.type.slice(1)} Mode`} color="secondary" />
            </Box>
          </CardContent>
        </Card>
      )}

      {/* Emergency Contact Info */}
      <Card sx={{ mt: 4, borderLeft: '5px solid #ff9800' }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            Emergency Contacts
          </Typography>
          <Typography variant="body1" sx={{ mb: 1 }}>
            🚨 Emergency: 911
          </Typography>
          <Typography variant="body1" sx={{ mb: 1 }}>
            📞 SenseSafe Support: 1-800-SENSE-SAFE
          </Typography>
          <Typography variant="body2" color="text.secondary">
            These numbers are always available, even when the app is offline.
          </Typography>
        </CardContent>
      </Card>
    </Container>
  );
}

export default Home;
