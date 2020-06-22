package com.withwings;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSnackbar;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXTreeView;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.HBox;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.support.avtransport.callback.Pause;
import org.fourthline.cling.support.avtransport.callback.Play;
import org.fourthline.cling.support.avtransport.callback.SetAVTransportURI;
import org.fourthline.cling.support.avtransport.callback.Stop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HelloController {
    private static final Logger log = LoggerFactory.getLogger(HelloController.class);


    @FXML
    private JFXButton pauseButton;

    @FXML
    private JFXButton stopButton;

    @FXML
    private HBox hBox;

    @FXML
    private JFXTreeView<?> deviceContainer;

    @FXML
    private JFXTextField uriTextField;

    @FXML
    private JFXButton playButton;


    private JFXSnackbar snackbar;
    private volatile  UpnpServiceImpl upnpService;
    private volatile Service avService;
    private ExecutorService executorService;

    @FXML
    public void initialize() {
        executorService= Executors.newSingleThreadExecutor();
        snackbar = new JFXSnackbar(hBox);
        uriTextField.setText("http://vfx.mtime.cn/Video/2019/02/04/mp4/190204084208765161.mp4");
        startUpnpService();
    }

    private void startUpnpService() {
        executorService.submit(()->{
            upnpService =
                    new UpnpServiceImpl(new CustomRegistryListener(new ArrayList<Device>()));
            upnpService.getControlPoint().search();
        });
    }


    @FXML
    void play() {
        String uri = uriTextField.getText();
        executorService.submit(()->{
            upnpService.getControlPoint().execute(new SetAVTransportURI(avService,uri) {
                @Override
                public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                }

                @Override
                public void success(ActionInvocation invocation) {
                    upnpService.getControlPoint().execute(new Play(avService) {
                        @Override
                        public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                        }
                    });
                }
            });
        });

    }

    @FXML
    void pause() {
        executorService.submit(()->{
            upnpService.getControlPoint().execute(new Pause(avService) {
                @Override
                public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                }
            });
        });
    }

    @FXML
    void stop() {
        upnpService.getControlPoint().execute(new Stop(avService) {
            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {

            }
        });
    }


    public void sayHello() {
//        http://vfx.mtime.cn/Video/2019/02/04/mp4/190204084208765161.mp4
//        http://vfx.mtime.cn/Video/2019/03/21/mp4/190321153853126488.mp4
//        http://vfx.mtime.cn/Video/2019/03/19/mp4/190319222227698228.mp4
//        http://vfx.mtime.cn/Video/2019/03/19/mp4/190319212559089721.mp4
//        http://vfx.mtime.cn/Video/2019/03/18/mp4/190318231014076505.mp4
//        http://vfx.mtime.cn/Video/2019/03/18/mp4/190318214226685784.mp4
//        http://vfx.mtime.cn/Video/2019/03/19/mp4/190319104618910544.mp4
//        http://vfx.mtime.cn/Video/2019/03/19/mp4/190319125415785691.mp4
//        http://vfx.mtime.cn/Video/2019/03/17/mp4/190317150237409904.mp4
//        http://vfx.mtime.cn/Video/2019/03/14/mp4/190314223540373995.mp4
//        http://vfx.mtime.cn/Video/2019/03/14/mp4/190314102306987969.mp4
//        http://vfx.mtime.cn/Video/2019/03/13/mp4/190313094901111138.mp4
//        http://vfx.mtime.cn/Video/2019/03/12/mp4/190312143927981075.mp4
//        http://vfx.mtime.cn/Video/2019/03/12/mp4/190312083533415853.mp4
//        http://vfx.mtime.cn/Video/2019/03/09/mp4/190309153658147087.mp4

    }


    public class CustomRegistryListener extends DefaultRegistryListener {

        private List<Device> deviceList;

        public CustomRegistryListener(List<Device> deviceList) {
            this.deviceList = new ArrayList<Device>();
        }

        @Override
        public void deviceAdded(Registry registry, Device device) {
            super.deviceAdded(registry, device);
            deviceList.add(device);
            snackbar.enqueue(new JFXSnackbar.SnackbarEvent(new Label(String.format(
                    "Device added:%s", device.getDisplayString()
            ))));
            Platform.runLater(() -> drawDeviceTree(deviceList));
        }

        @Override
        public void deviceRemoved(Registry registry, Device device) {
            super.deviceRemoved(registry, device);
            deviceList.remove(device);
            snackbar.enqueue(new JFXSnackbar.SnackbarEvent(new Label(String.format(
                    "Device removed:%s", device.getDisplayString()
            ))));
            Platform.runLater(() -> drawDeviceTree(deviceList));

        }
    }

    private void drawDeviceTree(List<Device> devices) {

        deviceContainer.setShowRoot(false);
        TreeItem root = new TreeItem<>();
        deviceContainer.setRoot(root);
        for (Device device : devices) {
            TreeItem deviceTreeItem = new TreeItem<>();
            deviceTreeItem.setValue(device);
            deviceTreeItem.setGraphic(new Label(device.getDisplayString()));
            root.getChildren().add(deviceTreeItem);
            for (Service service : device.getServices()) {
                TreeItem<Service> serviceTreeItem = new TreeItem<>();
                serviceTreeItem.setValue(service);
                final JFXButton serviceButton = new JFXButton(service.getServiceId().getId());
                serviceTreeItem.setGraphic(serviceButton);
                serviceButton.setOnMouseClicked(e->{
                    this.avService=serviceTreeItem.getValue();
                });
                deviceTreeItem.getChildren().add(serviceTreeItem);
            }

        }

    }
}
