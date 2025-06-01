# LiquidBounce MCP Configuration (SSE-Based)

This document describes the configuration for using LiquidBounce MCP with Server-Sent Events (SSE).

## Configuration Structure

```json
{
  "liquidbounce_mcp": {
    "type": "sse",
    "url": "http://localhost:8080/see"
  }
}
```


## Usage
1.Enable your MCP Module in game.

2.Open the MCP software that supports the LLM model, find the Add MCP server, 
and copy and paste the json snippet above.

3.Load your LLM model (e.g. Claude) and find the liquidbounce_mcp in the mcp server, 
and then you can query the state in your game by the LLM model.

4.If you get the message "Server started failed",
it is likely that the port you configured in the MC module is occupied,
please modify the port and then re-enable the module.

5.Enjoy your MCP experience!

## Tips

1.The numbers for all computer ports are between 0 and 66565, 
which means you cannot set a number beyond 66565 or below 1.

2.If "Server started failed" occurs, please check whether the port is occupied,
one of the most common cases is that you have two game instances open at the same time,
in this case, you need to close the MCP module in one of the game instances,
or set the MCP ports in the two game instances to different ports.


## Features

1.get Player Status(including name,health, position, hunger, mainHandItem,etc.)

2.get World Status(including time, weather,players,etc. )

3.get Server Status(including server name, server version, server ip, server port, etc.)

4.send messages to the client(only you can see).

5.send meesages to the server(all players in server can see).



