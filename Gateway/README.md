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
- Moonshine Control Protocol (not without TLS 1.3 or QUIC)
  - Everything from Minecraft Protocol (and over TLS 1.3)
  - Negotiated as post-handshake and pre-configuration state
  - Frames for Minecraft traffic
    - More Authentication modes for staff members (TBD)
      - WebAuthn
      - TOTP
  - Traffic control frames (QUIC only)
  - Unicast messaging frames
    - Query for player presence
    - Plugin defined messages
- QUIC (TBD)
  - Everything above
  - Dynamic Multiplexing Control
