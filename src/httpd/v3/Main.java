package httpd.v3;

import java.net.Socket;

import httpd.v3.server.MainService;


public class Main extends MainService {

  static {
    CalcOpService coService = new CalcOpService();
    
    services.put("/",         new HelloService());
    services.put("/calc",     new CalcService());
    services.put("/add",      coService);
    services.put("/subtract", coService);
    services.put("/multiply", coService);
    services.put("/divide",   coService);
    services.put("/exponent", coService);
    services.put("/students", new StudentsService());
  }

  public Main(Socket client) {
    super(client);
  }

  public static void main(String[] args) throws Exception {
    serverStart();
  }
}
