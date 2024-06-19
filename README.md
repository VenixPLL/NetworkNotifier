# NetworkNotifier

This simple Java program allows you to monitor your realtime ping to configured endpoints (combined)
and send windows notifications when your network connection fails or is unstable (elevated ping)

## Features
- Warning notifications
- Monitoring in the background
- XChart powered Graph with realtime ping values

## Configuration

Ping checking interval is set to 1 second by default

Preconfigured endpoints leads to google/cloudflare dns servers.

## TODO
- Fix sleep issue with malformed ping reports
- Fix sleep issue with keeping up ticks by JVM (Just restart the thread)

## Building
- You can build this project using Maven 
```mvn clean compile assembly:single```
- Or create an artifact with dependencies in IntelliJ