package servlets;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public class LoginFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        boolean isStaticResource = httpRequest.getRequestURI().startsWith("/Servlet_SessionManagement/style/");
        if (isStaticResource) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpRequest.getSession(false);
        String contextPath = httpRequest.getContextPath();
        String loginURI = contextPath + "/loginform.html";
        String loginServletURI = contextPath + "/loginservlet";

        boolean loggedIn = session != null && session.getAttribute("username") != null;
        boolean loginFormRequest = httpRequest.getRequestURI().equals(loginURI);
        boolean loginRequest = httpRequest.getRequestURI().equals(loginServletURI);

        if (loggedIn || loginFormRequest || loginRequest) {
            chain.doFilter(request, response);
        } else {
            httpResponse.sendRedirect(loginURI);
        }
    }

}
