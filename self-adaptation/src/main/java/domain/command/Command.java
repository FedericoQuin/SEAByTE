package domain.command;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;

public abstract class Command {
    public abstract Optional<String> execute();


    public static String executeCommand(String command) throws IOException, InterruptedException {
        return Command.executeCommand(command, null);
    }

    public static String executeCommand(String command, File dir) throws IOException, InterruptedException {
        String resultString = "";

        var result = Runtime.getRuntime().exec(command, null, dir);
        BufferedReader reader = new BufferedReader(new InputStreamReader(result.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            resultString += line + '\n';
        }
        result.waitFor();

        System.out.println(resultString);
        return resultString;
    }
}
