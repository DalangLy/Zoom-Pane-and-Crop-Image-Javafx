package crop.images;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class FXMLDocumentController implements Initializable {
    
    @FXML
    private Label label;
    @FXML
    private AnchorPane thisRootScene;
    
    @FXML
    private void handleButtonAction(ActionEvent event) {
        System.out.println("You clicked me!");
        label.setText("Hello World!");
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Image img = new Image("https://images.unsplash.com/photo-1546507318-dc206ad061c9?ixlib=rb-1.2.1&w=1000&q=80");
        ImageView imgView = new ImageView(img);
        
        imgView.setFitWidth(300);
        imgView.setFitHeight(300);
        imgView.setPreserveRatio(true);
        
        double size = img.getWidth()>img.getHeight()?img.getHeight():img.getWidth();
        double centerX = (img.getWidth()/2)-(size/2);
        double centerY = (img.getHeight()/2)-(size/2);
        imgView.setViewport(new Rectangle2D(centerX, centerY, size, size));
        
        
        StackPane container = new StackPane();
        container.setAlignment(Pos.CENTER);
//        container.prefWidthProperty().bind(imgView.fitWidthProperty());
//        container.prefHeightProperty().bind(imgView.fitHeightProperty());
        container.setPrefSize(306, 306);
        container.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(3))));
        container.setLayoutX(20);
        container.setLayoutY(20);
        container.getChildren().add(imgView);
        
        
        
        
        
        
        
        
        this.thisRootScene.getChildren().add(container);
    }   
}
