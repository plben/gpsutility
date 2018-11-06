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

package net.benpl.gpsutility.logger.holux_m241;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import net.benpl.gpsutility.logger.PrimaryController;
import net.benpl.gpsutility.misc.Logging;
import net.benpl.gpsutility.misc.Utils;
import net.benpl.gpsutility.type.IController;
import net.benpl.gpsutility.type.ILoggerStateListener;

/**
 * Controller of {@link fxml/HoluxM241.fxml}
 */
public class Controller implements IController {

    private static final ObservableList<Integer> bySecList = FXCollections.observableArrayList(1, 5, 10, 15, 30, 60, 120);
    private static final ObservableList<Integer> byDistList = FXCollections.observableArrayList(50, 100, 150, 300, 500, 1000);

    private GpsLogger gpsLogger;

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private RadioButton rcdMethodOverlap;

    @FXML
    private RadioButton rcdMethodStop;

    @FXML
    private RadioButton rcdBySec;

    @FXML
    private RadioButton rcdByDist;

    @FXML
    private ComboBox<Integer> rcdBySecChooser;

    @FXML
    private ComboBox<Integer> rcdByDistChooser;

    @FXML
    private TextField ownerNameInput;

    @FXML
    private Button saveConfigBtn;

    @FXML
    private void saveConfigBtnActionPerformed(ActionEvent event) {
        gpsLogger.execLoggerTask(new LoggerTask.SaveConfig(getRcdMethod(), getRecordBy(), getRecordBySec(), getRecordByDist()) {
            @Override
            public void onStart() {
                inExecuting();
            }

            @Override
            public void onSuccess() {
                outExecuting();
            }

            /**
             * For critical failure, {@link net.benpl.gpsutility.logger.LoggerThread} will notify {@link PrimaryController}
             * via {@link ILoggerStateListener#loggerIdle()}. This AnchorPane will be removed by {@link PrimaryController}.
             * So no action is necessary here.
             */
            @Override
            public void onFail() {
                Logging.errorln("Save logger config ... fail");
                outExecuting();
            }
        });
    }

    @FXML
    private Button renameOwnerBtn;

    @FXML
    private void renameOwnerBtnActionPerformed(ActionEvent event) {
        String userName = ownerNameInput.getText();
        if (Utils.isEmpty(userName)) return;

        gpsLogger.execLoggerTask(new LoggerTask.ModUserName(userName) {
            @Override
            public void onStart() {
                inExecuting();
            }

            @Override
            public void onSuccess() {
                outExecuting();
            }

            /**
             * For critical failure, {@link net.benpl.gpsutility.logger.LoggerThread} will notify {@link PrimaryController}
             * via {@link ILoggerStateListener#loggerIdle()}. This AnchorPane will be removed by {@link PrimaryController}.
             * So no action is necessary here.
             */
            @Override
            public void onFail() {
                Logging.errorln("Modify logger UserName ... fail");
                outExecuting();
            }
        });
    }

    @Override
    public void initialize() {
        // Record Method radio button group
        ToggleGroup toggleGroup1 = new ToggleGroup();
        rcdMethodOverlap.setToggleGroup(toggleGroup1);
        rcdMethodStop.setToggleGroup(toggleGroup1);

        // Record By radio button group
        ToggleGroup toggleGroup2 = new ToggleGroup();
        rcdBySec.setToggleGroup(toggleGroup2);
        rcdByDist.setToggleGroup(toggleGroup2);
        // listen to changes in selected toggle
        toggleGroup2.selectedToggleProperty().addListener((observable, oldVal, newVal) -> {
            if (newVal == rcdBySec) {
                rcdBySecChooser.setDisable(false);
                rcdByDistChooser.setDisable(true);
            } else {
                rcdBySecChooser.setDisable(true);
                rcdByDistChooser.setDisable(false);
            }
        });

        // Load Record By combobox
        rcdBySecChooser.setItems(bySecList);
        rcdByDistChooser.setItems(byDistList);
    }

    public void setGpsLogger(GpsLogger gpsLogger) {
        this.gpsLogger = gpsLogger;
    }

    public void setUserName(String userName) {
        ownerNameInput.setText(userName);
    }

    public void setRcdMethod(int rcdMethod) {
        if (rcdMethod == 1) {
            rcdMethodOverlap.setSelected(true);
        } else {
            rcdMethodStop.setSelected(true);
        }
    }

    public int getRcdMethod() {
        if (rcdMethodOverlap.isSelected()) {
            return 1;
        } else {
            return 2;
        }
    }

    public void setRecordBy(int rcdBy) {
        if (rcdBy == 0) {
            rcdBySec.setSelected(true);
        } else if (rcdBy == 1) {
            rcdByDist.setSelected(true);
        }
    }

    public int getRecordBy() {
        if (rcdBySec.isSelected()) {
            return 0;
        } else {
            return 1;
        }
    }

    public boolean setRecordBySec(int seconds) {
        for (int i = 0; i < bySecList.size(); i++) {
            if (seconds == bySecList.get(i)) {
                rcdBySecChooser.getSelectionModel().select(i);
                return true;
            }
        }
        return false;
    }

    public int getRecordBySec() {
        return rcdBySecChooser.getValue();
    }

    public boolean setRecordByDist(int meters) {
        for (int i = 0; i < byDistList.size(); i++) {
            if (meters == byDistList.get(i)) {
                rcdByDistChooser.getSelectionModel().select(i);
                return true;
            }
        }
        return false;
    }

    public int getRecordByDist() {
        return rcdByDistChooser.getVisibleRowCount();
    }

    /**
     * Transit to 'executing' state.
     * During executing state, no component should be operational.
     */
    private void inExecuting() {
        rcdMethodOverlap.setDisable(true);
        rcdMethodStop.setDisable(true);
        rcdBySec.setDisable(true);
        rcdByDist.setDisable(true);
        rcdBySecChooser.setDisable(true);
        rcdByDistChooser.setDisable(true);

        ownerNameInput.setDisable(true);

        saveConfigBtn.setDisable(true);
        renameOwnerBtn.setDisable(true);

        ObservableList<Tab> tabs = ((TabPane) anchorPane.getParent().getParent()).getTabs();
        tabs.forEach(tab -> {
            if (tab.getContent() != anchorPane) tab.setDisable(true);
        });
    }

    /**
     * Exit from 'executing' state.
     */
    private void outExecuting() {
        rcdMethodOverlap.setDisable(false);
        rcdMethodStop.setDisable(false);
        rcdBySec.setDisable(false);
        rcdByDist.setDisable(false);
        rcdBySecChooser.setDisable(!rcdBySec.isSelected());
        rcdByDistChooser.setDisable(!rcdByDist.isSelected());

        ownerNameInput.setDisable(false);

        saveConfigBtn.setDisable(false);
        renameOwnerBtn.setDisable(false);

        ObservableList<Tab> tabs = ((TabPane) anchorPane.getParent().getParent()).getTabs();
        tabs.forEach(tab -> tab.setDisable(false));
    }

}
