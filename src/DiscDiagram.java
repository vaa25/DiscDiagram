/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.PieChart.Data;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


public class DiscDiagram extends Application {
    public static Path homeDirectory = Paths.get("D:\\Program Files (x86)\\JetBrains\\IntelliJ IDEA Community Edition 14.1\\jre\\jre");
    private Branch currentBranch;
    private PieChart chart;
    private Stage primaryStage;
    private Scene scene;

    public static void main(String[] args) {
        Application.launch(DiscDiagram.class, args);
    }

    @Override
    public void start(Stage stage) {
        createNewBranch(homeDirectory);
        int width = 1000;
        int height = 800;
        primaryStage = stage;
        primaryStage.setTitle("Диаграмма диска");
        Group root = new Group();
        scene = new Scene(root, width + 100, height + 100, Color.BEIGE);
        primaryStage.setScene(scene);
        primaryStage.show();
        setGlobalListeners();
        chart = new PieChart();
        chart.setLayoutX(50);
        chart.setLayoutY(10);
        chart.setCursor(Cursor.CROSSHAIR);
        chart.setStyle("-fx-font:bold 14 Arial; -fx-text-fill:brown;");
        chart.setPrefSize(width, height);
        chart.setAnimated(true);
        chart.setTitle("Распределение " + currentBranch.getPath() + " по объемам");
        chart.setTitleSide(Side.TOP);
        chart.setLegendVisible(true);
        chart.setLegendSide(Side.RIGHT);
        chart.setClockwise(true);
        chart.setLabelsVisible(true);
        chart.setLabelLineLength(20);
        chart.setStartAngle(150);
        setNewBranchToChart(currentBranch);

        root.getChildren().addAll(chart);
        System.out.println();
    }
    private void createNewBranch(Path homeDirectory) {
        createNewBranch(homeDirectory, 0, null);
    }

    private void createNewBranch(Path homeDirectory, int id, Branch parent) {
        System.out.println("Создаю дерево каталогов...");
        long start = System.nanoTime();
        currentBranch = new Branch(homeDirectory, id, parent);
        ThreadManager.service.invoke(currentBranch);
        long finish = System.nanoTime();
        long duration = finish - start;
        System.out.println("Дерево каталогов создано за " + toString(duration));
    }

    private String toString(long duration) {
        String result;
        StringBuilder stringBuilder = new StringBuilder();
        long mc = duration / 1000000;
        long c = mc / 1000;
        long m = c / 60;
        mc = mc - c * 1000;
        c = c - m * 60;
        if (m > 0) {
            stringBuilder.append(m).append(" минут ");
        }
        if (c > 0) {
            stringBuilder.append(c).append(" секунд ");
        }
        if (mc > 0) {
            stringBuilder.append(mc).append(" миллисекунд");
        }
        result = stringBuilder.toString();
        return result;
    }

