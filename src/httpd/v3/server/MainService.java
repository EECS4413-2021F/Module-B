package httpd.v3.server;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;


public class MainService extends HTTPServer {

  protected static final Map<String, Service> services = new HashMap<>();
  
  public MainService(Socket client) {
    super(client);
  }

  protected void doRequest(RequestContext req, ResponseContext res) throws Exception {
    if (services.containsKey(req.uri)) {
      services.get(req.uri).doRequest(req, res);
    } else {
      res.setStatus(404);
    }
  }

  public static void serverStart() throws Exception {
    int port = 0;
    InetAddress host = InetAddress.getLocalHost(); // .getLoopbackAddress();
    try (ServerSocket server = new ServerSocket(port, 0, host)) {
      log.printf("Server listening on %s:%d\n", server.getInetAddress(), server.getLocalPort());
      while (true) {
        (new MainService(server.accept())).start();
      }
    }
  }
}
