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
import net.benpl.gpsutility.logger.ActionListener;
import net.benpl.gpsutility.logger.ActionTask;
import net.benpl.gpsutility.logger.PrimaryController;
import net.benpl.gpsutility.logger.StateListener;
import net.benpl.gpsutility.misc.Logging;
import net.benpl.gpsutility.misc.Utils;

/**
 * Controller of {@link fxml/HoluxM241.fxml}
 */
public class Controller implements net.benpl.gpsutility.logger.Controller {

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
        gpsLogger.performSaveConfig(new ActionListener() {
            @Override
            public void onStart() {
                priorExecution();
            }

            @Override
            public void onSuccess() {
                postExecution();
            }

            /**
             * For critical failure, {@link net.benpl.gpsutility.logger.LoggerThread} will notify {@link PrimaryController}
             * via {@link StateListener#stateChanged(int)}. This AnchorPane will be removed by {@link PrimaryController}.
             * So no action is necessary here.
             */
            @Override
            public void onFail(ActionTask.CAUSE cause) {
                Logging.errorln("Save logger config ... fail");
                postExecution();
            }
        }, getRcdMethod(), getRecordBy(), getRecordBySec(), getRecordByDist());
    }

    @FXML
    private Button renameOwnerBtn;

    @FXML
    private void renameOwnerBtnActionPerformed(ActionEvent event) {
        String userName = ownerNameInput.getText();
        if (Utils.isEmpty(userName)) return;

        gpsLogger.performModifyUserName(new ActionListener() {
            @Override
            public void onStart() {
                priorExecution();
            }

            @Override
            public void onSuccess() {
                postExecution();
            }

            /**
             * For critical failure, {@link net.benpl.gpsutility.logger.LoggerThread} will notify {@link PrimaryController}
             * via {@link StateListener#stateChanged(int)}. This AnchorPane will be removed by {@link PrimaryController}.
             * So no action is necessary here.
             */
            @Override
            public void onFail(ActionTask.CAUSE cause) {
                Logging.errorln("Modify logger UserName ... fail");
                postExecution();
            }
        }, userName);
    }

    /**
     * Method to initialize variables and status of this FX page.
     */
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

    /**
     * Set this FX page related logger entity.
     *
     * @param gpsLogger This FX page related logger entity.
     */
    public void setGpsLogger(GpsLogger gpsLogger) {
        this.gpsLogger = gpsLogger;
    }

    /**
     * Set user name to TextField {@link #ownerNameInput}.
     *
     * @param userName The user name.
     */
    public void setUserName(String userName) {
        ownerNameInput.setText(userName);
    }

    /**
     * Set RcdMethod to radio group.
     *
     * @param rcdMethod The RcdMethod.
     */
    public void setRcdMethod(int rcdMethod) {
        if (rcdMethod == 1) {
            rcdMethodOverlap.setSelected(true);
        } else {
            rcdMethodStop.setSelected(true);
        }
    }

    /**
     * Get RcdMethod from radio group.
     *
     * @return The RcdMethod.
     */
    public int getRcdMethod() {
        if (rcdMethodOverlap.isSelected()) {
            return 1;
        } else {
            return 2;
        }
    }

    /**
     * Set RcdBy to radio group.
     *
     * @param rcdBy The RcdBy.
     */
    public void setRecordBy(int rcdBy) {
        if (rcdBy == 0) {
            rcdBySec.setSelected(true);
        } else if (rcdBy == 1) {
            rcdByDist.setSelected(true);
        }
    }

    /**
     * Get RcdBy from radio group.
     *
     * @return The RcdBy.
     */
    public int getRecordBy() {
        if (rcdBySec.isSelected()) {
            return 0;
        } else {
            return 1;
        }
    }

    /**
     * Set BySec to ComboBox {@link #rcdBySecChooser}.
     *
     * @param bySec The BySec.
     */
    public void setRecordBySec(int bySec) {
        for (int i = 0; i < bySecList.size(); i++) {
            if (bySec == bySecList.get(i)) {
                rcdBySecChooser.getSelectionModel().select(i);
                return;
            }
        }
        Logging.errorln("Invalid BySec: %d", bySec);
    }

    /**
     * Get BySec from ComboBox {@link #bySecList}.
     *
     * @return The BySec.
     */
    public int getRecordBySec() {
        return rcdBySecChooser.getValue();
    }

    /**
     * Set ByDist to ComboBox {@link #rcdByDistChooser}.
     *
     * @param byDist The ByDist.
     */
    public void setRecordByDist(int byDist) {
        for (int i = 0; i < byDistList.size(); i++) {
            if (byDist == byDistList.get(i)) {
                rcdByDistChooser.getSelectionModel().select(i);
                return;
            }
        }
        Logging.errorln("Invalid ByDist: %d", byDist);
    }

    /**
     * Get ByDist from ComboBox {@link #rcdByDistChooser}.
     *
     * @return The ByDist.
     */
    public int getRecordByDist() {
        return rcdByDistChooser.getVisibleRowCount();
    }

    /**
     * Method to disable relevant components prior to action performed.
     */
    private void priorExecution() {
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
     * Method to enable relevant components post action performed.
     */
    private void postExecution() {
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
