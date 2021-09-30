package httpd.v3.server;

public interface Service {
  public void doRequest(RequestContext request, ResponseContext response) throws Exception;
}
