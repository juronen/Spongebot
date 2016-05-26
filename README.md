This is an experimental bot for the online MMORPG known as RuneScape (Old school version). I don't own RuneScape, it's made by Jagex, I'm not affiliated with Jagex and all that jazz. Presumably the bot is long outdated by now, as it has been about a long time since it was "completed", and the game has had time to change so most of my hooks would fail.

The game client is updated and re-obfuscated about every week or two, and since the bot works by both reading
and altering the game code it also needs to be updated every week. This is mostly handled by a separate updater. 
The updater is able to identify about a 100 of different fields in the game such as information about the visible players,
monsters and the landscape. It exports this information in a serialized format which the bot then utilizes.

The bot loads the game jar using a custom classloader and injects up-to-date field getters and method callbacks into the game classes.

While a conventional bot simply reads information, I took a bit more intrusive approach and had the bot inject
callbacks for every significant game event so that scripts for the bot could be written in a more streamlined,
event-driven fashion. I also made the bot allow a script to determine which type of game content spawns would
be cached, so for example when a script would have to do a search for the closest tree, it would only have to
sort through trees instead of every type of object that is loaded (big difference). Most bot developers have to create their own
walking methods (usually utilizing A*) to navigate the terrain, but in that scenario there will
be pathfinding calculations used to first create the desired path, and then by the game itself as it has to maneuver
the character tile by tile. This is quite silly, so I decided to just call the game's walking method directly, resulting
in just one round of calculations.

CPU usage is a hot topic among gold farmers in the game, so I made the bot handle all
interaction with the game by directly using the streams the game uses to communicate with the server. This allowed me to
turn off all graphics and hitbox calculations on demand since mouse interaction was not needed. If this was a commercial bot, Jagex
would react to begin detecting such behavior quickly. Until then, you can do some fun stuff, like decant an entire inventory of vials in the same time it would normally take to combine two vials into one.

The project is rather large for aimlessly trying to find interesting bits, so below I have listed some features/places you might want to look at!

**Note:** In general, looking into and beyond **org/spongebot/loader/updater/** is likely to be a bad time if you don't have experience writing an updater for an RS bot. None of the code is meaningful without blowing up the client bytecode right next to it. **org/spongebot/bot/** on the other hand is safe! :)

[org/spongebot/loader/updater/spawns/NPCSpawnInjector](https://bitbucket.org/Kneesause/spongebot/src/07f5ab95ca47fe5d8fb2b814ebf342589bf33465/src/org/spongebot/loader/updater/spawns/NPCSpawnInjector.java?at=master) - Locating the point in the game code where an NPC spawn request is sent
from the server and added to the game. The program injects a callback so that it is notified when such an event occurs.

[org/spongebot/bot/rs/spawncontrol/NPCSpawn](https://bitbucket.org/Kneesause/spongebot/src/07f5ab95ca47fe5d8fb2b814ebf342589bf33465/src/org/spongebot/bot/rs/spawncontrol/NPCSpawn.java?at=master) - The static method in this class is called by the injected callback that is
described above. The method checks if the program is currently running an user provided script, if so, notify the
running script of an NPC spawn if it implements the interface that indicates a need to be notified.

[org/spongebot/loader/updater/login/LoginHook](https://bitbucket.org/Kneesause/spongebot/src/07f5ab95ca47fe5d8fb2b814ebf342589bf33465/src/org/spongebot/loader/updater/login/LoginHook.java?at=master) - Identification of the method in the game code that logs a character into the game;
creation of a wrapper method callable by the program.

[org/spongebot/bot/script/bundled/TerribleMiner](https://bitbucket.org/Kneesause/spongebot/src/07f5ab95ca47fe5d8fb2b814ebf342589bf33465/src/org/spongebot/bot/script/bundled/TerribleMiner.java?at=master) - A small sample script that utilizes game object and inventory callbacks.

[org/spongebot/loader/RSClassLoader](https://bitbucket.org/Kneesause/spongebot/src/07f5ab95ca47fe5d8fb2b814ebf342589bf33465/src/org/spongebot/loader/RSClassLoader.java?at=master)  - bytecode transformations of the game code. You will see some bits of code commented
out as this source version is from an arbitrary point in time in the middle of development, and at that time those features
needed updating (naturally the game developers actively try to break such programs).