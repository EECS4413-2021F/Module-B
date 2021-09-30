package httpd.v4.server;

import java.net.Socket;

class MyThread extends Thread {
  final Socket socket;

  MyThread(Worker w, Socket socket) {
    super(w);
    this.socket = socket;
  }
}
