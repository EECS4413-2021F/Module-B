package httpd.v5;

import java.io.PrintStream;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import httpd.v5.server.HTTPServer;
import httpd.v5.server.ServiceWorker;


public class Main {
  public static void main(String[] args) throws Exception {
    int port         = 0;
    InetAddress host = InetAddress.getLocalHost();
    PrintStream log  = System.out;

    Map<String, BiFunction<HTTPServer, String, ServiceWorker>> serviceConstrs = new HashMap<>();

    serviceConstrs.put("/",         HelloService::new);
    serviceConstrs.put("/calc",     CalcService::new);
    serviceConstrs.put("/add",      CalcOpService::new);
    serviceConstrs.put("/subtract", CalcOpService::new);
    serviceConstrs.put("/multiply", CalcOpService::new);
    serviceConstrs.put("/divide",   CalcOpService::new);
    serviceConstrs.put("/exponent", CalcOpService::new);
    serviceConstrs.put("/students", StudentsService::new);

    new HTTPServer(host, port, log, serviceConstrs);
  }
}
