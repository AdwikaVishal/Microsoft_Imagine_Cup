// Mock API services for SenseSafe frontend
// These will be replaced with real Azure services in production

import axios from 'axios';
import { mockData } from '../mock/data.js';

// Simulate network delays
const delay = (ms = 1000) => new Promise(resolve => setTimeout(resolve, ms));

// Error simulation for testing
const simulateError = (probability = 0.1) => {
  if (Math.random() < probability) {
    throw new Error('Network error occurred');
  }
};

/**
 * Get all SOS alerts
 * @returns {Promise<Array>} Array of SOS alerts
 */
export const getSOS = async () => {
  await delay(800);
  simulateError(0.05); // 5% chance of error
  
  // TODO: Replace with real Azure Functions call
  // const response = await axios.get('/api/sos-alerts');
  // return response.data;
  
  return mockData.sosAlerts;
};

/**
 * Get all incidents
 * @returns {Promise<Array>} Array of incidents
 */
export const getIncidents = async () => {
  await delay(1200);
  simulateError(0.05);
  
  // TODO: Replace with Cosmos DB fetch
  // const response = await axios.get('/api/incidents');
  // return response.data;
  
  return mockData.incidents;
};

/**
 * Send user status update
 * @param {Object} statusData - User status data
 * @returns {Promise<Object>} Response with success status
 */
export const sendUserStatus = async (statusData) => {
  await delay(600);
  simulateError(0.08);
  
  // TODO: AZURE FUNCTIONS CALL HERE
  // const response = await axios.post('/api/user-status', statusData);
  // return response.data;
  
  console.log('📡 Sending user status to Azure Functions:', statusData);
  
  // Simulate successful response
  return {
    success: true,
    message: 'Status sent successfully',
    id: `status_${Date.now()}`,
    timestamp: new Date().toISOString(),
  };
};

/**
 * Update incident status
 * @param {Object} incidentData - Incident data to update
 * @returns {Promise<Object>} Response with updated incident
 */
export const updateIncident = async (incidentData) => {
  await delay(900);
  simulateError(0.1);
  
  // TODO: COSMOS DB UPDATE HERE
  // const response = await axios.put(`/api/incidents/${incidentData.id}`, incidentData);
  // return response.data;
  
  console.log('📝 Updating incident in Cosmos DB:', incidentData);
  
  // Simulate successful response
  return {
    success: true,
    message: 'Incident updated successfully',
    id: `incident_${Date.now()}`,
    data: incidentData,
    timestamp: new Date().toISOString(),
  };
};

/**
 * Get user profile by ID
 * @param {string} userId - User ID
 * @returns {Promise<Object>} User profile
 */
export const getUserProfile = async (userId) => {
  await delay(500);
  simulateError(0.05);
  
  // TODO: Replace with Cosmos DB query
  // const response = await axios.get(`/api/users/${userId}`);
  // return response.data;
  
  const user = mockData.users.find(u => u.id === userId);
  if (!user) {
    throw new Error('User not found');
  }
  
  return user;
};

/**
 * Send disaster alert to users
 * @param {Object} alertData - Alert data
 * @returns {Promise<Object>} Response
 */
export const sendDisasterAlert = async (alertData) => {
  await delay(1500);
  simulateError(0.1);
  
  // TODO: AZURE NOTIFICATION HUB HERE
  // const response = await axios.post('/api/disaster-alerts', alertData);
  // return response.data;
  
  console.log('🚨 Sending disaster alert via Azure Notification Hub:', alertData);
  
  return {
    success: true,
    message: 'Alert sent to all users',
    recipients: Math.floor(Math.random() * 10000) + 1000, // Mock recipient count
    timestamp: new Date().toISOString(),
  };
};

/**
 * Get AI risk assessment for user
 * @param {Object} userData - User data for risk assessment
 * @returns {Promise<Object>} Risk assessment result
 */
export const getRiskAssessment = async (userData) => {
  await delay(2000);
  simulateError(0.15);
  
  // TODO: AZURE ML RISK API HERE
  // const response = await axios.post('/api/risk-assessment', userData);
  // return response.data;
  
  const riskScore = Math.floor(Math.random() * 100);
  const riskLevel = riskScore >= 80 ? 'Critical' : 
                   riskScore >= 60 ? 'High' : 
                   riskScore >= 40 ? 'Medium' : 'Low';
  
  return {
    riskScore,
    riskLevel,
    factors: [
      'Low battery detected',
      'No movement detected',
      'Severe weather area',
      'Vulnerable user category',
    ],
    recommendation: riskScore >= 60 ? 'Immediate dispatch recommended' : 'Monitor situation',
    timestamp: new Date().toISOString(),
  };
};

/**
 * Get AI explanation for risk assessment
 * @param {string} incidentId - Incident ID
 * @returns {Promise<Object>} AI explanation
 */
export const getAIExplanation = async (incidentId) => {
  await delay(1800);
  simulateError(0.12);
  
  // TODO: AZURE OPENAI CALL HERE
  // const response = await axios.post('/api/ai-explanation', { incidentId });
  // return response.data;
  
  const explanations = [
    'High risk due to low battery, no movement, severe area risk.',
    'Elevated risk from combination of factors: user location, weather conditions, and device status.',
    'Moderate risk detected. User appears safe but monitoring recommended.',
    'Low risk assessment. Multiple positive indicators suggest user safety.',
  ];
  
  return {
    explanation: explanations[Math.floor(Math.random() * explanations.length)],
    confidence: Math.floor(Math.random() * 30) + 70, // 70-100%
    factors: [
      'Device battery level: 15%',
      'Location: High-risk zone',
      'Weather: Severe storm warning',
      'User category: Vulnerable',
    ],
    timestamp: new Date().toISOString(),
  };
};

/**
 * Authenticate user (mock)
 * @param {Object} credentials - Login credentials
 * @returns {Promise<Object>} Authentication response
 */
export const authenticateUser = async (credentials) => {
  await delay(800);
  simulateError(0.05);
  
  // TODO: AZURE AD B2C HERE
  // const response = await axios.post('/api/auth/login', credentials);
  // return response.data;
  
  if (credentials.email && credentials.password) {
    return {
      success: true,
      token: `mock_token_${Date.now()}`,
      user: {
        id: 'admin_001',
        email: credentials.email,
        role: 'admin',
        name: 'SenseSafe Administrator',
      },
      timestamp: new Date().toISOString(),
    };
  } else {
    throw new Error('Invalid credentials');
  }
};

/**
 * Get system health status
 * @returns {Promise<Object>} System health information
 */
export const getSystemHealth = async () => {
  await delay(400);
  
  // TODO: Azure Monitor integration
  const services = ['Database', 'Notification Hub', 'Functions', 'Maps', 'AI Services'];
  const status = services.map(service => ({
    service,
    status: Math.random() > 0.1 ? 'Operational' : 'Degraded', // 90% operational
    responseTime: Math.floor(Math.random() * 500) + 100, // 100-600ms
  }));
  
  return {
    overall: 'Operational',
    services,
    status,
    timestamp: new Date().toISOString(),
    uptime: '99.9%',
  };
};
