package application;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.FileChooser;
import javafx.util.Duration;

public class Controller implements Initializable{
	
	@FXML
	private AnchorPane anchorPane;
	@FXML 
	private SubScene subScene;
	
	private FileChooser fileChooser;
	private File stlFile;
	private STL model;
	
	
	
	private static final int MOV_FACTOR = 5;
	private static final int ROT_FACTOR = 2;
	private static final double SCALE_FACTOR = 0.1;
	
    final Group root = new Group();
    final Xform world = new Xform();
    
    final PerspectiveCamera camera = new PerspectiveCamera(true);
    final Xform cameraXform = new Xform();
    final Xform cameraXform2 = new Xform();
    final Xform cameraXform3 = new Xform();
    final double cameraDistance = 450;
    
    final Group axisGroup = new Group();
    
    private Timeline timeline;
    boolean timelinePlaying = false;
    double ONE_FRAME = 1.0/24.0;
    double DELTA_MULTIPLIER = 200.0;
    double CONTROL_MULTIPLIER = 0.1;
    double SHIFT_MULTIPLIER = 0.1;
    double ALT_MULTIPLIER = 0.5;
        
    double mousePosX;
    double mousePosY;
    double mouseOldX;
    double mouseOldY;
    double mouseDeltaX;
    double mouseDeltaY;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		buildSubScene();
		buildCamera();
		buildAxes();
		
		handleKeyboard(subScene, root);
		handleMouse(subScene, root);
	}
	
	private void buildSubScene() {
		this.subScene.setRoot(root);
		this.subScene.setCamera(camera);
		root.getChildren().add(world);
	}
	
    final Xform stlGroup = new Xform();
	
	public void loadSTL() throws Exception {
		this.fileChooser = new FileChooser();
		this.fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("STL Files", "*.stl")
		);
		this.stlFile = fileChooser.showOpenDialog(anchorPane.getScene().getWindow());
		if (this.stlFile != null) {
			buildSTL();
		}
	}
	
	private void buildSTL() throws Exception {
		this.model = new STL(this.stlFile);
		this.stlGroup.getChildren().add(this.model);
		this.world.getChildren().add(stlGroup);
		this.stlGroup.setScale(0.5);
		this.stlGroup.setRotateZ(180);
	}
	
	
	private void buildCamera() {
		root.getChildren().add(cameraXform);
		cameraXform.getChildren().add(cameraXform2);
	    cameraXform2.getChildren().add(cameraXform3);
	    cameraXform3.getChildren().add(camera);
	    cameraXform3.setRotateZ(180.0);
	 
	    camera.setNearClip(0.1);
	    camera.setFarClip(10000.0);
	    camera.setTranslateZ(-cameraDistance);
	    cameraXform.ry.setAngle(320.0);
	    cameraXform.rx.setAngle(40);
	}
	

    private void buildAxes() {
        final PhongMaterial redMaterial = new PhongMaterial();
        redMaterial.setDiffuseColor(Color.DARKRED);
        redMaterial.setSpecularColor(Color.RED);
 
        final PhongMaterial greenMaterial = new PhongMaterial();
        greenMaterial.setDiffuseColor(Color.DARKGREEN);
        greenMaterial.setSpecularColor(Color.GREEN);
 
        final PhongMaterial blueMaterial = new PhongMaterial();
        blueMaterial.setDiffuseColor(Color.DARKBLUE);
        blueMaterial.setSpecularColor(Color.BLUE);
 
        final Box xAxis = new Box(700, 1, 1);
        final Box yAxis = new Box(1, 700, 1);
        final Box zAxis = new Box(1, 1, 700);
        
        xAxis.setMaterial(redMaterial);
        yAxis.setMaterial(greenMaterial);
        zAxis.setMaterial(blueMaterial);
 
        axisGroup.getChildren().addAll(xAxis, yAxis, zAxis);
        world.getChildren().addAll(axisGroup);
    }
	
