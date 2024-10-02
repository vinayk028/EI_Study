import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

interface TaskObserver {
    void onTaskAdded(Task task);
    void onTaskRemoved(Task task);
    void onTaskUpdated(Task oldTask, Task newTask);
}

class ConflictDetectionObserver implements TaskObserver {
    private static final Logger LOGGER = Logger.getLogger(ConflictDetectionObserver.class.getName());

    @Override
    public void onTaskAdded(Task task) {
        LOGGER.info("New task added: " + task.getDescription());
    }

    @Override
    public void onTaskRemoved(Task task) {
        LOGGER.info("Task removed: " + task.getDescription());
    }

    @Override
    public void onTaskUpdated(Task oldTask, Task newTask) {
        LOGGER.info("Task updated from: " + oldTask.getDescription() + " to: " + newTask.getDescription());
    }
}

interface Command {
    void execute();
    void undo();
}

class AddTaskCommand implements Command {
    private final ScheduleManager scheduleManager;
    private final Task task;

    public AddTaskCommand(ScheduleManager scheduleManager, Task task) {
        this.scheduleManager = scheduleManager;
        this.task = task;
    }

    @Override
    public void execute() {
        scheduleManager.addTaskInternal(task);
    }

    @Override
    public void undo() {
        scheduleManager.removeTaskInternal(task);
    }
}

class RemoveTaskCommand implements Command {
    private final ScheduleManager scheduleManager;
    private final Task task;

    public RemoveTaskCommand(ScheduleManager scheduleManager, Task task) {
        this.scheduleManager = scheduleManager;
        this.task = task;
    }

    @Override
    public void execute() {
        scheduleManager.removeTaskInternal(task);
    }

    @Override
    public void undo() {
        scheduleManager.addTaskInternal(task);
    }
}

class ScheduleManager {
    private static final ScheduleManager INSTANCE = new ScheduleManager();
    private final Map<String, Task> tasks = new ConcurrentHashMap<>();
    private final List<TaskObserver> observers = new ArrayList<>();
    private static final Logger LOGGER = Logger.getLogger(ScheduleManager.class.getName());

    private ScheduleManager() {}

    public static ScheduleManager getInstance() {
        return INSTANCE;
    }

    public void addObserver(TaskObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(TaskObserver observer) {
        observers.remove(observer);
    }

    void addTaskInternal(Task task) {
        tasks.put(task.getId(), task);
        notifyObservers(task, null, ChangeType.ADDED);
    }

    void removeTaskInternal(Task task) {
        tasks.remove(task.getId());
        notifyObservers(task, null, ChangeType.REMOVED);
    }

    private void notifyObservers(Task task, Task oldTask, ChangeType changeType) {
        for (TaskObserver observer : observers) {
            switch (changeType) {
                case ADDED -> observer.onTaskAdded(task);
                case REMOVED -> observer.onTaskRemoved(task);
                case UPDATED -> observer.onTaskUpdated(oldTask, task);
            }
        }
    }

    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    public boolean hasConflictingTask(Task newTask) {
        for (Task existingTask : tasks.values()) {
            if (isTimeOverlap(existingTask, newTask)) {
                return true;
            }
        }
        return false;
    }

    private boolean isTimeOverlap(Task existingTask, Task newTask) {
        return newTask.getStartTime().isBefore(existingTask.getEndTime())
                && newTask.getEndTime().isAfter(existingTask.getStartTime());
    }
}

class TaskFactory {
    public static Task createTask(String description, String startTime, String endTime, String priority) throws DateTimeParseException {
        return new TaskBuilder()
                .setDescription(description)
                .setStartTime(LocalTime.parse(startTime))
                .setEndTime(LocalTime.parse(endTime))
                .setPriority(Priority.valueOf(priority.toUpperCase()))
                .build();
    }
}

enum Priority {
    HIGH, MEDIUM, LOW
}

enum ChangeType {
    ADDED, REMOVED, UPDATED
}

interface Task {
    String getId();
    String getDescription();
    LocalTime getStartTime();
    LocalTime getEndTime();
    Priority getPriority();
}

class BasicTask implements Task {
    private final String id;
    private final String description;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final Priority priority;

    public BasicTask(TaskBuilder builder) {
        this.id = UUID.randomUUID().toString();
        this.description = builder.description;
        this.startTime = builder.startTime;
        this.endTime = builder.endTime;
        this.priority = builder.priority;
    }

