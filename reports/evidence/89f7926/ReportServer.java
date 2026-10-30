import com.billiards.server.*;
import java.net.*;
/** Uses production handlers on a separate loopback port; no UDP discovery or app edits. */
public class ReportServer {
 public static void main(String[] args)throws Exception {
  DatabaseManager db=new DatabaseManager();
  if(!db.isConnected())throw new IllegalStateException("Local development database unavailable");
  AuthManager auth=new AuthManager(db);LobbyManager lobby=new LobbyManager(db);
  try(ServerSocket ss=new ServerSocket(12355,50,InetAddress.getByName("127.0.0.1"))){
   System.out.println("REPORT_SERVER_READY 127.0.0.1:12355");
   while(true){Socket s=ss.accept();s.setTcpNoDelay(true);new Thread(new ClientHandler(s,auth,lobby,db)).start();}
  }
 }
}
