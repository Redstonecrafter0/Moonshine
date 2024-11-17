# Gateway

This application is the entrypoint for every player joining the Minecraft Server Network.

## Features
- Minecraft Protocol
  - VirtualHost Support
  - UUID-based Load Distribution
  - Customizable ServerListPing handlers
- Minecraft Protocol over TLS 1.3
  - VirtualHost Support
  - Server certificate verification for players
  - Client certificate verification for staff members (protection against account theft)
- QUIC (TBD)
  - Everything above
  - Dynamic Multiplexing Control
