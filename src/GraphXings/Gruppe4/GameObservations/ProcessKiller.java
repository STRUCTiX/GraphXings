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
        // Start killing
        try {
            if (isWindows) {
                // Windows strategy
                Process p = Runtime.getRuntime().exec(new String[]{"Taskkill", "/IM", "python.exe", "/F"});
                Process p3 = Runtime.getRuntime().exec(new String[]{"Taskkill", "/IM", "python3.exe", "/F"});
            } else {
                // Linux/MacOS strategy
                Process p = Runtime.getRuntime().exec(new String[]{"pkill", "-9", "python"});
                Process p3 = Runtime.getRuntime().exec(new String[]{"pkill", "-9", "python3"});
            }
        } catch (IOException e) {
            // Didn't work or no Python process exists.
            return false;
        }
        return true;
    }
}
