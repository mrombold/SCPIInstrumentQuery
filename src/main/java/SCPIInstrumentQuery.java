import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.concurrent.*;


//NOTES:  To compile, do Maven Clean, then Maven Build with "Goals" of "clean package".
// This will compile the full code as a JAR in the ./target directory

public class SCPIInstrumentQuery {
    private static final String scope =     "10.11.13.220"; // Instrument IP addresses
    private static final String dmm =       "10.11.13.221";
    private static final String spectrum =  "10.11.13.222";
    private static final String signal =    "10.11.13.223";
    private static final String power =     "10.11.13.224";
    private static final int PORT =         5025;           // Standard SCPI port
    private static final String DB_URL =    "jdbc:sqlite:measurements.db";
    

    public static void main(String[] args) {
        setupDatabase();
        setupInstrument();
        
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(SCPIInstrumentQuery::performMeasurement, 0, 10, TimeUnit.SECONDS);
    }
    
    
    

    private static void setupDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            // Create table if it doesn't exist
            String createTableSQL = "CREATE TABLE IF NOT EXISTS measurements (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "value TEXT NOT NULL," +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ")";
            stmt.execute(createTableSQL);

            System.out.println("Database and table setup completed.");

        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    private static void setupInstrument() {
        try (Socket dmmSocket = new Socket(dmm, PORT);
                PrintWriter out = new PrintWriter(dmmSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(dmmSocket.getInputStream()))) {
             
               out.println("CONF:TEMP THER,KITS90");    //Configure to measure K type thermocouple
               out.println("UNIT:TEMP F");    //Configure to measure K type thermocouple
                              
           } catch (Exception e) {
               e.printStackTrace();
           }
    }
    
    private static void performMeasurement() {
        try (Socket socket = new Socket(dmm, PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Connection conn = DriverManager.getConnection(DB_URL)) {

            // Send SCPI command
            out.println("READ?");

            // Read response
            String response = in.readLine();

            // Save to database
            String sql = "INSERT INTO measurements (value) VALUES (?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, response);
                pstmt.executeUpdate();
            }

            System.out.println("Measurement taken and saved: " + response);

        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
}