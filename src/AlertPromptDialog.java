import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class AlertPromptDialog extends Stage {

    public static final boolean NO = false;
    public static final boolean YES = true;
    private static final int WIDTH_DEFAULT = 400;
    private static Label label;
    private static boolean result;
    private static Image img;
    private static List<Stage> openedDialogs = new ArrayList<>();

    private AlertPromptDialog() {
        setResizable(false);
        initModality(Modality.APPLICATION_MODAL);
//        initStyle(StageStyle.TRANSPARENT);

//        img = new Image(AlertPromptDialog.class.getResourceAsStream("/images/MYICON.png"));

        addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent ke) {
                if (ke.getCode() == KeyCode.ESCAPE) {
                    AlertPromptDialog.this.close();
                }
            }

        });

        label = new Label();
        label.setWrapText(true);
        label.setGraphicTextGap(20);

        Button ybutton = new Button("Yes");
        ybutton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                result = YES;
                AlertPromptDialog.this.close();
            }
        });

        Button nbutton = new Button("No");
        nbutton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                result = NO;
                AlertPromptDialog.this.close();
            }
        });

        BorderPane borderPane = new BorderPane();

        BorderPane dropShadowPane = new BorderPane();
        dropShadowPane.getStyleClass().add("content");
        dropShadowPane.setTop(label);

        HBox hbox = new HBox();
        hbox.setSpacing(15);
        hbox.setAlignment(Pos.CENTER);
        hbox.getChildren().add(ybutton);
        hbox.setAlignment(Pos.CENTER);
        hbox.getChildren().add(nbutton);

        dropShadowPane.setBottom(hbox);

        borderPane.setCenter(dropShadowPane);

        Scene scene = new Scene(borderPane);
//        scene.getStylesheets().add(getResource("alert.css").toExternalForm());
        scene.setFill(Color.TRANSPARENT);
        setScene(scene);
    }

    public static boolean show(Stage owner, String msg) {
        closeOpenedDialogs();
        Stage popup = new AlertPromptDialog();
        openedDialogs.add(popup);
        label.setText(msg);
        label.setGraphic(new ImageView(img));

        // calculate width of string
        final Text text = new Text(msg);
        text.snapshot(null, null);
        // + 40 because there is padding 10 left and right and there are 2 containers now
        // + 20 because there is text gap between icon and messge
        int width = (int) text.getLayoutBounds().getWidth() + 60;

//        if (width + img.getWidth() > WIDTH_DEFAULT) width = WIDTH_DEFAULT;

        int height = 120;

        popup.setWidth(width);
        popup.setHeight(height);

        // make sure this stage is centered on top of its owner
        popup.setX(owner.getX() + (owner.getWidth() / 2 - popup.getWidth() / 2));
        popup.setY(owner.getY() + (owner.getHeight() / 2 - popup.getHeight() / 2));

        popup.showAndWait();

        return result;
    }

    private static void closeOpenedDialogs() {
        for (Stage openedDialog : openedDialogs) {
            openedDialog.close();
        }
        openedDialogs.clear();
    }

}