    private void setGlobalListeners() {
        scene.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.F5) {
                createNewBranch(currentBranch.getPath(), currentBranch.getId(), currentBranch.getParent());
                setNewBranchToChart(currentBranch);
            }
        });
        scene.setOnMouseClicked(event -> {
            if (isPrimary(event)) {
                PopupMenuManager.close();
            }
            if (event.isControlDown()) {
                if (currentBranch.getParent() != null) {
                    setNewBranchToChart(currentBranch.getParent());
                } else {
                    Path newPath = currentBranch.getPath().getParent();
                    if (newPath != null) {
                        homeDirectory = newPath;
                        createNewBranch(newPath);
                        setNewBranchToChart(currentBranch);
                    }
                }
            }
        });
    }


    private void addDataListeners() {
        for (final Data data : chart.getData()) {
            if (data.getPieValue() > 0.5) {
                data.getNode().setOnMouseClicked(event -> {
                    System.out.println("data currentBranch = " + currentBranch);
                    if (isDoubleClick(event)) {
                        Branch newCurrentBranch = currentBranch.getBranchByName(data.getName());
                        setNewBranchToChart(newCurrentBranch);
                    } else if (isPrimary(event)) {
                        Popup popup = new Popup();
                        popup.setAutoHide(true);
                        Label label = new Label(String.valueOf(data.getPieValue()).substring(0, 4) + "%");
                        label.setStyle("-fx-font: bold 20 Arial;-fx-text-fill:brown");
                        popup.getContent().addAll(label);
                        popup.setX(event.getScreenX());
                        popup.setY(event.getScreenY());
                        popup.show(primaryStage);
                    }
                    if (isSecondary(event)) {
                        PopupMenuManager.close();
                        Branch childBranch = currentBranch.getBranchByName(data.getName());
                        ContextMenu contextMenu = new ContextMenu();
                        MenuItem menuItemOpen = new MenuItem("Открыть в папке");
                        MenuItem menuItemDel = new MenuItem();
                        if (childBranch != null) {
                            menuItemDel.setText("Удалить '" + childBranch.getName() + "'");
                            menuItemDel.setOnAction(actionEvent -> {
                                if (AlertPromptDialog.show(primaryStage,
                                        "Действительно ли вы хотите удалить каталог \n" + data.getName().split("\\n")[0] + "\n co всем его содержимым ?")) {
                                    System.out.println("Удаляю '" + childBranch.getName() + "'");
                                    deleteBranchFromDisk(childBranch);
                                }
                            });
                            menuItemOpen.setOnAction(actionEvent -> {
                                try {
                                    Runtime.getRuntime().exec("explorer.exe " + childBranch.getPath());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });


                        } else {
                            menuItemDel.setText("Удалить файлы");
                            menuItemDel.setOnAction(actionEvent -> {
                                if (AlertPromptDialog.show(primaryStage,
                                        "Действительно ли вы хотите удалить все файлы из текущего каталога ?")) {
                                    System.out.println("Удаляю файлы");
                                    deleteBranchFilesFromDisk(currentBranch);
                                }
                            });
                            menuItemOpen.setOnAction(actionEvent -> {
                                try {
                                    Runtime.getRuntime().exec("explorer.exe " + currentBranch.getPath());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                        contextMenu.setHideOnEscape(true);
                        contextMenu.getItems().addAll(menuItemOpen, menuItemDel);
                        contextMenu.setAutoHide(true);
                        contextMenu.show(chart, event.getScreenX(), event.getScreenY());
                        contextMenu.focusedProperty().addListener((observable, oldValue, newValue) -> {
                            contextMenu.hide();
                            System.out.println("hide");
                        });
                        PopupMenuManager.register(contextMenu);
                        System.out.println(contextMenu.isFocused());
                    }
                });
            }
        }

    }

    private boolean isSecondary(MouseEvent event) {
        return event.getButton() == MouseButton.SECONDARY;
    }

    private boolean isDoubleClick(MouseEvent event) {
        return isPrimary(event) && event.getClickCount() == 2;
    }

    private boolean isPrimary(MouseEvent event) {
        System.out.println(event.getButton());
        return event.getButton() == MouseButton.PRIMARY;
    }

    private void deleteBranchFromDisk(Branch branch) {
        List<Branch> innerBranches = branch.getBranches();
        for (Branch innerBranch : innerBranches) {
            deleteBranchFromDisk(innerBranch);
        }
        deleteBranchFilesFromDisk(branch);
//        delete(branch.getPath());
    }

    private void deleteBranchFilesFromDisk(Branch branch) {
        Path path = branch.getPath();
        if (branch.getLeafsSize() > 0) {
            try {
                DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path, "*.*");
                for (Path pathToDelete : directoryStream) {
//                    if (delete(pathToDelete)){
                    System.out.println(pathToDelete + " удален");
//                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean delete(Path pathToDelete) {
        try {
            return Files.deleteIfExists(pathToDelete);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void setNewBranchToChart(Branch newCurrentBranch) {
        currentBranch = newCurrentBranch;
        chart.setData(getDataFromCurrentBranch());
        chart.setTitle("Распределение '" + currentBranch.getPath() + "' по объемам");
        addDataListeners();
    }

    private ObservableList<Data> getDataFromCurrentBranch() {
        ObservableList<Data> pieChartData = FXCollections.observableArrayList();
        long size = currentBranch.getSize();
        List<Branch> branches = currentBranch.getBranches();
        for (Branch branch : branches) {
            long branchSize = branch.getSize();
            Data data = generateData(size, branchSize, branch.getName());
            pieChartData.add(data);
        }
        if (currentBranch.getLeafsSize() > 0) {
            pieChartData.add(generateData(size, currentBranch.getLeafsSize(), "Различные файлы"));
        }
        return pieChartData;
    }

    private Data generateData(long size, long branchSize, String name) {
        int kB = 1024;
        int mB = kB * 1024;
        int gB = mB * 1024;
        String branchSize$ = String.valueOf(branchSize > gB ? branchSize / gB + " GB" : branchSize > mB ? branchSize / mB + " mB" : branchSize > kB ? branchSize / kB + " kB" : branchSize + " bytes");
        String label = name + "\n" + branchSize$;
        double value = 100.0 * branchSize / size;
        return new Data(label, value);
    }
}
