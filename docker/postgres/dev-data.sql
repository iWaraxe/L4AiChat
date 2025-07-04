-- Development data for L4AiChat
-- This script provides initial data for development and testing

-- Insert default admin user (password: admin123)
-- Note: This will be handled by the application's user registration
-- but kept here for reference

-- Sample chat conversations for testing
-- These will be created through the API during development

-- Development configuration data
INSERT INTO public.app_config (key, value, description) VALUES 
('default_model', 'gpt-3.5-turbo', 'Default AI model for chat'),
('max_conversation_length', '10', 'Maximum messages per conversation'),
('enable_rate_limiting', 'false', 'Enable rate limiting in development')
ON CONFLICT (key) DO NOTHING;

-- Sample roles and permissions
-- These will be managed by the security module

COMMENT ON TABLE public.app_config IS 'Application configuration for development';

-- Create some test data for development
-- This helps developers quickly test features without manual setup