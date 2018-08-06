# cubizer-project
A Java utility for creating 3D models out of cubes, and using them to create 2D sprite animations from different angles for games. Please note that I did not use any existing 3D library or toolkit to render the 3D objects; I developed and rendered everything using 2D drawings from scratch.

This is a complete Java project which can be opened in the latest NetBeans IDE. It can also be run by executing 'Cubizer.jar' in the 'dist' directory.

Controls:
Use left/right arrow keys to move cube selector on the X axis; up/down arrow keys to move on the Y axis; +/- keys to move on the Z axis (requires NumPad).

The mouse can be used to move or rotate either the current model or the entire scene.
Holding the left mouse button to drag will MOVE; holding the right mouse button to drag will ROTATE.
Holding Ctrl while dragging the mouse will apply the move or rotate operation to the entire scene; holding Alt while dragging the mouse will apply the move or rotate operation to the current model only.

Either Ctrl or Alt must be held to move or rotate. Simply dragging the mouse by itself will not do anything.

Drag mouse right/left to move or rotate on Y axis; up/down to move or rotate on X axis; up-left/down-right to move or rotate on Z axis. You can only move or rotate on one axis at a time.

Press spacebar to place a cube at the current location.

There are so many controls and features of this program which I do not have time to document right now. Please experiment to figure it out!

Notes:
- Try the "Load 3D Model (+submodels)" function from the File menu, and use the dialog to open "adam_chest.czmod" from the 'models' directory. Then use "Animation -> Load Animation File" to open the "adam.czani" animation from the "anims" folder. Now, in the program, change to the "Animations" tab, and click on the different directions/poses...
