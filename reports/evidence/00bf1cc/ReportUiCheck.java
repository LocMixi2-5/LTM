import com.billiards.client.NetworkClient;
import com.billiards.client.protocol.PacketType;
import com.billiards.client.scenes.*;
import javafx.application.Platform;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.WritableImage;
import javafx.scene.robot.Robot;
import javafx.stage.Stage;
import java.lang.reflect.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class ReportUiCheck {
 static class FixtureNetwork extends NetworkClient {
  Consumer<ReceivedPacket> callback; String[] registered; String requestedHistory;
  FixtureNetwork(){super("127.0.0.1",12345);}
  public void setOnPacketReceived(Consumer<ReceivedPacket> c){callback=c;}
  public void sendFriendListRequest(){}
  public void sendLeaderboardRequest(){}
  public void sendChatHistoryRequest(String n){requestedHistory=n;}
  public void sendChatMessage(String n,String m){}
  public boolean isConnected(){return true;}
  public void sendRegister(String e,String u,String p){registered=new String[]{e,u,p};}
  void emit(PacketType t,String s){callback.accept(new ReceivedPacket(t,s));}
 }
 static Stage stage; static String out;
 static <T>T fx(Callable<T> f)throws Exception{FutureTask<T> t=new FutureTask<>(f);Platform.runLater(t);return t.get(10,TimeUnit.SECONDS);}
 static Object field(Object o,String n)throws Exception{Field f=o.getClass().getDeclaredField(n);f.setAccessible(true);return f.get(o);}
 static Object call(Object o,String n,Class<?>[] types,Object...args)throws Exception{Method m=o.getClass().getDeclaredMethod(n,types);m.setAccessible(true);return m.invoke(o,args);}
 static void noarg(Object o,String n)throws Exception{call(o,n,new Class<?>[]{});}
 static void text(Object o,String n,String value)throws Exception{((TextInputControl)field(o,n)).setText(value);}
 static void settle()throws Exception{Thread.sleep(200);fx(()->null);}
 static void check(boolean b,String m){if(!b)throw new AssertionError(m);}
 static void save(WritableImage im,String name)throws Exception{
  BufferedImage b=new BufferedImage((int)im.getWidth(),(int)im.getHeight(),BufferedImage.TYPE_INT_ARGB);
  for(int y=0;y<b.getHeight();y++)for(int x=0;x<b.getWidth();x++)b.setRGB(x,y,im.getPixelReader().getArgb(x,y));
  ImageIO.write(b,"png",new File(out,name));
 }
 static void snap(String name,boolean popup)throws Exception{settle();fx(()->{
  stage.getScene().getRoot().applyCss();stage.getScene().getRoot().layout();
  WritableImage im=popup?new Robot().getScreenCapture(null,stage.getX(),stage.getY(),stage.getWidth(),stage.getHeight()):stage.getScene().snapshot(null);
  save(im,name);return null;});}
 static void append(LobbyScene l,String sender,String time,String msg,boolean mine)throws Exception{
  call(l,"appendChatMessage",new Class<?>[]{String.class,String.class,String.class,boolean.class},sender,time,msg,mine);
 }
 public static void main(String[] args)throws Exception{
  out=args[0];Platform.startup(()->Platform.setImplicitExit(false));int result=0;
  try{
   FixtureNetwork net=new FixtureNetwork();
   RegisterScene register=fx(()->{stage=new Stage();stage.setX(30);stage.setY(30);RegisterScene r=new RegisterScene(net,new SceneManager(stage,net));stage.setScene(r.getScene());stage.show();return r;});
   fx(()->{text(register,"emailField","cothu@example.com");text(register,"usernameField","CoThuDemo");text(register,"passwordField","ReportTest2026");text(register,"confirmField","ReportTest2026");return null;});
   snap("registration_actual.png",false);
   fx(()->{noarg(register,"doRegister");check(net.registered!=null&&net.registered[0].equals("cothu@example.com"),"Explicit email reaches sendRegister");net.registered=null;
    text(register,"emailField","email-khong-hop-le");noarg(register,"doRegister");check(net.registered==null,"Block malformed email");return null;});
   snap("registration_validation.png",false);
   fx(()->{text(register,"emailField","player@example.technology");noarg(register,"doRegister");check(net.registered==null,"Long valid TLD blocked by current regex");return null;});
   System.out.println("PASS JavaFX explicit email submission and malformed-email validation (fixture transport)");
   System.out.println("FINDING JavaFX rejects a syntactically valid long TLD: player@example.technology");
   LobbyScene lobby=fx(()->{LobbyScene l=new LobbyScene(net,new SceneManager(stage,net),"CoThuDemo",0,1,0,0);stage.setScene(l.getScene());stage.sizeToScene();noarg(l,"stopFriendAutoRefresh");return l;});
   String a="MinhDemo|2|0|true;ManhDemo|1|0|false";
   fx(()->{net.emit(PacketType.FRIEND_LIST_RESPONSE,a);return null;});settle();
   Object old=fx(()->((VBox)field(lobby,"friendListBox")).getChildren().get(0));
   fx(()->{net.emit(PacketType.FRIEND_LIST_RESPONSE,a);return null;});settle();
   check(fx(()->((VBox)field(lobby,"friendListBox")).getChildren().get(0))==old,"Identical friends avoid rebuild");
   snap("lobby_actual.png",false);
   fx(()->{for(Node n:((VBox)field(lobby,"friendListBox")).lookupAll(".button"))if(n instanceof Button b&&b.getText().equals("▾")){b.fire();break;}return null;});settle();
   check(fx(()->field(lobby,"activeFriendMenu"))!=null,"Friend dropdown opens");
   snap("friend_menu_actual.png",true);
   String b="MinhDemo|2|0|false;ManhDemo|1|0|false";
   fx(()->{net.emit(PacketType.FRIEND_LIST_RESPONSE,b);return null;});settle();
   check(fx(()->((ContextMenu)field(lobby,"activeFriendMenu")).isShowing()),"Menu survives refresh");
   check(fx(()->field(lobby,"pendingFriendListData")).equals(b),"Update deferred");
   fx(()->{((ContextMenu)field(lobby,"activeFriendMenu")).hide();return null;});settle();
   check(fx(()->field(lobby,"lastFriendListData")).equals(b),"Deferred refresh applies after hide");
   System.out.println("PASS friend menu remains open during refresh; unchanged data skips redraw; deferred data applied on close");
   // Reproduce A -> B -> A while menu remains open.
   fx(()->{net.emit(PacketType.FRIEND_LIST_RESPONSE,a);return null;});settle();
   fx(()->{for(Node n:((VBox)field(lobby,"friendListBox")).lookupAll(".button"))if(n instanceof Button bt&&bt.getText().equals("▾")){bt.fire();break;}return null;});settle();
   fx(()->{net.emit(PacketType.FRIEND_LIST_RESPONSE,b);return null;});settle();
   fx(()->{net.emit(PacketType.FRIEND_LIST_RESPONSE,a);return null;});settle();
   fx(()->{((ContextMenu)field(lobby,"activeFriendMenu")).hide();return null;});settle();
   check(fx(()->field(lobby,"lastFriendListData")).equals(b),"Current implementation applies stale B after A-B-A");
   System.out.println("FINDING friend list A->B->A while menu open applies stale B after close");
   fx(()->{call(lobby,"showChatView",new Class<?>[]{String.class},"MinhDemo");append(lobby,"MinhDemo","10:30","OK",false);append(lobby,"MinhDemo","10:30","OK",false);check(((VBox)field(lobby,"chatMessagesBox")).getChildren().size()==1,"Duplicate key suppresses row");return null;});
   System.out.println("FINDING two intentional equal messages in the same minute render as one (no message ID)");
   fx(()->{((VBox)field(lobby,"chatMessagesBox")).getChildren().clear();append(lobby,"MinhDemo","10:31","Chào bạn, cùng chơi một ván nhé!",false);append(lobby,"Bạn","10:32","Mình đã sẵn sàng, bạn gửi lời mời nhé.",true);append(lobby,"MinhDemo","10:33","Được nhé. Mình sẽ gửi ngay.",false);return null;});
   snap("chat_actual.png",false);
   // Delayed history from a former conversation is not correlated with the current friend.
   fx(()->{call(lobby,"showChatView",new Class<?>[]{String.class},"ManhDemo");net.emit(PacketType.CHAT_HISTORY_RESPONSE,"MinhDemo|10:31|Lich su cua Minh");return null;});settle();
   check(fx(()->field(lobby,"currentChatFriend")).equals("ManhDemo"),"Current friend is Manh");
   check(fx(()->((VBox)field(lobby,"chatMessagesBox")).getChildren().get(0).getUserData().toString()).contains("MinhDemo"),"Old history appears in new chat");
   System.out.println("FINDING late history response from prior friend can populate current conversation (fixture callback replay)");
   // New changes: thread-affine timeline, challenge popup, non-blocking result toast.
   noarg(lobby,"startFriendAutoRefresh");settle();
   check(fx(()->field(lobby,"friendRefreshTimeline"))!=null,"Start refresh off FX thread safely schedules on FX");
   noarg(lobby,"stopFriendAutoRefresh");settle();
   check(fx(()->field(lobby,"friendRefreshTimeline"))==null,"Stop refresh off FX thread safely schedules on FX");
   System.out.println("PASS friend refresh start/stop called from worker thread uses JavaFX thread");
   fx(()->{net.emit(PacketType.FRIEND_LIST_RESPONSE,a);return null;});settle();
   fx(()->{noarg(lobby,"showFriendListView");call(lobby,"showChallengeReceivedDialog",new Class<?>[]{String.class},"MinhDemo");return null;});settle();
   check(fx(()->((Stage)field(lobby,"activeChallengeDialog")).isShowing()),"Challenge popup visible");
   check(fx(()->((Stage)field(lobby,"activeChallengeDialog")).isAlwaysOnTop()),"Challenge popup always on top");
   snap("challenge_actual.png",true);
   fx(()->{noarg(lobby,"closeActiveChallengeDialog");return null;});
   System.out.println("PASS challenge popup shown above owner and closes through JavaFX lifecycle");
   java.io.ByteArrayOutputStream cb=new java.io.ByteArrayOutputStream();
   java.io.DataOutputStream cd=new java.io.DataOutputStream(cb);cd.writeBoolean(false);byte[] msg="Lời mời đã bị từ chối.".getBytes(java.nio.charset.StandardCharsets.UTF_8);cd.writeInt(msg.length);cd.write(msg);
   net.callback.accept(new NetworkClient.ReceivedPacket(PacketType.CHALLENGE_RESULT,cb.toByteArray()));settle();
   check(fx(()->field(lobby,"currentToast"))!=null,"Challenge result uses toast");
   snap("challenge_toast.png",false);
   System.out.println("PASS challenge result shows toast without modal alert");
   fx(()->{noarg(lobby,"stopFriendAutoRefresh");noarg(lobby,"dismissCurrentToast");return null;});
   GameScene practice=fx(()->{GameScene g=new GameScene(net,new SceneManager(stage,net),"SoloDemo");stage.setScene(g.getScene());stage.sizeToScene();return g;});settle();
   check(fx(()->(boolean)field(practice,"isPractice")),"Practice flag");
   check(fx(()->!((HBox)field(practice,"oppBallsBox")).isVisible()&&!((HBox)field(practice,"oppBallsBox")).isManaged()),"Opponent balls excluded from solo layout");
   check(fx(()->practice.getScene().getRoot().lookupAll(".label").stream().noneMatch(n->n instanceof Label l&&l.getText().contains("Khung Trống"))),"No empty solo slot label");
   snap("practice_actual.png",false);
   fx(()->{((javafx.animation.AnimationTimer)field(practice,"gameLoop")).stop();return null;});
   System.out.println("PASS solo practice hides opponent layout and removes empty slot");
   GameScene game=fx(()->{GameScene g=new GameScene(net,new SceneManager(stage,net),"CoThuDemo",0,"#DEB887","MinhDemo",0,"#DEB887",1,false,123L);stage.setScene(g.getScene());stage.sizeToScene();return g;});
   java.io.ByteArrayOutputStream ab=new java.io.ByteArrayOutputStream();java.io.DataOutputStream ad=new java.io.DataOutputStream(ab);ad.writeBoolean(true);ad.writeDouble(.65);ad.writeDouble(.5);ad.writeDouble(0);ad.writeDouble(0);
   net.callback.accept(new NetworkClient.ReceivedPacket(PacketType.CUE_AIM,ab.toByteArray()));settle();
   check(fx(()->(boolean)field(game,"oppIsAiming")),"Aim payload activates opponent cue");
   snap("game_actual.png",false);
   fx(()->{((javafx.animation.AnimationTimer)field(game,"gameLoop")).stop();return null;});
   System.out.println("PASS current 34-byte aim frame decoded by GameScene (fixture callback)");
   // Actual parser expects a string even though the server sends only a boolean.
   boolean settingsMismatch=false;
   try{call(net,"readPacketPayload",new Class<?>[]{PacketType.class,java.io.DataInputStream.class},PacketType.SETTINGS_RESPONSE,new java.io.DataInputStream(new java.io.ByteArrayInputStream(new byte[]{1})));}
   catch(NoSuchMethodException e){Method m=NetworkClient.class.getDeclaredMethod("readPacketPayload",PacketType.class,java.io.DataInputStream.class);m.setAccessible(true);try{m.invoke(net,PacketType.SETTINGS_RESPONSE,new java.io.DataInputStream(new java.io.ByteArrayInputStream(new byte[]{1})));}catch(InvocationTargetException failure){settingsMismatch=failure.getCause() instanceof java.io.EOFException;}}
   check(settingsMismatch,"Client expects nonexistent string in settings response");
   System.out.println("FINDING client SETTINGS_RESPONSE parser requires missing string; boolean-only server payload raises EOF");
   System.out.println("Screenshots: actual JavaFX scenes of 00bf1cc; controlled sample data and fixture transport, not end-to-end chat/game tests.");
  }catch(Throwable e){e.printStackTrace();result=1;}
  finally{fx(()->{if(stage!=null)stage.hide();return null;});Platform.exit();}
  System.exit(result);
 }
}
