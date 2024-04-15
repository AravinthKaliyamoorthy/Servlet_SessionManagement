package servlets;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@WebServlet(name = "loginservlet", value = "/loginservlet")
public class LoginServlet extends HttpServlet {

    private static final String CLIENT_ID = "762e065b1ff134a17c9d";  // Replace with your GitHub Client ID
    private static final String CLIENT_SECRET = "ad72270b8e67ffa539a724d87cfe2e43a6d81695";  // Replace with your GitHub Client Secret
    private static final String REDIRECT_URI = "http://localhost:8080/Servlet_SessionManagement/loginservlet"; // Replace with your redirect URI
    private static final String GIT_HUB_OAUTH_URL = "https://github.com/login/oauth/access_token";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String mailid = req.getParameter("mailid");
        String password = req.getParameter("password");
        if (!(mailid.isEmpty() && password.isEmpty())) {
            HttpSession session = req.getSession(true);
            session.setAttribute("username", mailid);
            resp.sendRedirect("index.html");
        } else {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid username or password");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String code = req.getParameter("code");

        if (code != null) {
            // Exchange authorization code for access token
            String accessToken = getAccessToken(code);

            // Use the access token to retrieve user information from GitHub API
            String userInfo = getUserInfo(accessToken);

            // Parse user information (e.g., username, email) and create session
            String username = parseUsername(userInfo);
            String email = parseEmail(userInfo);

            HttpSession session = req.getSession(true);
            session.setAttribute("username", username);
            session.setAttribute("email", email);

            resp.sendRedirect("index.html");
        } else {
            // User hasn't logged in yet, redirect back to login page (optional)
            resp.sendRedirect("login.html");
        }
    }

    // Method to exchange authorization code for access token
    private String getAccessToken(String code) throws IOException {
        URL url = new URL(GIT_HUB_OAUTH_URL + "?client_id=" + CLIENT_ID + "&client_secret=" + CLIENT_SECRET + "&code=" + code + "&redirect_uri=" + REDIRECT_URI);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        catch (IOException e) {
            // Handle potential IOException during HTTP request
            e.printStackTrace();
            throw new IOException("Failed to retrieve access token: " + e.getMessage());
        }

        String accessToken = parseAccessToken(response.toString());
        return accessToken;
    }

    // Helper method to parse access token from response string
    private String parseAccessToken(String response) {
        String[] split = response.split("=");
        return split[1]; // Assuming access token is the second element after splitting by "="
    }

    // Method to retrieve user information from GitHub API
    private String getUserInfo(String accessToken) throws IOException {
        URL url = new URL("https://api.github.com/user?access_token=" + accessToken);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        return response.toString();
    }

    // Method to parse username from user information JSON
    private String parseUsername(String userInfo) {
        JsonElement jsonElement = JsonParser.parseString(userInfo);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String username = jsonObject.get("login").getAsString();
        return username;
    }

    // Method to parse email from user information JSON (optional)
    private String parseEmail(String userInfo) {
        JsonElement jsonElement = JsonParser.parseString(userInfo);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        // Check if email exists in the JSON response
        if (jsonObject.has("email")) {
            String email = jsonObject.get("email").getAsString();
            return email;
        } else {
            // Email not available in the response, handle appropriately (optional)
            // You can log a message, return an empty string, etc.
            System.out.println("Email not found in user information");
            return "";
        }
    }


}
