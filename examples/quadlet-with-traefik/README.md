# Rootless Mopat with TLS

This document explains how to run MoPat as a container using **rootless Podman** and **quadlets**, with **TLS and secrets** for sensitive information such as passwords.

Using a dedicated service account without root offers advantages when multiple users share the server.

## Preparation

The following packages are needed:

- `podman`
- `passt`
- `podman-docker`

Ensure your user has the necessary permissions and UID/GID mappings for rootless containers.
Create a user to run the services and grant the necessary permissions:

```bash
sudo adduser podman
sudo usermod --add-subuids 100000-165535 podman
sudo usermod --add-subgids 100000-165535 podman

# allows access to logs
sudo usermod -a -G systemd-journal podman

# lets services run even when the user is not logged in
sudo loginctl enable-linger podman
```

## Installation (rootless)

Switch to the service user `sudo --login -u podman`.

```bash
# Create the directory for quadlet configurations and move your files
mkdir -p ~/.config/containers/systemd/mopat/db
mv ./examples/qadlet-with-traefik/* ~/.config/containers/systemd/mopat/
mv ./db/* ~/.config/containers/systemd/mopat/db/

# Start the user socket for container discovery inside of traefik
systemctl --user enable --now podman.socket
```

By default these paths are mounted:

- `/data/db`
- `/data/images`
- `/data/upload`
- `/data/export/FHIR`
- `/data/export/HL7`
- `/data/export/ODM`

You can customize these paths in the .container files.

## Secrets Certs and Permissions

This setup uses Podman secrets to store confidential values instead of environment variables.

```bash
# Generate and save random credentials
printf "mopat" | podman secret create MYSQL_USER -
openssl rand -base64 30 | podman secret create MYSQL_ROOT_PASSWORD -
openssl rand -base64 30 | podman secret create MYSQL_PASSWORD -
openssl rand -base64 30 | podman secret create PEPPER -

# To check a secret value
podman secret inspect --showsecret --format "{{.Spec.Name}} {{.SecretData}}" MYSQL_PASSWORD
```

Rootless containers use user namespace remapping. Update the ownership of directories accordingly:

```bash
podman unshare chown -R 999:999 .config/containers/systemd/mopat/db/
podman unshare chown -R 999:999 /data/db/
```

For securing your connection with TLS you need to place your cert under:

- `.config/containers/systemd/mopat/ssl/server.crt`
- `.config/containers/systemd/mopat/ssl/server.key`

## Starting and Logs

Quadlets are managed by the systemd generator and do not require a central daemon like Docker Compose.

```bash
# After adding or modifying quadlet files, reload the user systemd daemon
systemctl --user daemon-reload

# start the containers
systemctl --user start db-container
systemctl --user start webapp-container
systemctl --user start traefik

# show status
systemctl --user status traefik
# show logs
journalctl --user -xeu traefik
```

The services are autoautomatically started at boot. This can be configured in the [Install] part of the .container files.
