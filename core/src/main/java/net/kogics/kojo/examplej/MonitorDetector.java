package net.kogics.kojo.examplej;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Graphics.Monitor;
import com.badlogic.gdx.Gdx;

public class MonitorDetector extends ApplicationAdapter {
    public static void detectMonitors() {
        // Get all available monitors
        Monitor[] monitors = Gdx.graphics.getMonitors();

        System.out.println("Found " + monitors.length + " monitor(s):");

        // Iterate through each monitor
        for (int i = 0; i < monitors.length; i++) {
            Monitor monitor = monitors[i];

            // Get the monitor's name
            String name = monitor.name;

            // Get the monitor's current display mode
            DisplayMode currentMode = Gdx.graphics.getDisplayMode(monitor);

            // Get all supported display modes
            DisplayMode[] displayModes = Gdx.graphics.getDisplayModes(monitor);

            System.out.println("\nMonitor " + i + ": " + name);
            System.out.println("Current mode: " +
                    currentMode.width + "x" + currentMode.height +
                    ", " + currentMode.refreshRate + "Hz, " +
                    currentMode.bitsPerPixel + " bits per pixel");

            System.out.println("Supported modes:");
            for (DisplayMode mode : displayModes) {
                System.out.println("\t" + mode.width + "x" + mode.height +
                        ", " + mode.refreshRate + "Hz, " +
                        mode.bitsPerPixel + " bits per pixel");
            }
        }
    }

    public static Monitor getPrimaryMonitor() {
        return Gdx.graphics.getPrimaryMonitor();
    }

    @Override
    public void create() {
        detectMonitors();
    }
}