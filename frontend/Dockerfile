# ==============================================================================
# Stage 1: Build Angular Application
# ==============================================================================
FROM docker.io/library/node:22-alpine AS build

WORKDIR /app

# Install dependencies with cache optimization
COPY package.json package-lock.json ./
RUN npm ci

# Copy Angular application source
COPY . ./

# Build production bundle
RUN npm run build -- --configuration production

# ==============================================================================
# Stage 2: Production NGINX Web Server & API Reverse Proxy
# ==============================================================================
FROM docker.io/library/nginx:1.27-alpine AS runtime

# Remove default NGINX configuration and sample html
RUN rm -rf /usr/share/nginx/html/* /etc/nginx/conf.d/default.conf

# Copy compiled Angular assets from build stage
COPY --from=build /app/dist/Angular/browser /usr/share/nginx/html

# Copy custom NGINX core configuration
COPY nginx/nginx.conf /etc/nginx/nginx.conf

# Copy dynamic virtual host template (processed by entrypoint envsubst)
COPY nginx/default.conf.template /etc/nginx/templates/default.conf.template

# Default environment variables for backend routing
ENV BACKEND_HOST="host.containers.internal" \
    BACKEND_PORT="8080" \
    NGINX_ENVSUBST_FILTER="BACKEND_"

# Expose HTTP port
EXPOSE 80

# Health check probe against local NGINX endpoint
HEALTHCHECK --interval=30s --timeout=5s --start-period=5s --retries=3 \
    CMD wget -qO- http://127.0.0.1:80/health || exit 1

CMD ["nginx", "-g", "daemon off;"]
