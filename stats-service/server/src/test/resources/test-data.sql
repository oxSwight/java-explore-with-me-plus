INSERT INTO hits (app, uri, ip, timestamp)
VALUES
    ('event-service', '/events/1', '12.452.0.1', TIMESTAMP '2025-01-01 12:00:00'),
    ('event-service', '/events/1', '12.452.0.1', TIMESTAMP '2025-01-01 12:30:00'),
    ('event-service', '/events/1', '12.452.0.1', TIMESTAMP '2025-01-02 10:00:00'),
    ('event-service', '/events/2', '12.452.0.4', TIMESTAMP '2025-01-01 15:00:00'),
    ('event-service', '/events/2', '12.452.0.5', TIMESTAMP '2025-01-02 11:00:00'),
    ('user-service', '/users', '192.168.1.5', TIMESTAMP '2025-01-02 14:00:00');