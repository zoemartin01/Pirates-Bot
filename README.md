# 🏴‍☠️ Pirates-Bot

## Table of contents
* [Features](#features)
* [Talking to the bot](#talking-to-the-bot)
* [Development](#development)

## Features
* **discord data:** show all data about servers, users or emoji
* **autoresponse:** make the bot automatically respond to messages that contain trigger words
* **assemblies:** collect users from various channels in one channel and move them back when needed
* **custom prefix:** set a custom prefix for running commands in your server
* **moderation:** keep track of user warnings

## Talking to the bot
You can talk to the bot using commands. Get started by sending `@mention help` to a text channel with the bot. 

## Development
Follow these steps to set up your development environment:
1. Clone the repository: `git clone https://github.com/zoemartin01/Pirates-Bot`
2. Open the repository folder with your code editor of choice
3. Run `./gradlew run --args="<discord-token> jdbc:postgresql://<database-host>/<database> <database-user> <database-password>"` to start the bot

