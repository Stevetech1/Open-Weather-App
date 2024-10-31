
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalTime;

public class WeatherInformationApp extends Application {

    // Define UI components and weather-related fields
    private TextField cityInput;
    private Label weatherInfoLabel;
    private Label temperatureLabel;
    private Label humidityLabel;
    private ImageView weatherIcon;
    private VBox historyBox;

    private static final String API_KEY = "aa5ae935a4f0e3c80fb8424caaaf5ab2";  // Replace with your actual API key

    @Override
    public void start(Stage primaryStage) {

        // Set up input for city name
        cityInput = new TextField();
        cityInput.setPromptText("Enter city name");

        // Create a button to fetch weather data
        Button fetchWeatherButton = new Button("Get Weather");
        fetchWeatherButton.setOnAction(e -> fetchWeatherData());

        // Initialize weather information labels
        weatherInfoLabel = new Label("Weather Information:");
        temperatureLabel = new Label("Temperature:");
        humidityLabel = new Label("Humidity:");

        // Set font for clarity and size consistency
        weatherInfoLabel.setFont(new Font("Arial", 16));
        temperatureLabel.setFont(new Font("Arial", 14));
        humidityLabel.setFont(new Font("Arial", 14));

        // ImageView to hold the weather icon
        weatherIcon = new ImageView();
        weatherIcon.setFitHeight(50);
        weatherIcon.setFitWidth(50);

        // History section to track recent searches
        historyBox = new VBox(5);
        historyBox.setAlignment(Pos.TOP_LEFT);

        // Layout for weather information display
        VBox weatherBox = new VBox(10, weatherInfoLabel, temperatureLabel, humidityLabel, weatherIcon);

        // Main container for the GUI
        VBox root = new VBox(20, cityInput, fetchWeatherButton, weatherBox, new Label("Search History:"), historyBox);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: #ADD8E6;");  // Light blue background as default

        // Set scene and stage properties
        Scene scene = new Scene(root, 300, 400);
        primaryStage.setTitle("Weather Information App");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Fetch weather data from the OpenWeatherMap API based on the city input.
     */
    private void fetchWeatherData() {
        String city = cityInput.getText().trim();

        // Validate the city name input
        if (city.isEmpty()) {
            showErrorAlert("City name cannot be empty. Please enter a valid city name.");
            return;
        }

        try {
            // Build URL for API request with city and API key
            String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + API_KEY + "&units=metric";
            HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
            connection.setRequestMethod("GET");

            // Check if the response is OK
            if (connection.getResponseCode() == 200) {
                // Parse JSON response
                InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                StringBuilder jsonResponse = new StringBuilder();
                int read;
                while ((read = reader.read()) != -1) {
                    jsonResponse.append((char) read);
                }

                JSONObject jsonObject = new JSONObject(jsonResponse.toString());
                updateWeatherInfo(jsonObject);

                // Record the search history
                addToHistory(city);

            } else {
                // Show error if API fails
                showErrorAlert("Could not retrieve data for " + city + ". Please check the city name.");
            }

            connection.disconnect();

        } catch (Exception e) {
            showErrorAlert("Failed to retrieve weather data. Please try again later.");
            e.printStackTrace();
        }
    }

    /**
     * Updates the GUI with the retrieved weather data.
     *
     * @param jsonObject JSON object containing weather data.
     */
    private void updateWeatherInfo(JSONObject jsonObject) {
        // Parse weather data from JSON response
        String cityName = jsonObject.getString("name");
        double temperature = jsonObject.getJSONObject("main").getDouble("temp");
        int humidity = jsonObject.getJSONObject("main").getInt("humidity");
        String iconCode = jsonObject.getJSONArray("weather").getJSONObject(0).getString("icon");

        // Update labels and icon in the GUI
        weatherInfoLabel.setText("Weather in " + cityName);
        temperatureLabel.setText("Temperature: " + temperature + " °C");
        humidityLabel.setText("Humidity: " + humidity + "%");

        // Display the weather icon using the icon code from the API
        String iconUrl = "http://openweathermap.org/img/wn/" + iconCode + "@2x.png";
        weatherIcon.setImage(new Image(iconUrl));

        // Change background color based on the time of day
        setDynamicBackground();
    }

    /**
     * Adds a city search to the history section.
     *
     * @param city Name of the city.
     */
    private void addToHistory(String city) {
        Label historyEntry = new Label("Searched: " + city + " at " + java.time.LocalDateTime.now());
        historyBox.getChildren().add(0, historyEntry);
    }

    /**
     * Set background color based on time of day.
     */
    private void setDynamicBackground() {
        LocalTime time = LocalTime.now();

        // Choose background color based on time of day
        if (time.isAfter(LocalTime.of(6, 0)) && time.isBefore(LocalTime.of(18, 0))) {
            cityInput.getScene().setFill(Color.LIGHTSKYBLUE);
        } else {
            cityInput.getScene().setFill(Color.DARKSLATEBLUE);
        }
    }

    /**
     * Shows an error alert with a custom message.
     *
     * @param message Error message to display.
     */
    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
