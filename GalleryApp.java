package cs1302.gallery;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

import javafx.scene.control.Alert.*;
import javafx.scene.control.*;
import javafx.event.*;
import javafx.scene.layout.*;
import javafx.animation.*;
import javafx.util.Duration;
import java.time.LocalTime;
import java.util.Random;
import javafx.scene.text.Text;
import java.net.*;
import com.google.gson.*;
import javafx.scene.control.ButtonBar.ButtonData;

import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.net.MalformedURLException;

import java.io.*;

/**
 * Represents an iTunes GalleryApp.
 */
public class GalleryApp extends Application {

    VBox vbox;
    String userSearch;
    String backupSearch;
    String formattedURL;

    JsonArray results;
    JsonArray usedJson;
    JsonArray allResults;

    String[] results2;

    ImageView[] imageviews = new ImageView[20];
    TilePane tilepane = new TilePane();

    ProgressBar p1 = new ProgressBar();
    double progress;
    Timeline timeline;

    boolean playing;
    double playing2 = 0;
    Button playButton;

/**
 * MenuBar generation.
 * @return menuBar a menubar with options
 */
    public MenuBar newMenu() {
        Menu menuFile = new Menu("File"); // creating the File tab
        Menu menuHelp = new Menu("Help");
        MenuBar menuBar = new MenuBar();

        MenuItem menuItemExit = new MenuItem("Exit");
        MenuItem menuItemAbout = new MenuItem("About");
        menuItemExit.setOnAction(e -> System.exit(0)); // change to sysexit
        menuItemAbout.setOnAction(e -> aboutEC());
        menuFile.getItems().addAll(menuItemExit);
        menuHelp.getItems().add(menuItemAbout);
        menuBar.getMenus().addAll(menuFile, menuHelp);
        return menuBar;
    } // newMenu

    /**
     * ToolBar for Commands.
     * @return toolBar that contains functions for the user.
     */
    public ToolBar newToolBar() {
        ToolBar toolBar = new ToolBar();
        playButton = new Button("Play");
        Separator sep1 = new Separator();
        Label searchLabel = new Label("Search Query:");
        TextField enterSearch = new TextField("Kid Cudi");
        userSearch = formatQuery(enterSearch);
        // backupSearch = userSearch;
        Button updateImages = new Button("Update Images");
        toolBar.getItems().addAll(playButton, sep1, searchLabel, enterSearch, updateImages);

        updateImages.setOnAction(e -> {
            String formatted2 = formatQuery(enterSearch);
            System.out.println(formatted2);

            userSearch = formatQuery(enterSearch);
            p1.setProgress(0); // added this 10:12

            Thread t = new Thread(() -> {
                getImages();
                Platform.runLater(() -> {
                    // pane.getChildren().add(updateImages());
                    updateImages();
                });
            });
            t.setDaemon(true);
            t.start();
        });

        playButton.setOnAction(e -> imageRandomizerGui());
        return toolBar;

    } // newToolBar

    /**
     * Randomly selects images to load in and cleans out old ones.
     */
    public void imageRandomizer() {
        EventHandler<ActionEvent> play = (e -> {
            Random rand = new Random();
            int random = rand.nextInt(results.size());
            int square = rand.nextInt(imageviews.length); // imageviews.length, 20
            JsonObject result = allResults.get(random).getAsJsonObject();
            JsonElement artworkUrl100 = result.get("artworkUrl100");
            if (artworkUrl100 != null) {
                usedJson.add(artworkUrl100);
                // System.out.println("in load art if");
                String artworkUrl = artworkUrl100.getAsString();
                Image image = new Image(artworkUrl);
                allResults.add(results.get(square)); // recycler
                imageviews[square] = new ImageView();
                imageviews[square].setImage(image);
                tilepane.getChildren().clear();
                for (int i = 0; i < 20; i++) {
                    tilepane.getChildren().add(imageviews[i]);
                } // for
            } // if
            if (usedJson.contains(allResults.get(random))) {
                allResults.remove(random);
            }
        }); // EH play
        timeline(play);
    } // imageRandomizer

    /**
     * Determines whether of not the images should be updating or not.
     */
    public void imageRandomizerGui() {
        // runs everytime playButton is pressed, starts 0 as pause
        playing2++;
        if (playing2 % 2 == 0) {
            playing = false;
        }
        if (playing2 % 2 != 0) {
            playing = true;
        }

        Platform.runLater(() -> {
            if (playing == true) {
                playButton.setText("Pause");
            }
            if (playing == false) {
                playButton.setText("Play");
                timeline.pause();
            }
        });
        if (playing == true) {
            imageRandomizer();
        }
    } // imageRandomizerGui

