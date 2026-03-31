# Dokploy Deployment Guide for Petcare Backend

## Problem Analysis

Based on your description, the issue is with port mapping in Dokploy. Your Spring Boot backend is configured to run on port 8088, but you're trying to map host port 80 to container port 80, where nothing is listening.

## Solution Options

### Option 1: Change Backend to Port 80 (Recommended for Dokploy)

Modify your backend to listen on port 80 inside the container, then map host port 80:80.

#### Step 1: Update application.properties

```properties
# src/main/resources/application.properties
server.port=80
```

#### Step 2: Update Dockerfile (if needed)

Your current Dockerfile already exposes port 8088. You'll need to change this:

```dockerfile
# Change line 54 from:
EXPOSE 8088
# To:
EXPOSE 80
```

#### Step 3: Update Health Check

Update the health check in your Dockerfile:

```dockerfile
# Change line 51 from:
CMD wget --no-verbose --tries=1 --spider http://localhost:8088/actuator/health || exit 1
# To:
CMD wget --no-verbose --tries=1 --spider http://localhost:80/actuator/health || exit 1
```

### Option 2: Keep Backend on Port 8088, Change Dokploy Mapping

Keep your backend on port 8088 and configure Dokploy to map host port 80 to container port 8088.

#### In Dokploy Dashboard

1. When creating/editing your service
2. Go to "Ports" or "Network" section
3. Set:
   - Host Port: 80
   - Container Port: 8088
   - Protocol: TCP

### Option 3: Deploy Frontend and Backend as Separate Services

If you actually have a frontend (not visible in current repo), deploy them separately:

#### Backend Service

- Host Port: 8088 (or 80 if preferred)
- Container Port: 8088
- Environment: SPRING_PROFILES_ACTIVE=prod

#### Frontend Service

- Host Port: 80
- Container Port: 80 (or whatever your frontend uses)
- Set up reverse proxy or subdomain routing

## Dokploy-Specific Configuration

### Using Dokploy's Port Management Feature

According to Dokploy documentation (PR #930), you can manage additional ports:

1. In your Dokploy service settings, look for "Additional Ports"
2. Add port mapping: 8088:8088 (host:container)
3. Access backend via: `http://your-domain.com:8088`
4. Keep frontend on port 80: `http://your-domain.com`

### Environment Variables for Dokploy

Consider adding these to your Dokploy service configuration:

```properties
SERVER_PORT=80
SPRING_PROFILES_ACTIVE=prod
```

## Verification Steps

1. After deploying, check Dokploy logs to confirm backend started on correct port
2. Test backend health endpoint:
   - If on port 80: `http://your-domain.com/actuator/health`
   - If on port 8088: `http://your-domain.com:8088/actuator/health`
3. Test API endpoints:
   - `http://your-domain.com/api/v1/users` (if on port 80)
   - `http://your-domain.com:8088/api/v1/users` (if on port 8088)

## Troubleshooting

### If You Still See No Backend Response

1. Check Dokploy container logs for startup errors
2. Verify the container is actually listening on the expected port:

   ```bash
   # In Dokploy, you might need to use their exec feature or check logs
   # Look for: "Tomcat started on port(s): 80 (http)"
   ```

3. Ensure no other process is conflicting on port 80
4. Check if Dokploy has any reverse proxy or nginx layer that might be interfering

### Dokploy Port Mapping Best Practices

1. Always verify what port your application is listening on internally
2. Map host port to the correct container port
3. Use Dokploy's port inspection tools to confirm mappings
4. Start with simple mappings before adding complexity

## Recommended Approach for Your Case

Since you only have a backend in this repository:

1. **Modify backend to use port 80** (simplest for Dokploy)
2. **Deploy single service** with host port 80:80 mapping
3. **Access your API** at `http://your-domain.com/api/v1/...`

This avoids port confusion and follows common Dokploy patterns for single-service deployments.

