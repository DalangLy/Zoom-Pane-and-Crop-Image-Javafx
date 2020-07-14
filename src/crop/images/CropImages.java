package crop.images;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

public class CropImages extends Application {
    
    private static final String IMAGE_URL = "https://www.nasa.gov/sites/default/files/thumbnails/image/global-mosaic-of-pluto-in-true-color.jpg";
    private static final int MIN_PIXELS = 10;
    Stage primaryStage;
    
    @Override
    public void start(Stage stage) throws IOException {
//        Parent root = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));
//        
//        Scene scene = new Scene(root);
//        
//        stage.setScene(scene);
//        stage.show();

        this.primaryStage = stage;
    
        Image image = new Image(IMAGE_URL);
        double width = image.getWidth();
        double height = image.getHeight();

        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(300);
        imageView.setFitHeight(300);
        reset(imageView, width, height);

        ObjectProperty<Point2D> mouseDown = new SimpleObjectProperty<>();

        imageView.setOnMousePressed(e -> {
            
            Point2D mousePress = imageViewToImage(imageView, new Point2D(e.getX(), e.getY()));
            mouseDown.set(mousePress);
        });

        imageView.setOnMouseDragged(e -> {
            Point2D dragPoint = imageViewToImage(imageView, new Point2D(e.getX(), e.getY()));
            shift(imageView, dragPoint.subtract(mouseDown.get()));
            mouseDown.set(imageViewToImage(imageView, new Point2D(e.getX(), e.getY())));
        });

        imageView.setOnScroll(e -> {
            double delta = e.getDeltaY();
            Rectangle2D viewport = imageView.getViewport();

            
            double Max_Pixel = this.getLargestImageDimension(width, height);//not allow to zoom out of image longest dimension
            double scale = clamp(Math.pow(1.01, delta),

                // don't scale so we're zoomed in to fewer than MIN_PIXELS in any direction:
                Math.min(MIN_PIXELS / viewport.getWidth(), MIN_PIXELS / viewport.getHeight()),

                // don't scale so that we're bigger than image dimensions:
                //Math.max(width / viewport.getWidth(), height / viewport.getHeight())
                Math.max(Max_Pixel / viewport.getWidth(), Max_Pixel / viewport.getHeight())

            );

            Point2D mouse = imageViewToImage(imageView, new Point2D(e.getX(), e.getY()));

            
            
            double newWidth = viewport.getWidth() * scale;
            double newHeight = viewport.getHeight() * scale;

            // To keep the visual point under the mouse from moving, we need
            // (x - newViewportMinX) / (x - currentViewportMinX) = scale
            // where x is the mouse X coordinate in the image

            // solving this for newViewportMinX gives

            // newViewportMinX = x - (x - currentViewportMinX) * scale 

            // we then clamp this value so the image never scrolls out
            // of the imageview:

            double newMinX = clamp(mouse.getX() - (mouse.getX() - viewport.getMinX()) * scale, 
                    0, width - newWidth);
            double newMinY = clamp(mouse.getY() - (mouse.getY() - viewport.getMinY()) * scale, 
                    0, height - newHeight);

            
            imageView.setViewport(new Rectangle2D(newMinX, newMinY, newWidth, newHeight));
        });

            imageView.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) {
                reset(imageView, width, height);
            }
        });

            
        //buttons
        HBox buttons = createButtons(width, height, imageView);
        Tooltip tooltip = new Tooltip("Scroll to zoom, drag to pan");
        Tooltip.install(buttons, tooltip);

        
        
        
        //image container
        StackPane imageFrame = new StackPane(imageView);
        imageFrame.setAlignment(Pos.CENTER);
        imageFrame.setPrefSize(306, 306);
        imageFrame.setMaxSize(306, 306);
        imageFrame.setBorder(new Border(new BorderStroke(Color.BLUE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(3))));
        
        
        
        
        
        
        
        //layout
        VBox root = new VBox(imageFrame, buttons);
        root.setAlignment(Pos.CENTER);
        

        
        
        
        
        
        
        stage.setScene(new Scene(root));
        stage.setTitle("Zoom and Pane");
        stage.setWidth(1000);
        stage.setHeight(900);
        stage.show();
    }
    
    private double getLargestImageDimension(double imageWidth, double imageHeight){
        return imageWidth>imageHeight?imageHeight:imageWidth;
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    private HBox createButtons(double width, double height, ImageView imageView) {
        Button reset = new Button("Reset");
        reset.setOnAction(e -> reset(imageView, width, height));
        Button btnCrop = new Button("Crop");
        btnCrop.setOnAction(e -> {
            Bounds selectionBounds = imageView.getBoundsInParent();

            // show bounds info
            System.out.println( "Selected area: " + selectionBounds);

            // crop the image
            crop(imageView, selectionBounds);
        });
        HBox buttons = new HBox(10, btnCrop, reset);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(10));
        return buttons;
    }
    
    
    
    //crop image
    private void crop(ImageView imageView, Bounds bounds) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("image(*.jpg)", "*.jpg"));
        fileChooser.setTitle("Save Image");

        File file = fileChooser.showSaveDialog( primaryStage);
        if (file == null)
            return;

        int width = (int) bounds.getWidth();
        int height = (int) bounds.getHeight();

        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        parameters.setViewport(new Rectangle2D( bounds.getMinX(), bounds.getMinY(), width, height));

        WritableImage wi = new WritableImage( width, height);
        imageView.snapshot(parameters, wi);

        // save image 
        // !!! has bug because of transparency (use approach below) !!!
        // --------------------------------