    /**
     * Timeline for the image updates.
     * @param handler an action event
     */
    public void timeline(EventHandler<ActionEvent> handler) {
        // event -> System.out.println(LocalTime.now());
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(2), handler);
        timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    } // timeline

    /**
     * Format the query to search using a TextField object.
     * @param textField a TextField object
     * @return formattedURL a string that is formatted properly
     */
    public String formatQuery(TextField textField) {
        userSearch = textField.getText();
        try {
            formattedURL = URLEncoder.encode(userSearch, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            System.out.println(uee.getMessage() + "  readQuery error");
        }
        String start = "https://itunes.apple.com/search?term=";
        String end = "&limit=200&media=music";
        formattedURL = start + formattedURL + end;
        return formattedURL;
    }

    /**
     * Format the query to search using a string.
     * @param userSearch a string to be formatted
     * @return formattedURL a string that is formatted properly
     */
    public String formatQuery(String userSearch) {
        try {
            formattedURL = URLEncoder.encode(userSearch, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            System.out.println(uee.getMessage() + "  readQuery error");
        }
        String start = "https://itunes.apple.com/search?term=";
        String end = "&limit/=200&media=music";
        formattedURL = start + formattedURL + end;
        return formattedURL;
    }

    /**
     * Parser for to read the Json files.
     * @param sUrl a string url
     * @param url a URL
     * @param reader an InputStreamReader
     */
    public void readJson(String sUrl, URL url, InputStreamReader reader) {
        p1.setProgress(0);
        System.out.println("Start of readJson()");
        // needs to be ran to use loadArtwork(), for results
        try {
            url = new URL(sUrl);
            reader = new InputStreamReader(url.openStream());
        } catch (MalformedURLException mue) {
            System.out.println(mue.getMessage());
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }
        JsonElement je = JsonParser.parseReader(reader);
        JsonObject root = je.getAsJsonObject();
        results = root.getAsJsonArray("results");
        allResults = root.getAsJsonArray("results");
        usedJson = new JsonArray();
        results2 = new String[results.size()];
        System.out.println("End of readJson()");
        // userSearch = backupSearch;
    }

    /**
     * Loads the urls for images.
     * @param i the position where the image is getting called
     */
    public void loadArtwork(int i) {
        JsonObject result = results.get(i).getAsJsonObject(); // will return 1-2-3 chain
        usedJson.add(result); // adds the jsonobj result
        JsonElement artworkUrl100 = result.get("artworkUrl100"); // artworkURl100 member

        if (artworkUrl100 != null) {
            // System.out.println("in load art if");
            String artworkUrl = artworkUrl100.getAsString();
            results2[i] = artworkUrl; // strings
            Image image = new Image(artworkUrl);
            imageviews[i] = new ImageView();
            // imageviews[i].setImage(new Image(image)); // needs to be imageview first
            imageviews[i].setImage(image);
        }
        // System.out.println(progress);
        progress = progress + 0.05;
        p1.setProgress(progress); // added this 10:12
        System.out.println(progress);
    }

    /** Gets images. */
    public void getImages() {
        tilepane.setPrefColumns(5);
        tilepane.setPrefRows(4);
        // backupSearch = userSearch;
        // String queryUrl = formatQuery(userSearch); // crashes it all
        String queryUrl = userSearch;
        URL url = null;
        InputStreamReader reader = null;
        readJson(queryUrl, url, reader); // String, URL, InputStreamReader
        if (results2.length > 20) {
            progress = 0;
            for (int i = 0; i < 20; i++) {
                loadArtwork(i);
            }
            //  backupSearch = userSearch;
            // System.out.println("right: " + backupSearch);
        }
        if (results2.length < 21) {
            Platform.runLater(() -> {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setResizable(true);
                alert.setTitle("Not enough images with query.");
                alert.setHeaderText("Change the query please.");
                // ButtonType type = new ButtonType("Ok", ButtonData.OK_DONE);
                // alert.getDialogPane().getButtonTypes().add(type);
                alert.setHeight(400);
                alert.setWidth(400);
                alert.showAndWait();
            });

            // System.out.println(userSearch);
            // System.out.println(backupSearch);
            // userSearch = backupSearch;
            p1.setProgress(1);
        }
        System.out.println("End of getImages()");
    } // getImages

    /**
     * Updates the images on the TilePane.
     * @return tilepane to add to the scene
     */
    public TilePane updateImages() {
        int available = results2.length;
        if (available > 20) {
            tilepane.getChildren().clear();
            for (int i = 0; i < 20; i++) {
                imageviews[i].setImage(new Image(results2[i]));
                tilepane.getChildren().add(imageviews[i]);
            }
        } else {
            return tilepane;
        }
        for (int i = 0; i < allResults.size(); i++) {
            if (usedJson.contains(allResults.get(i))) {
                allResults.remove(i);
            }
        }
        return tilepane;
    } // updateImages

    /**
     * Adds a progressbar and a credit to iTunes.
     * @return Hbox that holds the items.
     */
    public HBox newCredits() { // Progress & Image Credits
        HBox hbox = new HBox();
        Label label = new Label("Images provided courtesy of iTunes");
        hbox.getChildren().addAll(p1, label);
        return hbox;
    } // newCredits

    /** Information section. */
    public void aboutEC() {
        Stage about = new Stage();
        about.setTitle("About Nilay Patel");
        Text infos = new Text("Nilay Patel; ndp54703@uga.edu; Version 1.18");
        String picture = "https://i.ibb.co/pRnkCwv/profile-picture-2.jpg";
        VBox holder = new VBox();
        Image picture2 = new Image(picture);
        ImageView iv2 = new ImageView();
        iv2.setFitWidth(150);
        iv2.setFitHeight(150);
        iv2.setImage(picture2);
        holder.getChildren().addAll(infos, iv2);
        Scene scene2 = new Scene(holder);
        about.setScene(scene2);
        about.showAndWait();
    } // about

    @Override
    public void start(Stage stage) {
        // HBox pane = new HBox();
        VBox pane = new VBox();
        pane.getChildren().addAll(newMenu(), newToolBar());

        // Runnable r = () -> {
        /* task code here */
        //  };
        Thread t = new Thread(() -> {
            // getImages(userSearch);
            getImages();
            Platform.runLater(() -> {
                pane.getChildren().add(updateImages());
            });
        });
        t.setDaemon(true);
        t.start();
        pane.getChildren().addAll(newCredits());

        Scene scene = new Scene(pane, 500, 480);
        stage.setMaxWidth(1280);
        stage.setMaxHeight(720);
        stage.setTitle("GalleryApp!");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();
    } // start

} // GalleryApp
