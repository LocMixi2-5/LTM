import com.billiards.client.scenes.*;
import com.billiards.client.protocol.PacketType;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import javafx.stage.Window;

/** Capture production JavaFX views using explicitly synthetic account data. */
public class CaptureAllUi89 extends ReportUiCheck89 {
 static class CaptureNetwork extends FixtureNetwork {
  public void sendSearchPlayers(String s){}
  public void sendPendingRequestsList(){}
  public void sendPregameReady(){}
  public void sendFindMatch(){}
 }
 static void show(Scene s)throws Exception {fx(()->{stage.setScene(s);stage.sizeToScene();stage.setX(30);stage.setY(30);return null;});settle();}
 static void shot(Stage s,String name)throws Exception {settle();fx(()->{s.getScene().getRoot().applyCss();s.getScene().getRoot().layout();save(s.getScene().snapshot(null),name);return null;});System.out.println("CAPTURE "+name);}
 public static void main(String[] args)throws Exception {
  out=args[0];new java.io.File(out).mkdirs();Platform.startup(()->Platform.setImplicitExit(false));int status=0;
  try{
   CaptureNetwork net=new CaptureNetwork();net.setSession("CoThuDemo",400,3,4,2);
   fx(()->{stage=new Stage();stage.setX(30);stage.setY(30);return null;});
   SceneManager manager=new SceneManager(stage,net);
   show(fx(()->new LoginScene(net,manager).getScene()));fx(()->{stage.show();return null;});shot(stage,"login.png");
   fx(()->{ServerConfigDialog.show(stage,net,()->{});return null;});settle();
   Stage server=fx(()->(Stage)Window.getWindows().stream().filter(w->w!=stage&&w.isShowing()).findFirst().orElseThrow());shot(server,"server_config.png");fx(()->{server.close();return null;});
   LobbyScene lobby=fx(()->new LobbyScene(net,manager,"CoThuDemo",400,3,4,2));show(lobby.getScene());fx(()->{noarg(lobby,"stopFriendAutoRefresh");net.emit(PacketType.FRIEND_LIST_RESPONSE,"MinhDemo|2|0|true;ManhDemo|1|0|false");return null;});shot(stage,"lobby.png");
   FriendSearchDialog friends=fx(()->{FriendSearchDialog d=new FriendSearchDialog(stage,net);d.show();net.emit(PacketType.FRIEND_LIST_RESPONSE,"MinhDemo|2|0|true");net.emit(PacketType.SEARCH_PLAYERS_RESPONSE,"MinhDemo|2|0;TrungDemo|3|1");net.emit(PacketType.PENDING_REQUESTS_RESPONSE,"ManhDemo|1|0");return d;});shot(friends.getDialog(),"friend_search.png");fx(()->{friends.getDialog().close();return null;});
   SettingsDialog settings=fx(()->new SettingsDialog(stage,net,(a,c)->{}));Stage settingsStage=fx(()->(Stage)field(settings,"dialog"));
   Platform.runLater(settings::show);settle();
   TabPane tabs=fx(()->(TabPane)settingsStage.getScene().getRoot().lookup(".tab-pane"));
   for(int i=0;i<3;i++){final int ix=i;fx(()->{tabs.getSelectionModel().select(ix);return null;});shot(settingsStage,new String[]{"settings_cues.png","settings_avatar.png","settings_audio.png"}[i]);}
   fx(()->{settingsStage.close();return null;});
   LeaderboardScene ranking=fx(()->new LeaderboardScene(net,manager,"CoThuDemo",400,3,4,2));show(ranking.getScene());fx(()->{net.emit(PacketType.LEADERBOARD_RESPONSE,"1|TrungDemo|5|800|8|2;2|CoThuDemo|3|400|4|2;3|MinhDemo|2|200|2|3");return null;});shot(stage,"leaderboard.png");
   MatchmakingScene matchmaking=fx(()->new MatchmakingScene(net,manager,"CoThuDemo"));show(matchmaking.getScene());shot(stage,"matchmaking.png");fx(()->{((javafx.animation.Timeline)field(matchmaking,"dotsAnimation")).stop();return null;});
   PreGameScene pregame=fx(()->new PreGameScene(net,manager,"CoThuDemo",3,"MinhDemo",2));show(pregame.getScene());shot(stage,"pregame.png");fx(()->{((javafx.animation.Timeline)field(pregame,"countdownTimer")).stop();return null;});
   show(fx(()->new ResultScene(net,manager,true,100,"CoThuDemo","MinhDemo").getScene()));shot(stage,"result_win.png");
   show(fx(()->new ResultScene(net,manager,false,0,"CoThuDemo","MinhDemo").getScene()));shot(stage,"result_loss.png");
   System.out.println("Complete: source 89f7926 production scenes/dialogs, JavaFX Linux/Xvfb, synthetic data. Screenshots do not establish end-to-end correctness.");
  }catch(Throwable t){t.printStackTrace();status=1;}
  finally{fx(()->{for(Window w:java.util.List.copyOf(Window.getWindows()))w.hide();return null;});Platform.exit();}
  System.exit(status);
 }
}
