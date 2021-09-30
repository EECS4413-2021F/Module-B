package httpd.v5.server;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;


public abstract class Worker implements Runnable {

  protected final HTTPServer server;

  public Worker(HTTPServer server) {
    this.server = server;   
  }

  public void run() {
    try {
      MyThread mt = (MyThread) Thread.currentThread();
      this.handle(mt.socket);
    } catch (Exception e) {
      server.insertLogEntry("Worker Exception: ", e.getMessage());
    }
  }

  protected abstract void doRequest(RequestContext request, ResponseContext response);

  protected void handle(Socket client) {
    final String clientAddress = client.getInetAddress() + ":" + client.getPort();

    try (Socket clientSocket = client;
         InputStream input   = client.getInputStream();
         OutputStream output = client.getOutputStream()
    ) {
      server.insertLogEntry("Connection Established:", clientAddress);

      RequestContext request   = new RequestContext(server, client.getInputStream());
      ResponseContext response = new ResponseContext(server, client.getOutputStream());
      doRequest(request, response);
      
      request.in.close();
      response.out.close();
    } catch (Exception e) {
      server.insertLogEntry("Connection Exception:", e.getMessage());
    } finally {
      server.insertLogEntry("Connection Ended:", clientAddress);
    }
  }
}
