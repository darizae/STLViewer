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
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyEvent;
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
	
	private Group root;
	
	private PerspectiveCamera camera;
	private Rotate xRotate = new Rotate(0, Rotate.X_AXIS);
	private Rotate yRotate = new Rotate(0, Rotate.Y_AXIS);
	private Rotate zRotate = new Rotate(0, Rotate.Z_AXIS);
	private Translate pivot = new Translate();
	private Rotate animationRot;
	private Boolean is3D;
	
	private Box xAxis;
	private Box yAxis;
	private Box zAxis;
	Boolean axisVisible;
	
	private Timeline timeline;
	private Boolean isPlaying;
	
	private static final int MOV_FACTOR = 5;
	private static final int ROT_FACTOR = 2;
	private static final double SCALE_FACTOR = 0.1;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		this.root = new Group();
		
		buildCamera();
		set3DView();
		buildSubScene();
		buildAxis();
		addAxis();
		setAnimation();
		
		initSubSceneControls();
	}
	
	private void buildSubScene() {
		this.subScene.setRoot(root);
		this.subScene.setCamera(camera);
		this.subScene.setOnMousePressed(e -> {
	          subScene.requestFocus();
			});
	}
	
	public void loadSTL() throws Exception {
		this.fileChooser = new FileChooser();
		this.fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("STL Files", "*.stl")
		);
		this.stlFile = fileChooser.showOpenDialog(anchorPane.getScene().getWindow());
		this.model = new STL(this.stlFile);
		this.root.getChildren().add(this.model);
		this.model.makeRotable();
		initSTLControls(this.model);
	}
	
	public void set2DView() {
		this.camera.getTransforms().clear();
		this.camera.getTransforms().add(new Translate(0,0,-300));
		is3D = false;
	}
	
	public void set3DView() {
		setCameraTransforms();
		this.camera.getTransforms().addAll(
                new Rotate(-20, Rotate.X_AXIS),
                new Translate(0,0,-500)
		);
		this.yRotate.setAngle(-44);
		is3D = true;
	}
	
	private void buildCamera() {
		this.animationRot = new Rotate(0, Rotate.Y_AXIS);
		this.camera = new PerspectiveCamera(true);
		this.camera.setNearClip(0.1);
		this.camera.setFarClip(10000);
		setCameraTransforms();
	}
	
	private void setCameraTransforms(){
		this.camera.getTransforms().clear();
		this.camera.getTransforms().addAll (
        		pivot,
        		animationRot,
                yRotate,
                xRotate,
                zRotate
        );
	}
	
	private void initSTLControls(STL model) {
		this.subScene.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				switch(event.getCode()) {
				//Move Object Along Axis
				case NUMPAD5: model.moveUp(MOV_FACTOR); break;
				case NUMPAD2: model.moveDown(MOV_FACTOR); break;
				case NUMPAD1: model.moveLeft(MOV_FACTOR); break;
				case NUMPAD3: model.moveRight(MOV_FACTOR); break;
				case NUMPAD4: model.moveForward(MOV_FACTOR);; break;
				case NUMPAD6: model.moveBack(MOV_FACTOR); break;
				
				//Change Scale
				case K: model.increaseSize(SCALE_FACTOR); break;
				case L: model.decreaseSize(SCALE_FACTOR); break;
				
				//Delete STL Model
				case DELETE: try {model.delete(root); stlFile = null;} catch (Exception e) {e.printStackTrace();} break;
				
				default: break;
				}
			}
		});
	}
	
	public void upCam() {xRotate.setAngle(xRotate.getAngle() - ROT_FACTOR);}
	public void downCam() {xRotate.setAngle(xRotate.getAngle() + ROT_FACTOR);}
	public void leftCam() {yRotate.setAngle(yRotate.getAngle() + ROT_FACTOR);}
	public void rightCam() {yRotate.setAngle(yRotate.getAngle() - ROT_FACTOR);}
	public void forwardCam() {zRotate.setAngle(zRotate.getAngle() - ROT_FACTOR);}
	public void backCam() {zRotate.setAngle(zRotate.getAngle() + ROT_FACTOR);}
	public void centerCam() {
		if (is3D) {
			set3DView();
		} else {
			set2DView();
		}
	}
	
	private void initSubSceneControls() {
		this.subScene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
			switch(event.getCode()) {
			case W: upCam(); break;
			case S: downCam(); break;
			case A: leftCam(); break;
			case D: rightCam(); break;
			case Q: forwardCam(); break;
			case E: backCam(); break;
			
			case N: if (axisVisible) {removeAxis();} else{addAxis();} break;
			
			case SPACE: controlAnimation(); break;
			
			default: break;
			}
		});
		
		this.subScene.setOnScroll(new EventHandler<ScrollEvent>() {
			@Override
			public void handle(ScrollEvent event) {
				event.consume();
				double delta = event.getDeltaY();
				if (delta == 0) {
					return;
				} else {
					camera.getTransforms().add(new Translate(0,0,delta)); 
				}
			}
		});
	}
	
	/*
	 * Function name: buildAxis
	 * 
	 * Inside the Function:
	 * 1. Sets colors for axis (x->red, y->green, z->blue)
	 * 2. Creates new boxes, that are to serve as axis, with corresponding dimensions
	 * 
	 */
	private void buildAxis() {

        final PhongMaterial redMaterial = new PhongMaterial();
        redMaterial.setDiffuseColor(Color.DARKRED);
        redMaterial.setSpecularColor(Color.RED);
 
        final PhongMaterial greenMaterial = new PhongMaterial();
        greenMaterial.setDiffuseColor(Color.DARKGREEN);
        greenMaterial.setSpecularColor(Color.GREEN);
 
        final PhongMaterial blueMaterial = new PhongMaterial();
        blueMaterial.setDiffuseColor(Color.DARKBLUE);
        blueMaterial.setSpecularColor(Color.BLUE);
        
        xAxis = new Box(700, 1, 1);
        yAxis = new Box(1, 700, 1);
        zAxis = new Box(1, 1, 700);
        
        xAxis.setMaterial(redMaterial);
        yAxis.setMaterial(greenMaterial);
        zAxis.setMaterial(blueMaterial);
		
	}
	
	/*
	 * Function name: addAxis
	 * 
	 * Inside the Function:
	 * 1. Makes axis visible
	 */
	private void addAxis() {
		this.root.getChildren().addAll(xAxis, yAxis, zAxis);
		axisVisible = true;
	}
	
	/*
	 * Function name: removeAxis
	 * 
	 * Inside the Function:
	 * 1. Makes axis invisible
	 */
	private void removeAxis() {
		this.root.getChildren().remove(xAxis);
		this.root.getChildren().remove(yAxis);
		this.root.getChildren().remove(zAxis);
		axisVisible = false;
	}
	
	/*
	 * Function name: setAnimation
	 * 
	 * Inside the Function:
	 * 1. Creates orbiting animation of camera around displayed objects (3D visualization)
	 */
	private void setAnimation() {
		timeline = new Timeline();
		timeline.getKeyFrames().addAll(
				new KeyFrame(Duration.seconds(0), new KeyValue(animationRot.angleProperty(), animationRot.getAngle())),
				new KeyFrame(Duration.seconds(30), new KeyValue(animationRot.angleProperty(), animationRot.getAngle()+360))
		);
        timeline.setCycleCount(Timeline.INDEFINITE);
        isPlaying = false;
	}
	
	private void controlAnimation() {
		if (isPlaying) {
			timeline.pause(); isPlaying = false;
		} else {
			timeline.play(); 
			isPlaying = true;
		}
	}
	
}
