# Redstonecloud

This project is currently under development and far from finished. Using it is currently not possible, but you can investigate the source code and test it out if you know what you're doing.

## Goal
My goal is to build an ecosystem around Minecraft networks providing a web interface to set it up.
It can run on any current Windows or Linux installation but its target is something like Kubernetes
running a master pod for managing java installations, templates and persistent data storage
and many agents for all the workload. Using LoadBalancers for Proxies allows a giant number of players on the same network.

You can join [here](https://discord.gg/aZKuas4) to discuss this project or use the discussion tab.

## Concept
```mermaid
flowchart TB
    entrypoint[Entrypoint]-->loadbalancer[Load Balancer]
    subgraph proxies [Entry Proxies]
        proxy_1[Proxy]
        proxy_2[Proxy]
        proxy_1 & proxy_2 -.- proxy_db_1[(Database)]
    end
    loadbalancer-->proxy_1 & proxy_2
    proxy_1 & proxy_2 ---> lobby_proxy_1 & lobby_proxy_2 & game_1_proxy_1 & game_1_proxy_2 & game_2_proxy_1 & game_2_proxy_2

    subgraph subproxies [Game Proxies]
        subgraph lobbyproxies [Lobby Proxies]
            lobby_proxy_1[Proxy]
            lobby_proxy_2[Proxy]
            lobby_proxy_1 & lobby_proxy_2 -.- lobbyproxy_db_1[(Database)]
        end
        subgraph game1proxies [Game 1 Proxies]
            game_1_proxy_1[Proxy]
            game_1_proxy_2[Proxy]
            game_1_proxy_1 & game_1_proxy_2 -.- game_1_proxy_db_1[(Database)]
        end
        subgraph game2proxies [Game 2 Proxies]
            game_2_proxy_1[Proxy]
            game_2_proxy_2[Proxy]
            game_2_proxy_1 & game_2_proxy_2 -.- game_2_proxy_db_1[(Database)]
        end
    end
    
    subgraph gameservers [Game Servers]
        subgraph lobby_sub [Lobby Subservers]
            lobby_sub_1[Lobby Subserver 1]
            lobby_sub_2[Lobby Subserver 2]
        end
        lobby_proxy_1 & lobby_proxy_2 ---> lobby_sub_1 & lobby_sub_2
        subgraph game_1_sub [Game 1 Game Servers]
            game_1_sub_1[Game 1 Subserver 1]
            game_1_sub_3[Game 1 Subserver 3]
        end
        game_1_proxy_1 & game_1_proxy_2 ---> game_1_sub_1 & game_1_sub_3
        subgraph game_2_sub [Game 2 Game Servers]
            game_2_sub_1[Game 1 Subserver 1]
            game_2_sub_3[Game 1 Subserver 3]
        end
        game_2_proxy_1 & game_2_proxy_2 ---> game_2_sub_1 & game_2_sub_3
    end
```
