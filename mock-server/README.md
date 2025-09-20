# VPN Client Mock Server

This directory contains a mock REST API server using json-server for development and testing of the VPN Client Android app.

## Setup

1. Install dependencies:
```bash
cd mock-server
npm install
```

2. Start the server:
```bash
npm start
```

The server will run on `http://localhost:3000` by default.

## API Endpoints

### Authentication
- `POST /api/v1/auth/login` - Login with email/password
  - Body: `{"email": "test@vpn.com", "password": "password123"}`
  - Response: `{"id": 1, "email": "test@vpn.com", "token": "mock_jwt_token_12345"}`

### VPN Nodes
- `GET /api/v1/nodes` - Get list of available VPN nodes
  - Response: Array of node objects with id, name, country, latency_ms, public_key, endpoint_ip

### Sessions
- `GET /api/v1/sessions` - Get connection sessions
- `POST /api/v1/sessions` - Create new session
- `PUT /api/v1/sessions/:id` - Update session

## Test Credentials

- Email: `test@vpn.com`, Password: `password123`
- Email: `admin@vpn.com`, Password: `admin123`

## Development Mode

Use `npm run dev` to start with artificial delay (500ms) to simulate real network conditions.