//        try {
//          ImageIO.write(SwingFXUtils.fromFXImage( wi, null), "jpg", file);
//      } catch (IOException e) {
//          e.printStackTrace();
//      }


        // save image (without alpha)
        // --------------------------------
        BufferedImage bufImageARGB = SwingFXUtils.fromFXImage(wi, null);
        BufferedImage bufImageRGB = new BufferedImage(bufImageARGB.getWidth(), bufImageARGB.getHeight(), BufferedImage.OPAQUE);

        Graphics2D graphics = bufImageRGB.createGraphics();
        graphics.drawImage(bufImageARGB, 0, 0, null);

        try {

            ImageIO.write(bufImageRGB, "jpg", file); 

            System.out.println( "Image saved to " + file.getAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
        }

        graphics.dispose();

    }
    
    
    

    // reset to the top left:
    private void reset(ImageView imageView, double width, double height) {
        double size = this.getLargestImageDimension(width, height);
        double centerX = (width/2)-(size/2);
        double centerY = (height/2)-(size/2);
        imageView.setViewport(new Rectangle2D(centerX, centerY, size, size));
        //imageView.setViewport(new Rectangle2D(0, 0, width, height));
    }

    // shift the viewport of the imageView by the specified delta, clamping so
    // the viewport does not move off the actual image:
    private void shift(ImageView imageView, Point2D delta) {
        Rectangle2D viewport = imageView.getViewport();

        double width = imageView.getImage().getWidth() ;
        double height = imageView.getImage().getHeight() ;

        double maxX = width - viewport.getWidth();
        double maxY = height - viewport.getHeight();
        
        double minX = clamp(viewport.getMinX() - delta.getX(), 0, maxX);
        double minY = clamp(viewport.getMinY() - delta.getY(), 0, maxY);

        imageView.setViewport(new Rectangle2D(minX, minY, viewport.getWidth(), viewport.getHeight()));
    }

    private double clamp(double value, double min, double max) {

        if (value < min)
            return min;
        if (value > max)
            return max;
        return value;
    }

    // convert mouse coordinates in the imageView to coordinates in the actual image:
    private Point2D imageViewToImage(ImageView imageView, Point2D imageViewCoordinates) {
        double xProportion = imageViewCoordinates.getX() / imageView.getBoundsInLocal().getWidth();
        double yProportion = imageViewCoordinates.getY() / imageView.getBoundsInLocal().getHeight();

        Rectangle2D viewport = imageView.getViewport();
        return new Point2D(
                viewport.getMinX() + xProportion * viewport.getWidth(), 
                viewport.getMinY() + yProportion * viewport.getHeight());
    }

}
