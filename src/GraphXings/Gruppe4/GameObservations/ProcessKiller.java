package GraphXings.Gruppe4.GameObservations;

import java.io.IOException;

public class ProcessKiller {

    // MacOS and Linux should work roughly the same. Therefore check for Windows.
    private final boolean isWindows;

    public ProcessKiller() {
        String osName = System.getProperty("os.name");

        isWindows = osName.contains("Windows");
    }

    public boolean isWindows() {
        return isWindows;
    }

    public boolean killPythonProcess() {
        ProcessBuilder pb = new ProcessBuilder();
        if (isWindows) {
            // Windows strategy
            pb.command("Taskkill /IM python.exe /F; Taskkill /IM python3.exe /F");
        } else {
            // Linux/MacOS strategy
            pb.command("killall python; killall python3");
        }

        // Start killing
        try {
            pb.start();
        } catch (IOException e) {
            // Didn't work or no Python process exists.
            return false;
        }
        return true;
    }
}
