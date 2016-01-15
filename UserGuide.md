

# 1.) Selecting a process #

At the top of the window, the **Process:** combobox lists the currently running applications, if an application was launched after the memory editor, use the **Refresh** to update the list. (Some exotic applications may not appear in this list, but in my experience, most games and programs do.)

The bottom statusbar displays an error text if something failed. The success is indicated by a code **0 -**.

# 2.) Memory layout #

In experiments with Windows XP, I found out that some larger memory addresses should be avoided completely as they cause BSOD or simply hang the computer. Note that, some (older) games are using so-called guard pages, which when read or written, cause a CPU protection fault and terminate the game to avoid cheating.

The **Address start** and **Address end** optional fields may limit the search area. Use hexadecimal 32 bit numbers between 00000000 and 7FFFFFFF. If left empty, the default range is also 00000000 and 7FFFFFFF.

The left side lists the memory layout of the application. Large, multi MB memory spaces, may be the areas where the program keeps its variables.

# 3.) Enter values to search for #

The **Value start** and **Value end** allows the user to search for values in that range. Some games store the health in floating point number and display only the whole part: HP of 89 might be stored as 89.246. Enter 88 and 91 to look for this value. It is generally advised to have a value range a bit larger than the game value: the game might round it up or down and you may just miss it by few fractions.

**Note:** If you enter only the **Value start** field and leave the other empty, that counts for an exact search. E.g., you see you have 239 gold then search for 239 exactly.

Select the **Value type** which the program may store the value. Generally, using Int32 and Float does the trick.

The **Result limit** may be used to limit the number of found addresses. In case you look for a broader range or your value is just too general, e.g., looking for an ammo value of 1 might list over a million memory addresses.

# 4.) Find New, Find and Filter #

Clicking the **Find New** button win start a new search with the parameters above. You should start with this button.

The **Find** button looks for the value (or range) within the results of a previous find, but without looking at the memory, e.g., you may eliminate some unnecessary results this way. The common usage is to **Find New** some value, let the game progress, then click **Filter** with a **Changed** option. Some memory location might go way outside the expected range (e.g, turn into -1000000). Clicking on **Find** will eliminate these unwanted values.

The **Filter** button compares the values of the last search with the values currently in memory via the specified filtering method. This may be used to zero in on a specific (set) of memory by letting the game progress and looking for what has (not) changed.

For example, you want to find the HP of your character and you see 90 on screen. You search for 88 to 92 and get 200 results with lots of fractional values between the two. Any of it could be the HP. Return to the game and let's take some damage! After that, return to the memory editor and select a filter option:

  * **Changed**: the memory addresses to keep had changed since last seen
  * **Unchanged**: the memory address to keep must remain the same
  * **Increased**: the value in the memory address should have increased (e.g, more gold, health regenerated, etc.)
  * **Decreased**: the value in the memory address should have decreased (e.g., gold spent, damaged, ammo fired, etc.)

In our example, lets choose **Decreased**, this should eliminate a lot of addresses. If you look at the game and see, the value is now 81, then you can enter 80 and 83 into the value box and hit **Find** this will hopefully eliminate any addresses with too large changes.

Repeat this process until you find one or a few memory addresses (with the exact same values) and you nailed it.

Note that some game use multiple memory locations to store values to detect cheating or corruption, you'll need to have all of these locations changed together before returning into the game.

The **History** combobox memorizes the search and filter results you performed and lets you track back into an earlier snapshot in case you accidentally filtered the wrong values or the results ended up empty. Histories may get quite big so the **Clear** button will empty the history.

# 5.) The results #

The middle list contains the search results you may want to observe and get hints about where and what to search next.

Double clicking on a line will transfer its values to the **Address** and **Value** below.

The list features a popup menu with the following options:

  * **Use item**: same as double clicking on a line, sets its parameters in the editors below.
  * **Re-read item(s)**: re-read the values of the selected items only.
  * **Remove items(s)**: remove the selected items from the list
  * **Retain items(s)**: keep the selected items and remove everything else.

# 6.) Changing a memory address #

The **Address** and **Value** fields at the bottom lets you read or change a memory value. If you found an interesting memory address, use **Read** to confirm its value, the enter a new one and hit **Write**. Return to the game and look for the changes. Some games might not immediately show the new value, you may need to enter into menu or switch screens to see the difference. Some games may require you to write multiple memory addresses in order to affect the target property.

# 7.) Holding values #

Resetting your HP too often becomes tedious. The hold value may help you in this, which periodically checks for the memory location and adjusts it if it has deviated from the target value.

The **Hold value** combobox contains three options:

  * **Exactly**: whenever the value changes, just reset it to the predefined value.
  * **At least**: if the value goes below the specified, reset it to the specified. I usually set this for properties like HP: they may decrease but will be stopped if it gets too low: avoids dying but you still may apply healing and potions as you like.
  * **At most**: if the value goes above the specified, reset it to the specified.

The **Cycle time** will set how often the held value needs to be checked in 10s of milliseconds. Setting it to high might miss a drastic change (e.g., you die), setting it to low might get your application less smooth. 10 x 10 ms is a good value.

Click on the **Hold** button to start holding the value. It will be shown in the right listings (beside the results).

The hold list features a popup menu as well with the following options:

  * **Use**: set the address in the editors below, same as double clicking.
  * **Enable**: allow holding the value.
  * **Disable**: temporarily disable the hold value. Some games may require this, e.g., when your death is scripted. In this case, the value gets reset and the game breaks and won't continue properly.
  * **Remove item(s)**: stop holding the selected values.
  * **Retain item(s)**: keep only the selected hold items, and stop holding the rest.