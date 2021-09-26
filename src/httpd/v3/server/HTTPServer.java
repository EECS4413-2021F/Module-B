package httpd.v3.server;

import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;


public class HTTPServer {

  public final PrintStream log;
  public final InetAddress host;
  public final int port;

  private Map<String, ServiceWorker> services = new HashMap<>();

  public HTTPServer(InetAddress host, int port, PrintStream log, Map<String, BiFunction<HTTPServer, String, ServiceWorker>> serviceConstrs) {
    this.log  = log;
    this.host = host;
    this.port = port;

    serviceConstrs.forEach((uri, constr) -> {
      services.put(uri, constr.apply(this, uri));
    });

    Worker worker = RootWorker.getInstance(this, services);

    try (ServerSocket server = new ServerSocket(port, 0, host)) {
      insertLogEntry("Server Start:", server.getInetAddress().toString() + ":" + server.getLocalPort());

      while (true) {
        Socket client = server.accept();
        MyThread mt = new MyThread(worker, client);
        mt.start();
      }
    } catch (Exception e) {
      insertLogEntry("Server Exception:", e.getMessage());
    } finally {
      insertLogEntry("Server", "Stop");
    }
  }

  public void insertLogEntry(String entry, String subEntry) {
    log.printf("[%tT] %s %s\n", new Date(), entry, subEntry);
  }
}
