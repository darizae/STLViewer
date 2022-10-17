package application;

import java.io.File;

import com.interactivemesh.jfx.importer.stl.StlMeshImporter;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.input.PickResult;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;

public class STL extends MeshView{
	
	private Translate t;
	private Rotate rx;
	private Rotate ry;
	private Rotate rz;
	private Scale s;
	
	//For rotation
	private double anchorX, anchorY;
	private double anchorAngleX = 0;
	private double anchorAngleY = 0;
	private DoubleProperty angleX = new SimpleDoubleProperty(0);
	private DoubleProperty angleY = new SimpleDoubleProperty(0);
	//
	
	private final double DRAG_SPEED = 1.8;
	
	public STL(File stlFile) throws Exception {
        super(loadSTL(stlFile));
        
		this.t = new Translate();
		this.rx = new Rotate(); this.rx.setAxis(Rotate.X_AXIS);
		this.ry = new Rotate(); this.ry.setAxis(Rotate.Y_AXIS);
		this.rz = new Rotate(); this.rz.setAxis(Rotate.Z_AXIS);
		this.s = new Scale();
		
		this.getTransforms().addAll(
				t, rx, ry, rz, s
		);
	}
	

	private static TriangleMesh loadSTL(File stlFile) throws Exception{
		StlMeshImporter stlImporter = new StlMeshImporter();
		stlImporter.read(stlFile);
		TriangleMesh triangleMesh = stlImporter.getImport();
		stlImporter.close();
		return triangleMesh;
	}
	
	/*
	 * Movement-related methods
	 * 1. Move Up/Down => Translation along Y-Axis
	 * 2. Move Left/Right => Translation along X-Axis
	 * 3. Move Forward/Back => Translation along Z-Axis
	 */
	public void moveUp(double movFactor) {this.t.setY(this.t.getY() - movFactor);}
	public void moveDown(double movFactor) {this.t.setY(this.t.getY() + movFactor);}
	
	public void moveLeft(double movFactor) {this.t.setX(this.t.getX() - movFactor);}
	public void moveRight(double movFactor) {this.t.setX(this.t.getX() + movFactor);}
	
	public void moveForward(double movFactor) {this.t.setZ(this.t.getZ() + movFactor);}
	public void moveBack(double movFactor) {this.t.setZ(this.t.getZ() - movFactor);}
	
	/*
	 * Rotation-related methods
	 * 1. 3D Rotation along X-Axis
	 * 2. 3D Rotation along Y-Axis
	 * 3. 3D Rotation along Z-Axis
	 */
	public void rotateX(double angle) {this.rx.setAngle(this.rx.getAngle() + angle);}
	public void rotateY(double angle) {this.ry.setAngle(this.ry.getAngle() + angle);}
	public void rotateZ(double angle) {this.rz.setAngle(this.rz.getAngle() + angle);}
	
	/*
	 * Scale-related methods
	 * 1. Increases overall size of DNC Structure
	 * 2. Decreases overall size of DNC Structure
	 */
	public void increaseSize(double scaleFactor) {
		this.s.setX(this.s.getX() + scaleFactor);
		this.s.setY(this.s.getY() + scaleFactor);
		this.s.setZ(this.s.getZ() + scaleFactor);
	}
	
	public void decreaseSize(double scaleFactor) {
		this.s.setX(this.s.getX() - scaleFactor);
		this.s.setY(this.s.getY() - scaleFactor);
		this.s.setZ(this.s.getZ() - scaleFactor);
	}
	
	public void makeRotable() {
		Rotate xRotate;
		Rotate yRotate;
		
		this.getTransforms().addAll(
				xRotate = new Rotate(0, Rotate.X_AXIS),
				yRotate = new Rotate(0, Rotate.Y_AXIS)
		);
		
		xRotate.angleProperty().bind(angleX);
		yRotate.angleProperty().bind(angleY);
		
		this.setOnMousePressed(event -> {
			anchorX = event.getSceneX();
			anchorY = event.getSceneY();
			anchorAngleX = angleX.get();
			anchorAngleY = angleY.get();
		});
		
		this.setOnMouseDragged(event -> {
			angleX.set(anchorAngleX - (anchorY - event.getSceneY()));
			angleY.set(anchorAngleY + (anchorX - event.getSceneX()));
		});
	}
	
	/*
	 * Function Name: delete
	 * 
	 * @param group (Group)
	 * 
	 * Inside the Function:
	 * 1. Removes 3D STL Model from root
	 */
	public void delete(Group root) throws Exception {
		root.getChildren().remove(this);
	}
	
	public String toString() {
		return "Model Created";
	}
	
}