package httpd.v3.server;

import java.io.PrintStream;
import java.net.Socket;


public abstract class HTTPServer extends Thread {
  
  public static final PrintStream log = System.out;

  protected Socket client;
  
  public HTTPServer(Socket client) {
    this.client = client;
  }

  protected abstract void doRequest(RequestContext req, ResponseContext res) throws Exception;

  public void run() {
    final String clientAddress = String.format("%s:%d", client.getInetAddress(), client.getPort());
    log.printf("Connected to %s\n", clientAddress);

    try (Socket client = this.client) {
      RequestContext  request  = null;
      ResponseContext response = null;

      try {
        request  = new RequestContext(client.getInputStream());
        response = new ResponseContext(client.getOutputStream());
        request.processRequest(response);        
        doRequest(request, response);
      } catch (Exception err) {       
        log.println(err.getMessage());
        response.setStatus(500);
      }

      if (response.getStatus() != 200 && response.getResponseText().isEmpty()) {
        response.out.println(response.getStatusText());
      }

      response.headers.put("Content-Length", response.getResponseText().getBytes().length);
      response.send(!request.method.equals("HEAD"));

      request.req.close();
      response.res.close();
    } catch (Exception e) {
      log.println(e);
    } finally {
      log.printf("Disconnected from %s\n", clientAddress);
    }
  }
}