    @Override public String getId() { return id; }
    @Override public String getDescription() { return description; }
    @Override public LocalTime getStartTime() { return startTime; }
    @Override public LocalTime getEndTime() { return endTime; }
    @Override public Priority getPriority() { return priority; }
}

class TaskBuilder {
    String description;
    LocalTime startTime;
    LocalTime endTime;
    Priority priority;

    public TaskBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public TaskBuilder setStartTime(LocalTime startTime) {
        this.startTime = startTime;
        return this;
    }

    public TaskBuilder setEndTime(LocalTime endTime) {
        this.endTime = endTime;
        return this;
    }

    public TaskBuilder setPriority(Priority priority) {
        this.priority = priority;
        return this;
    }

    public Task build() {
        return new BasicTask(this);
    }
}

public class AstronautSchedulerApp {
    private static final Logger LOGGER = Logger.getLogger(AstronautSchedulerApp.class.getName());
    private final ScheduleManager scheduleManager;
    private final Stack<Command> undoStack = new Stack<>();

    public AstronautSchedulerApp() {
        this.scheduleManager = ScheduleManager.getInstance();
        this.scheduleManager.addObserver(new ConflictDetectionObserver());
    }

    public void addTask(String description, String startTime, String endTime, String priority) {
        try {
            // Check for valid time format
            Task task = TaskFactory.createTask(description, startTime, endTime, priority);

            // Check for task conflict
            if (scheduleManager.hasConflictingTask(task)) {
                LOGGER.severe("Error: Task conflicts with existing task.");
            } else {
                Command addCommand = new AddTaskCommand(scheduleManager, task);
                addCommand.execute();
                undoStack.push(addCommand);
                LOGGER.info("Task added successfully: " + description);
            }
        } catch (DateTimeParseException e) {
            LOGGER.severe("Error: Invalid time format.");
        } catch (Exception e) {
            LOGGER.severe("Failed to add task: " + e.getMessage());
        }
    }

    public void removeTask(String description) {
        Optional<Task> taskToRemove = scheduleManager.getTasks().stream()
                .filter(task -> task.getDescription().equalsIgnoreCase(description))
                .findFirst();

        if (taskToRemove.isPresent()) {
            Task task = taskToRemove.get();
            Command removeCommand = new RemoveTaskCommand(scheduleManager, task);
            removeCommand.execute();
            undoStack.push(removeCommand);
            LOGGER.info("Task removed successfully: " + description);
        } else {
            LOGGER.severe("Error: Task not found - " + description);
        }
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            Command command = undoStack.pop();
            command.undo();
            LOGGER.info("Undone last command");
        }
    }

    public void displaySchedule() {
        List<Task> tasks = scheduleManager.getTasks();

        if (tasks.isEmpty()) {
            System.out.println("No tasks scheduled.");
            return;
        }

        System.out.println(String.format("%-12s %-12s %-12s %-30s %-10s", "Date", "StartTime", "EndTime", "Description", "Priority"));
        System.out.println("---------------------------------------------------------------------------------------------");

        for (Task task : tasks) {
            String date = LocalDate.now().toString();  // Assuming today's date for simplicity
            String startTime = task.getStartTime().toString();
            String endTime = task.getEndTime().toString();
            String description = task.getDescription();
            String priority = task.getPriority().toString();

            System.out.println(String.format("%-12s %-12s %-12s %-30s %-10s", date, startTime, endTime, description, priority));
        }
    }

    public static void main(String[] args) {
        AstronautSchedulerApp app = new AstronautSchedulerApp();
        System.out.println();
        // Test case: adding tasks
        app.addTask("Morning Exercise", "07:00", "08:00", "High");
        app.addTask("Team Meeting", "09:00", "10:00", "Medium");
        System.out.println();
        System.out.println("\nSchedule Initial:");

        app.displaySchedule();
        System.out.println();
        // Removing a task
        app.removeTask("Morning Exercise");
        System.out.println();
        System.out.println("\nSchedule after task removal:");

        app.displaySchedule();
        System.out.println();

        app.addTask("Lunch Break", "12:00", "13:00", "LOW");
        System.out.println();

        System.out.println("\nSchedule after task added:");
        app.displaySchedule();

        System.out.println();
        // Test case: task conflict
        app.addTask("Training Session", "09:30", "10:30", "High");
        System.out.println();
        // Test case: invalid time format
        app.addTask("Invalid Time Task", "25:00", "26:00", "Low");
        System.out.println();
        // Test case: removing task
        app.removeTask("Non-existent Task");

        System.out.println();
        // Displaying schedule
        app.displaySchedule();
    }
}
