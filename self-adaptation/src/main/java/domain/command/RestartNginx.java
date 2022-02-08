package domain.command;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;


// TODO remove this command all-together?
public class RestartNginx extends Command {
    private static String COMMAND_TEMPLATE = "ansible %s -i hosts.ini -a \"/etc/init.d/nginx reload\" -u %s --become --extra-vars \"ansible_password=%s ansible_sudo_pass=%s\"";
    private static String ANSIBLE_PATH = "/path/to/ansible/files/";

    private String ipAddress;
    private String user;
    private String password;

    private Logger logger = Logger.getLogger(RestartNginx.class.getName());

    public RestartNginx(String ipAddress, String user, String password) { 
        this.ipAddress = ipAddress;
        this.user = user;
        this.password = password;
    }

    public void execute() {
        // ansible-playbook nginx_restart.yml --extra-vars "user=%s host=%s"
        try {
            File file = new File(String.format("%s/hosts.ini", ANSIBLE_PATH));

            if (file.exists()) {
                file.delete();
            }

            file.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write("[host]\n");
            writer.write(String.format("%s ansible_connections=ssh ansible_user=%s ansible_password=%s\n", 
                this.ipAddress, this.user, this.password));

            writer.close();

            // TODO add wait-for-it such that other services are reachable before rebooting nginx
            Command.executeCommand("sleep 50");

            String command = String.format(COMMAND_TEMPLATE, "host", this.user, this.password, this.password, this.password);
            logger.info(command);
            var result = Command.executeCommand(command, new File(ANSIBLE_PATH));
            logger.info(result);
            
        } catch (IOException | InterruptedException e) {
            logger.severe(e.getMessage());
        }
    }
}
