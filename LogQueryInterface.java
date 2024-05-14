import java.io.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class Log {
    private String level;
    private String logString;
    private String timestamp;
    private String source;

    public Log(String level, String logString, String source) {
        this.level = level;
        this.logString = logString;
        this.source = source;
        this.timestamp = getCurrentTimestamp();
    }

    private String getCurrentTimestamp() {
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        return currentTime.format(formatter);
    }

    public String getLevel() {
        return level;
    }

    public String getLogString() {
        return logString;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getSource() {
        return source;
    }

    @Override
    public String toString() {
        return "Log{" +
                "level='" + level + '\'' +
                ", logString='" + logString + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", source='" + source + '\'' +
                '}';
    }
}

class LogStorage {
    private List<Log> logs;
    private Properties config;

    public LogStorage() {
        this.logs = new ArrayList<>();
        this.config = loadConfig();
    }

    private Properties loadConfig() {
        Properties properties = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("log.properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
            } else {
                System.err.println("Failed to load log configuration file.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    public void addLog(Log log) {
        logs.add(log);
    }

    public void log(String api, String logString) {
        String level = config.getProperty(api + ".level", "info");
        String filepath = config.getProperty(api + ".filepath", "default.log");
        Log log = new Log(level, logString, filepath);
        addLog(log);
        try {
            writeToFile(filepath, log);
        } catch (IOException e) {
            System.err.println("Failed to write log to file: " + e.getMessage());
        }
    }

    private void writeToFile(String filepath, Log log) throws IOException {
        try (FileWriter writer = new FileWriter(filepath, true)) {
            writer.write(log.getLevel() + " - " + log.getLogString() + " - " + log.getTimestamp() + "\n");
        }
    }

    public List<Log> getLogs() {
        return logs;
    }
}

public class LogQueryInterface {
    private final LogStorage logStorage;

    public LogQueryInterface(LogStorage logStorage) {
        this.logStorage = logStorage;
    }

    public void log(String api, String logString) {
        logStorage.log(api, logString);
    }

    public List<Log> queryLogs(String level, String logString, String timestamp, String source) {
        List<Log> result = new ArrayList<>();

        for (Log log : logStorage.getLogs()) {
            if ((level == null || log.getLevel().equals(level)) &&
                    (logString == null || log.getLogString().contains(logString)) &&
                    (timestamp == null || log.getTimestamp().equals(timestamp)) &&
                    (source == null || log.getSource().equals(source))) {
                result.add(log);
            }
        }

        return result;
    }

    public static void main(String[] args) {
        // Sample usage
        LogStorage logStorage = new LogStorage();
        LogQueryInterface logQueryInterface = new LogQueryInterface(logStorage);

        // Log messages from different APIs
        logQueryInterface.log("api1", "Inside the Search API from API1");
        logQueryInterface.log("api2", "Query executed successfully from API2");
        logQueryInterface.log("api3", "Database connection failed from API3");
        logQueryInterface.log("api4", "Invalid request received from API4");
        logQueryInterface.log("api5", "Data processing completed from API5");
        logQueryInterface.log("api6", "User logged in successfully from API6");
        logQueryInterface.log("api7", "File uploaded successfully from API7");
        logQueryInterface.log("api8", "Payment processed successfully from API8");
        logQueryInterface.log("api9", "Email sent successfully from API9");

        Scanner sc = new Scanner(System.in);
        System.out.println("Enter the level");
        String level = sc.nextLine();
        System.out.println("Enter the source");
        String src = sc.nextLine();

        // Query logs
        List<Log> queriedLogs = logQueryInterface.queryLogs(level, null, null, src);
        if (queriedLogs.isEmpty()) {
            System.err.println("API log Data not found");
        } else {
            for (Log log : queriedLogs) {
                System.out.println();
                System.out.println(log);
            }
        }
    }
}
