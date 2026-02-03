// API configuration
// In Docker: gateway is accessible at http://gateway:8080
// In development: gateway is at http://localhost:8080
export const API_BASE_URL = window.location.hostname === 'localhost' 
  ? 'http://localhost:8080' 
  : `http://${window.location.hostname}:8080`;
