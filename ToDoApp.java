import java.io.*;
import java.util.*;

/**
 * Simple CLI To-Do application that persists tasks to tasks.txt
 * Usage:
 *   java ToDoApp
 *
 * Commands:
 *   list                 - show all tasks
 *   add <task description> - add a new task
 *   done <task number>   - mark a task as done
 *   remove <task number> - remove a task
 *   help                 - show commands
 *   exit                 - save and exit
 */
public class ToDoApp {
    private static final String DATA_FILE = "tasks.txt";

    static class Task {
        boolean done;
        String text;

        Task(boolean done, String text) {
            this.done = done;
            this.text = text;
        }

        @Override
        public String toString() {
            return (done ? "[x] " : "[ ] ") + text;
        }

        // line format for persistence: 0|task text  or 1|task text
        String serialize() {
            return (done ? "1" : "0") + "|" + text.replace("\n", " ");
        }

        static Task deserialize(String line) {
            String[] parts = line.split("\\|", 2);
            if (parts.length < 2) return new Task(false, line);
            boolean done = "1".equals(parts[0]);
            return new Task(done, parts[1]);
        }
    }

    private final List<Task> tasks = new ArrayList<>();

    public static void main(String[] args) {
        ToDoApp app = new ToDoApp();
        app.load();
        System.out.println("Welcome to ToDo CLI. Type 'help' for commands.");
        app.run();
        app.save();
        System.out.println("Goodbye!");
    }

    private void run() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            String line = sc.nextLine().trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("\\s+", 2);
            String cmd = parts[0].toLowerCase();
            String arg = parts.length > 1 ? parts[1].trim() : "";

            switch (cmd) {
                case "list":
                    cmdList();
                    break;
                case "add":
                    cmdAdd(arg);
                    break;
                case "done":
                    cmdDone(arg);
                    break;
                case "remove":
                    cmdRemove(arg);
                    break;
                case "help":
                    printHelp();
                    break;
                case "exit":
                    sc.close();
                    return;
                default:
                    System.out.println("Unknown command. Type 'help' to see commands.");
            }
        }
    }

    private void cmdList() {
        if (tasks.isEmpty()) {
            System.out.println("No tasks. Add one with: add Buy groceries");
            return;
        }
        for (int i = 0; i < tasks.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, tasks.get(i).toString());
        }
    }

    private void cmdAdd(String text) {
        if (text.isEmpty()) {
            System.out.println("Usage: add <task description>");
            return;
        }
        tasks.add(new Task(false, text));
        System.out.println("Added: " + text);
    }

    private void cmdDone(String arg) {
        Integer idx = parseIndex(arg);
        if (idx == null) return;
        Task t = tasks.get(idx);
        t.done = true;
        System.out.println("Marked done: " + t.text);
    }

    private void cmdRemove(String arg) {
        Integer idx = parseIndex(arg);
        if (idx == null) return;
        Task removed = tasks.remove((int) idx);
        System.out.println("Removed: " + removed.text);
    }

    private Integer parseIndex(String arg) {
        if (arg.isEmpty()) {
            System.out.println("Usage: <command> <task number>");
            return null;
        }
        try {
            int n = Integer.parseInt(arg);
            if (n < 1 || n > tasks.size()) {
                System.out.println("Invalid task number.");
                return null;
            }
            return n - 1;
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format.");
            return null;
        }
    }

    private void printHelp() {
        System.out.println("Commands:");
        System.out.println("  list                 - show all tasks");
        System.out.println("  add <desc>           - add new task");
        System.out.println("  done <num>           - mark task as done");
        System.out.println("  remove <num>         - remove task");
        System.out.println("  help                 - show this help");
        System.out.println("  exit                 - save and exit");
    }

    private void load() {
        File f = new File(DATA_FILE);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                tasks.add(Task.deserialize(line));
            }
        } catch (IOException e) {
            System.out.println("Failed to load tasks: " + e.getMessage());
        }
    }

    private void save() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(DATA_FILE))) {
            for (Task t : tasks) pw.println(t.serialize());
        } catch (IOException e) {
            System.out.println("Failed to save tasks: " + e.getMessage());
        }
    }
}
