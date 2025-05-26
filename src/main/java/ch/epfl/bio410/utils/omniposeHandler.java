package ch.epfl.bio410.utils;
import ij.IJ;
import net.imagej.ImageJ;
import org.scijava.command.Command;
import org.scijava.plugin.Plugin;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class omniposeHandler {
    public static void runOmnipose() throws IOException {
        // OPTIONAL : copy the python file  in a 'real' location, on the computer, from the resources folder
        // It is also possible to simply hard-code the path to the existing python code
        // BE CAREFUL : IN THAT CASE, YOU NEED TO COMMENT THE FILE DELETION AT THE END.
        File outputFile = File.createTempFile("test","test");// = ResourcesFolder.copyFileFromResources("HelloWorld.py");
        if(outputFile != null && outputFile.exists()) {
            // path to the python script .py
            String pythonExecPath = outputFile.getAbsolutePath();
            // path to the environment where the python script need to be executed
            String pythonEnvPath = "C:\\Users\\dornier\\Miniconda3\\envs\\bio410pythonjava2";
            // python version installed in the environment
            String pythonVersion = "3.9";
            // depending on the OS the location of the .exe python path has to be adapted
            String os = System.getProperty("os.name");
            if (os.toLowerCase().startsWith("w"))
                pythonEnvPath = pythonEnvPath + File.separator + "python"; // may need python.exe in some cases
            else
                pythonEnvPath = pythonEnvPath + File.separator + "bin" + File.separator + "python" + pythonVersion; // may need python.exe in some cases
            // list of commands to execute.
            // if the script has arguments, add new entries to the list (one entry = one argument)
            List<String> commandsList = new ArrayList<>();
            commandsList.add(pythonEnvPath);
            commandsList.add(pythonExecPath);
            try {
                // starts the execution of the script
                Process p = Runtime.getRuntime().exec(String.join(" ", commandsList));
                // One way to read the output
                // Read output directly using InputStreamReader
                InputStreamReader reader = new InputStreamReader(p.getInputStream());
                BufferedReader buffer = new BufferedReader(reader);
                String line;
                while ((line = buffer.readLine()) != null) {
                    IJ.log(line);
                }
                buffer.close();
                reader.close();
                // Capture error output
                BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                String error;
                while ((error = stdError.readLine()) != null) {
                    IJ.log("Python Error: " + error);
                }
                // Wait for process to complete
                int exitCode = p.waitFor();
                if (exitCode == 0)
                    IJ.log("Python script executed successfully.");
                else
                    IJ.log("Error executing Python script. Exit code: " + exitCode);
            } catch (Exception /*| IOException | InterruptedException */e) {
                IJ.log("ERROR -- Something went wrong during python execution");
                IJ.log(Arrays.toString(e.getStackTrace()));
            }
            // delete the temporary copy of the file
            // COMMENT THESE LINES IF YOU DON'T WANT TO DELETE THE FILE BECAUSE YOU DON'T HAVE IT IN THE RESOURCES FOLDER
            if (outputFile.exists()) {
                if (!outputFile.delete())
                    IJ.log("ERROR -- Temporary file has not been deleted");
            }
        }
    }


}


