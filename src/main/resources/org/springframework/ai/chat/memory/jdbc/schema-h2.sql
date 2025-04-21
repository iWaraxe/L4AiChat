CREATE TABLE IF NOT EXISTS ai_chat_memory (
    id IDENTITY PRIMARY KEY,
    conversation_id VARCHAR(255),
    message_type VARCHAR(50),
    role VARCHAR(50),
    content CLOB,
    created TIMESTAMP
);