// Utility function to extract error messages from API error responses
export const getErrorMessage = (error) => {
  if (error.response?.data) {
    const data = error.response.data;
    
    // New ErrorResponse format with messages array
    if (data.messages && Array.isArray(data.messages)) {
      return data.messages.join('\n');
    }
    
    // Old MessageResponse format with single message
    if (data.message) {
      return data.message;
    }
    
    // Fallback for errors with error field
    if (data.error) {
      return data.error;
    }
  }
  
  // Default error message
  return 'An unexpected error occurred. Please try again.';
};

// Format error messages for display with bullet points
export const formatErrorMessages = (error) => {
  if (error.response?.data?.messages && Array.isArray(error.response.data.messages)) {
    const messages = error.response.data.messages;
    if (messages.length === 1) {
      return messages[0];
    }
    return messages.map((msg, index) => `• ${msg}`).join('\n');
  }
  
  return getErrorMessage(error);
};
