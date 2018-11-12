/*
 * Copyright 2018 Ben Peng
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package net.benpl.gpsutility;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import net.benpl.gpsutility.logger.PrimaryController;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;

/**
 * The entry point of this application.
 */
public class AppEntry extends Application {

    private PrimaryController primaryController;

    public static void main(String[] args) {
        Locale.setDefault(Locale.ENGLISH);
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        // Load .fxml and reflect it into AnchorPane object.
        File fxmlFile = new File("fxml/PrimaryWindow.fxml");
        FXMLLoader loader = new FXMLLoader(fxmlFile.toURI().toURL());
        Parent root = loader.load();

        // Get attached controller from AnchorPane object.
        this.primaryController = loader.getController();

        // Override application default OnClose event handler.
        primaryStage.setOnCloseRequest(event -> {
            // Call Controller to continue this close operation
            this.primaryController.destroy();
            // Consume this event, let Controller to release all pending resources
            event.consume();
        });

        // Display this AnchorPane window.
        primaryStage.setTitle("GPS Utility - " + Version.current);
        File logoFile = new File("images/ic_logo.png");
        primaryStage.getIcons().add(new Image(new FileInputStream(logoFile)));
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);
        primaryStage.show();
    }
}
