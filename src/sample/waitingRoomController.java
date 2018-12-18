package sample;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ResourceBundle;

public class waitingRoomController implements Initializable {
    public Label statusLabel;
    public DispatchingInterface impl;
    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
    public void setInterface(DispatchingInterface impl){
        this.impl=impl;
    }
    public void waitForOtherPlayer(String sessionToken, int aantalspelers, boolean host){
        try {
            Task<Void> task = new Task<Void>() {
                @Override protected Void call() throws Exception {
                    int poortnr=impl.addToGame(sessionToken, aantalspelers, host);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                playerGevonden(sessionToken, poortnr);
                            }catch (Exception e){

                            }
                        }
                    });
                    return null;
                }
            };
            Thread th = new Thread(task);

            th.setDaemon(true);

            th.start();
            System.out.println("zoeken naar ander"+ sessionToken);

        }
        catch (Exception e){
            e.printStackTrace();
            System.out.println(e);
        }




        }
        public void playerGevonden(String sessionToken, int poortnummer) throws RemoteException, NotBoundException {

            Registry myRegistry = LocateRegistry.getRegistry("localhost", poortnummer);
// search for CounterService
            AppInterface impl= (AppInterface) myRegistry.lookup("Login");
            impl.testConnectie();
            System.out.println("game started");
            FXMLLoader Loader=new FXMLLoader();
            Loader.setLocation(getClass().getResource("gameWindow.fxml"));
            try{
                Loader.load();
            }
            catch (Exception e){
                System.out.println("failed");
            }

            Stage stage=new Stage();
            GameWindowController controller =Loader.getController();
            controller.setInterface(impl);
            controller.setHelper(new ListenerHelper(controller,impl,sessionToken));
            Parent root=Loader.getRoot();
            stage.setTitle("Game");
            stage.setScene(new Scene(root, 780, 840));
            System.out.println("nieuw spel");
            stage.show();
            Stage oldstage  = (Stage) statusLabel.getScene().getWindow();
            oldstage.close();
            controller.setGame(sessionToken);



        }
}