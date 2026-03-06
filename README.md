# AmavyaDecoration

AmavyaDecoration is a plugin for Minecraft servers running [Paper](https://papermc.io/).

The aim of the plugin is to provide additional ways to decorate your Minecraft worlds and builds, as well as add some quality of life features.

# Installation

Simply download the latest jar file and place it in your plugins folder.
If you want to disable certain features, edit the config.yml file that is generated when you start the server.
After saving the config file, run `/adreload` in the server console to reload the config.

# Features

Each feature below can be enabled / disabled in the config.yml file

### Slab Shelves

Slab shelves allow you to place items on slabs to display them.
To create a slab shelf, simply right click on a slab twice while holding a stick.

Afterwards, you can then place items on the shelf by right clicking with an item in your hand. Each shelf can hold four items.

To remove an item from the shelf, crouch and right click the corner of the shelf.

You can rotate items individually on the shelf by right clicking them.

![Slab Shelves Image](/assets/slab_shelves.png)

### Chiseled Bookshelf Title Display

When this feature is enabled, the titles of books inside Chiseled bookshelves will be displayed when you get close.

Enchanted books will also show the enchantments on them.

You can adjust the radius from the player at which the titles will display in config.yml

![Chiseled Bookshelves Image](/assets/chiseled_bookshelves.png)

# Permissions

The only permission is `amavyadecoration.reload` which gives access to the `/adreload` command to reload the config.yml file.