//	/*
//	 * Function name: setAnimation
//	 * 
//	 * Inside the Function:
//	 * 1. Creates orbiting animation of camera around displayed objects (3D visualization)
//	 */
//	private void buildAnimation() {
//		timeline = new Timeline();
//		timeline.getKeyFrames().addAll(
//				new KeyFrame(Duration.seconds(0), new KeyValue(animationRot.angleProperty(), animationRot.getAngle())),
//				new KeyFrame(Duration.seconds(30), new KeyValue(animationRot.angleProperty(), animationRot.getAngle()+360))
//		);
//        timeline.setCycleCount(Timeline.INDEFINITE);
//        isPlaying = false;
//	}
//	
//	private void controlAnimation() {
//		if (isPlaying) {
//			timeline.pause(); isPlaying = false;
//		} else {
//			timeline.play(); 
//			isPlaying = true;
//		}
//	}
	
	private void handleMouse(SubScene subScene, final Node root) {
		subScene.setOnMousePressed(new EventHandler<MouseEvent>() {
	           @Override public void handle(MouseEvent me) {
	               mousePosX = me.getSceneX();
	               mousePosY = me.getSceneY();
	               mouseOldX = me.getSceneX();
	               mouseOldY = me.getSceneY();
	   			   subScene.requestFocus();
	           }
	       });
	       subScene.setOnMouseDragged(new EventHandler<MouseEvent>() {
	           @Override public void handle(MouseEvent me) {
	               mouseOldX = mousePosX;
	               mouseOldY = mousePosY;
	               mousePosX = me.getSceneX();
	               mousePosY = me.getSceneY();
	               mouseDeltaX = (mousePosX - mouseOldX); 
	               mouseDeltaY = (mousePosY - mouseOldY); 
	               
	               double modifier = 1.0;
	               double modifierFactor = 0.1;
	               
	               if (me.isControlDown()) {
	                   modifier = 0.1;
	               } 
	               if (me.isShiftDown()) {
	                   modifier = 10.0;
	               }     
	               if (me.isPrimaryButtonDown()) {
	                   cameraXform.ry.setAngle(cameraXform.ry.getAngle() - mouseDeltaX*modifierFactor*modifier*2.0);  // +
	                   cameraXform.rx.setAngle(cameraXform.rx.getAngle() + mouseDeltaY*modifierFactor*modifier*2.0);  // -
	               }

	               else if (me.isMiddleButtonDown()) {
	                   cameraXform2.t.setX(cameraXform2.t.getX() + mouseDeltaX*modifierFactor*modifier*0.3);  // -
	                   cameraXform2.t.setY(cameraXform2.t.getY() + mouseDeltaY*modifierFactor*modifier*0.3);  // -
	               }
	           }
	       });
	       
	        subScene.setOnScroll(new EventHandler<ScrollEvent>() {
				@Override
				public void handle(ScrollEvent event) {
					double delta = event.getDeltaY();
					double z = camera.getTranslateZ();
					if (delta == 0) {
						return;
					} else {
						double newZ = z + delta;
						camera.setTranslateZ(newZ);
					}
				}
			});
	   }
	   
	   private void handleKeyboard(SubScene subScene, final Node root) {
	       final boolean moveCamera = true;
	       subScene.setOnKeyPressed(new EventHandler<KeyEvent>() {
	           @Override
	           public void handle(KeyEvent event) {
	               Duration currentTime;
	               switch (event.getCode()) {
	                   case Z:
	                       if (event.isShiftDown()) {
	                           cameraXform.ry.setAngle(0.0);
	                           cameraXform.rx.setAngle(0.0);
	                           camera.setTranslateZ(-300.0);
	                       }   
	                       cameraXform2.t.setX(0.0);
	                       cameraXform2.t.setY(0.0);
	                       break;
	                   case X:
	                       if (event.isControlDown()) {
	                           if (axisGroup.isVisible()) {
	                               System.out.println("setVisible(false)");
	                               axisGroup.setVisible(false);
	                           }
	                           else {
	                               System.out.println("setVisible(true)");
	                               axisGroup.setVisible(true);
	                           }
	                       }   
	                       break;
//	                   case SPACE:
//	                       if (timelinePlaying) {
//	                           timeline.pause();
//	                           timelinePlaying = false;
//	                       }
//	                       else {
//	                           timeline.play();
//	                           timelinePlaying = true;
//	                       }
//	                       break;
	                   case UP:
	                       if (event.isControlDown() && event.isShiftDown()) {
	                           cameraXform2.t.setY(cameraXform2.t.getY() - 10.0*CONTROL_MULTIPLIER);  
	                       }  
	                       else if (event.isAltDown() && event.isShiftDown()) {
	                           cameraXform.rx.setAngle(cameraXform.rx.getAngle() - 10.0*ALT_MULTIPLIER);  
	                       }
	                       else if (event.isControlDown()) {
	                           cameraXform2.t.setY(cameraXform2.t.getY() - 1.0*CONTROL_MULTIPLIER);  
	                       }
	                       else if (event.isAltDown()) {
	                           cameraXform.rx.setAngle(cameraXform.rx.getAngle() - 2.0*ALT_MULTIPLIER);  
	                       }
	                       else if (event.isShiftDown()) {
	                           double z = camera.getTranslateZ();
	                           double newZ = z + 5.0*SHIFT_MULTIPLIER;
	                           camera.setTranslateZ(newZ);
	                       }
	                       break;
	                   case DOWN:
	                       if (event.isControlDown() && event.isShiftDown()) {
	                           cameraXform2.t.setY(cameraXform2.t.getY() + 10.0*CONTROL_MULTIPLIER);  
	                       }  
	                       else if (event.isAltDown() && event.isShiftDown()) {
	                           cameraXform.rx.setAngle(cameraXform.rx.getAngle() + 10.0*ALT_MULTIPLIER);  
	                       }
	                       else if (event.isControlDown()) {
	                           cameraXform2.t.setY(cameraXform2.t.getY() + 1.0*CONTROL_MULTIPLIER);  
	                       }
	                       else if (event.isAltDown()) {
	                           cameraXform.rx.setAngle(cameraXform.rx.getAngle() + 2.0*ALT_MULTIPLIER);  
	                       }
	                       else if (event.isShiftDown()) {
	                           double z = camera.getTranslateZ();
	                           double newZ = z - 5.0*SHIFT_MULTIPLIER;
	                           camera.setTranslateZ(newZ);
	                       }
	                       break;
	                   case RIGHT:
	                       if (event.isControlDown() && event.isShiftDown()) {
	                           cameraXform2.t.setX(cameraXform2.t.getX() + 10.0*CONTROL_MULTIPLIER);  
	                       }  
	                       else if (event.isAltDown() && event.isShiftDown()) {
	                           cameraXform.ry.setAngle(cameraXform.ry.getAngle() - 10.0*ALT_MULTIPLIER);  
	                       }
	                       else if (event.isControlDown()) {
	                           cameraXform2.t.setX(cameraXform2.t.getX() + 1.0*CONTROL_MULTIPLIER);  
	                       }
	                       else if (event.isAltDown()) {
	                           cameraXform.ry.setAngle(cameraXform.ry.getAngle() - 2.0*ALT_MULTIPLIER);  
	                       }
	                       break;
	                   case LEFT:
	                       if (event.isControlDown() && event.isShiftDown()) {
	                           cameraXform2.t.setX(cameraXform2.t.getX() - 10.0*CONTROL_MULTIPLIER);  
	                       }  
	                       else if (event.isAltDown() && event.isShiftDown()) {
	                           cameraXform.ry.setAngle(cameraXform.ry.getAngle() + 10.0*ALT_MULTIPLIER);  // -
	                       }
	                       else if (event.isControlDown()) {
	                           cameraXform2.t.setX(cameraXform2.t.getX() - 1.0*CONTROL_MULTIPLIER);  
	                       }
	                       else if (event.isAltDown()) {
	                           cameraXform.ry.setAngle(cameraXform.ry.getAngle() + 2.0*ALT_MULTIPLIER);  // -
	                       }
	                       break;
	                   
	                   case W: stlGroup.rx.setAngle(stlGroup.rx.getAngle() + ROT_FACTOR); break;
	                   case S: stlGroup.rx.setAngle(stlGroup.rx.getAngle() - ROT_FACTOR); break;
	                   case A: stlGroup.ry.setAngle(stlGroup.ry.getAngle() + ROT_FACTOR); break;
	                   case D: stlGroup.ry.setAngle(stlGroup.ry.getAngle() - ROT_FACTOR); break;
	                   case Q: stlGroup.rz.setAngle(stlGroup.rz.getAngle() + ROT_FACTOR); break;
	                   case E: stlGroup.rz.setAngle(stlGroup.rz.getAngle() - ROT_FACTOR); break;
	                   
	                   default: break;
	               }
	           }
	       });
	   }
	
}
