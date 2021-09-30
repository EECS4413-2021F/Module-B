import java.io.*;
import javax.servlet.*;
import javax.servlet.annotation.*;
import javax.servlet.http.*;

/**
 * Servlet implementation class Start
 */
@WebServlet(urlPatterns = {"/Start", "/Startup", "/Startup/*"})
public class Start extends HttpServlet {

    /**
     * @see HttpServlet#HttpServlet()
     */
    public Start() {
        super();
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // do nothing
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        PrintStream console = System.out;
        PrintWriter res = response.getWriter();

        // In doGet, use System.out.println
        // to log a message on the console.

        console.println("Incoming request");

        // Send the message to the client instead.
        // Set the response content type to
        // text/plain and use its Writer.

        response.setContentType("text/plain");

        theNetworkingLayer(request, response, res);
        theHTTPLayer(request, res);
        theURL(request, response, res);
        theDeploymentDescriptor(request, res);
        theComputation(request, res);

    } // doGet

    private void theNetworkingLayer(
                    HttpServletRequest request,
                    HttpServletResponse response,
                    PrintWriter res
            ) throws IOException {

        // Show your IP and port to the client.

        res.println("Server IP:\t\t"   + request.getLocalAddr());
        res.println("Server Port:\t\t" + request.getLocalPort());

        // Echo the client's IP and port.

        res.println("Client IP:\t\t"   + request.getRemoteAddr());
        res.println("Client Port:\t\t" + request.getRemotePort());

        // How would you implement an IP-filtering firewall.

        if (request.getRemoteAddr().equals("<blocked IP>")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }

        res.println();

    } // theNetworkingLayer

    private void theHTTPLayer(HttpServletRequest request, PrintWriter res) {

        // Output the request's protocol and method.

        res.println("Request Protocol:\t" + request.getProtocol());
        res.println("Request Method:\t\t" + request.getMethod());

        // Output the entire query string sent be the client.

        res.println("Query String:\t\t" + request.getQueryString());

        // Embed spaces in the query string and
        // note the URL encoding in the develop tool.

        // Query String: "Bob Smith"
        // Encoded as: "Bob%20Smith"

        // Use the getParameter to extract a named request parameter

        if (request.getQueryString() != null) {
            res.println();
            for (String param : request.getParameterMap().keySet()) {
                res.println("\t" + param + ":\t" + request.getParameter(param));
            }
        }

        res.println();

    } // theHTTPLayer

    private void theURL(
                    HttpServletRequest request,
                    HttpServletResponse response,
                    PrintWriter res
            ) throws IOException {

        //
        // Change the annotation of your servlet
        // so it responds to /Startup as well as /Start.
        //
        // Change the URL mapping again to
        // include /Startup/*
        //

        // Output the various pieces of the URL:
        // URI, context, extra path, and the
        // translated path.

        res.println("Requested URI:\t\t" + request.getRequestURI());
        res.println("Context Path:\t\t"  + request.getContextPath());
        res.println("Extra Path Info:\t" + request.getPathInfo());
        res.println("Translated Path:\t" + request.getPathTranslated());

        // If the client visited your webapp via
        // /Startup/YorkBank then force their browser
        // to redirect to Start. Hint: use the sendRedirect
        // method of the response object.

        if ((request.getContextPath() + "/Startup/YorkBank").equals(request.getRequestURI())) {
            response.sendRedirect(request.getContextPath() + "/Start");
        }

        res.println();

    } // theURL

    private void theDeploymentDescriptor(HttpServletRequest request, PrintWriter res) {

        //
        // Add Start to the welcome file list and test it.
        //
        // Add a context parameter and verify
        // that you can access it in the servlet.
        //
        // Add an error-page for error-code 404
        // and point it at location /res/my404.jspx.
        // Test by visiting a non-existent page.
        //
        // Add an error-page for exception-type
        // java.lang.Exception and point it at location
        // /res/myException.jspx. Test by triggerring
        // any exception in your servlet.
        //

        //res.println("Ctx Parameter app_name: "
        //        + request
        //            .getServletContext()
        //            .getInitParameter("app_name"));
        //res.println();

    } // theDeploymentDescriptor

    private void theComputation(HttpServletRequest request, PrintWriter res) {

        //
        // Extract the values of the following request parameters: principle,
        // amortization, and interest. You can assume that any client-supplied value is
        // valid; i.e. do not validate.
        //
        // If a parameter is missing, supply a default value obtained from a context
        // parameter. This way, one can change the defaults w/o recompiling the servlet.
        //
        // Compute the monthly payment using the formula: r*A/[1 - (1+r)-n], where r is
        // the monthly interest rate, A is the present value (principle), and n is the
        // amortization period measured in months.
        //
        // Send the computed payment with an appropriate message to the client but format
        // the amount so it is rounded to the nearest cent.
        //
        // The complete mCalc webapp that we seek to duplicate sends, in addition to the
        // monthly payment, the option to recompute with a different interest rate; i.e.
        // the client supplies only the new interest rate without supplying the other two
        // parameters (the server must use their previous values. Can you think of a way
        // to replicate this functionality in our mCalc0 (even though http is stateless)?
        // Your solution must not involve creating a second servlet. Note: the older
        // values must be remembered somewhere; either on the server, or on the client,
        // or on the network.
        //

        HttpSession session = request.getSession();

        double principle = request.getParameter("principle") != null
            ? Double.parseDouble(request.getParameter("principle"))
            : (session.getAttribute("principle") != null
                ? (double)session.getAttribute("principle")
                : Double.parseDouble(request
                    .getServletContext()
                    .getInitParameter("principle")
                )
            );

        double amortization = request.getParameter("amortization") != null
            ? Double.parseDouble(request.getParameter("amortization"))
            : (session.getAttribute("amortization") != null
                ? (double)session.getAttribute("amortization")
                : Double.parseDouble(request
                    .getServletContext()
                    .getInitParameter("amortization")
                )
            );

        double interest = Double.parseDouble(request.getParameter("interest") != null
            ? request.getParameter("interest")
            : request
                .getServletContext()
                .getInitParameter("interest")
        ) / 100;

        double monthlyInterest = interest / 12;

        double monthlyPayment =
            monthlyInterest * principle / (
                1 - Math.pow(1 + monthlyInterest, -12 * amortization)
            );

        session.setAttribute("principle"   , principle);
        session.setAttribute("amortization", amortization);

        res.printf("Principle:\t\t%.2f\n"     , principle);
        res.printf("Amortization:\t\t%.2f\n"  , amortization);
        res.printf("Interest Rate:\t\t%.2f%%\n", interest * 100);
        res.printf("Monthly Payment:\t%.2f\n" , monthlyPayment);

        res.println();

    } // theComputation

} // Start
