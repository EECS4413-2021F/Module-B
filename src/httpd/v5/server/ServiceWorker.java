package httpd.v5.server;

public abstract class ServiceWorker extends Worker {

  public final String uri;
  
  public ServiceWorker(HTTPServer server, String uri) {
    super(server);    
    this.uri = uri;
  }

  public abstract void doRequest(RequestContext request, ResponseContext response);
}
