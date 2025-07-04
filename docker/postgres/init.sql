-- Initialize L4AiChat database
-- This script runs when the PostgreSQL container starts for the first time

-- Create additional schemas if needed
CREATE SCHEMA IF NOT EXISTS security;
CREATE SCHEMA IF NOT EXISTS ai_chat;
CREATE SCHEMA IF NOT EXISTS monitoring;

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_stat_statements";

-- Create indexes for better performance
-- These will be created by JPA/Hibernate but we can pre-create them

-- Grant permissions
GRANT ALL PRIVILEGES ON DATABASE l4aichat TO l4aichat;
GRANT ALL PRIVILEGES ON SCHEMA public TO l4aichat;
GRANT ALL PRIVILEGES ON SCHEMA security TO l4aichat;
GRANT ALL PRIVILEGES ON SCHEMA ai_chat TO l4aichat;
GRANT ALL PRIVILEGES ON SCHEMA monitoring TO l4aichat;

-- Set timezone
SET timezone = 'UTC';

-- Insert initial data if needed
-- This can be used for default admin user, etc.

COMMENT ON DATABASE l4aichat IS 'L4AiChat Spring AI Course Database';
COMMENT ON SCHEMA security IS 'Security-related tables (users, roles, etc.)';
COMMENT ON SCHEMA ai_chat IS 'AI chat conversation data';
COMMENT ON SCHEMA monitoring IS 'Application monitoring and metrics data';