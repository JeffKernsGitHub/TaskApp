# ==============================================================================
# Custom Jenkins Controller with Eclipse Temurin JDK 25, Podman CLI, and K8s Tools
# ==============================================================================
FROM jenkins/jenkins:lts-jdk21

LABEL maintainer="TaskApp DevOps Team"
LABEL description="Jenkins Controller container with JDK 25, Podman CLI, kubectl, and kustomize for TaskApp CI/CD"

USER root

# 1. Install prerequisites and system utilities
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
      ca-certificates \
      curl \
      gnupg \
      lsb-release \
      gosu \
      jq \
      unzip \
      tar \
      git && \
    rm -rf /var/lib/apt/lists/*

# 2. Install Eclipse Temurin JDK 25
ENV JAVA25_HOME=/opt/java/openjdk-25
ARG JDK25_URL="https://api.adoptium.net/v3/binary/latest/25/ga/linux/x64/jdk/hotspot/normal/eclipse"
RUN mkdir -p ${JAVA25_HOME} && \
    curl -fsSL "${JDK25_URL}" -o /tmp/openjdk-25.tar.gz && \
    tar -xzf /tmp/openjdk-25.tar.gz -C ${JAVA25_HOME} --strip-components=1 && \
    rm /tmp/openjdk-25.tar.gz && \
    mkdir -p /usr/lib/jvm && \
    ln -s ${JAVA25_HOME} /usr/lib/jvm/temurin-25-jdk

# 3. Install Podman CLI & Podman Compose
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
      podman \
      podman-docker \
      podman-compose && \
    rm -rf /var/lib/apt/lists/*

# 4. Install Kubectl CLI
RUN KUBECTL_VERSION=$(curl -L -s https://dl.k8s.io/release/stable.txt) && \
    curl -fsSL "https://dl.k8s.io/release/${KUBECTL_VERSION}/bin/linux/amd64/kubectl" -o /usr/local/bin/kubectl && \
    chmod +x /usr/local/bin/kubectl

# 5. Install CNCF Kustomize CLI
RUN KUSTOMIZE_VERSION=$(curl -s https://api.github.com/repos/kubernetes-sigs/kustomize/releases/latest | jq -r '.tag_name // "kustomize/v5.5.0"' | sed 's#^kustomize/##') && \
    curl -fsSL "https://github.com/kubernetes-sigs/kustomize/releases/download/kustomize%2F${KUSTOMIZE_VERSION}/kustomize_${KUSTOMIZE_VERSION}_linux_amd64.tar.gz" -o /tmp/kustomize.tar.gz && \
    tar -xzf /tmp/kustomize.tar.gz -C /usr/local/bin/ && \
    chmod +x /usr/local/bin/kustomize && \
    rm /tmp/kustomize.tar.gz

# 6. Install Node.js 22 LTS & npm (for Angular frontend pipeline builds)
RUN curl -fsSL https://deb.nodesource.com/setup_22.x | bash - && \
    apt-get install -y --no-install-recommends nodejs && \
    rm -rf /var/lib/apt/lists/*

# 7. Configure Podman/Docker group and add jenkins user
RUN groupadd -for podman && \
    usermod -aG podman jenkins

# 8. Pre-install Jenkins Plugins
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt

# 9. Configure Entrypoint wrapper
COPY entrypoint.sh /usr/local/bin/entrypoint-wrapper.sh
RUN chmod +x /usr/local/bin/entrypoint-wrapper.sh

# Environment variables
ENV PATH="${JAVA25_HOME}/bin:${PATH}"
ENV JAVA25_HOME="${JAVA25_HOME}"
ENV CONTAINER_HOST="unix:///run/podman/podman.sock"

ENTRYPOINT ["/usr/local/bin/entrypoint-wrapper.sh"]
CMD ["/usr/bin/tini", "--", "/usr/local/bin/jenkins.sh"]
