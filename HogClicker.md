# Introduction #

Basically an automated clicker program to play hidden object games where you take a screenshot, select the points you would like to click in a row,
then executing the clicks by sending mouse events to the original game window. This way, you can finish a screen faster, achieve bonuses for successful subsequent clicks, etc. Note that some clicking may trigger a different game state (e.g. a popup, storytelling, reward) which breaks the click sequence. You have to re-capture the frame, delete objects already clicked then re-run for the rest.

You should run the program in administrator privileges.

Best option is to run your HOG game in windowed mode and start the Windows' **Magnifier** tool. Zooming in is one of the best option for these games.

# Usage #

  1. Select a running process
  1. Click on the Capture button
    * If there is more than one Game window, a menu will pop up
    * If the game is minimized or uses some special overlay, the screen capture might fail
  1. Left click on the captured image to add a point
  1. Hold SHIFT and click on an existing point to remove it
  1. You can re-order the points listed in the left, remove or clear them
  1. You can change the click-indicator box color and size
  1. Adjust the delay between clicks
  1. Click on the left zoom window to open a popup window which you can resize and magnify as you see fit.
  1. Once finished, click the **Execute** button

When you click the **Execute** button, the game window will pop up after the specified delay, and after waiting again, the clicking is emulated. You may let go your mouse to let the program do its job.

# Screenshot #

![http://jmemoryeditorw.googlecode.com/svn/trunk/JMemEditorW/doc/HogClicker-0.1.png](http://jmemoryeditorw.googlecode.com/svn/trunk/JMemEditorW/doc/HogClicker-0.1.png)