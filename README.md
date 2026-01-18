# GWeather
GWeather helps you stay ahead of the weather with accurate, real-time updates and forecasts designed for everyday use.


### Current Status

The application is fully functional and meets all core requirements. It successfully:

- Fetches live weather data
- Manages user authentication
- Stores and displays a history of weather reports

### Next Steps
To elevate the project from a functional prototype to a production-ready application, the next focus areas would be:

Robustness
-Improve error handling and edge-case management (e.g., no internet connection, permission denial)
-Add stronger input validation and defensive checks

User Experience (UX)
-Enhance loading states and empty states
-Improve feedback for errors and user actions
-Make the interface feel smoother and more responsive
-Fix minor UI glitches and inconsistencies
-Weather history should display per user basis

Code Maintainability
-Refactor and simplify complex logic
-Move hardcoded values to appropriate resources
-Add meaningful comments and follow best practices for long-term scalability


Setup:
Open  `app/src/main/java/com/lovely/gweather/data/network/ApiConstants.kt`
Replace`"YOUR_API_KEY"`with a free API key from **OpenWeather**