package application;

import java.io.File;

import com.interactivemesh.jfx.importer.stl.StlMeshImporter;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
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
	}
	

	private static TriangleMesh loadSTL(File stlFile) throws Exception{
		if (stlFile == null) {
			throw new IllegalArgumentException("No .stl file loaded");
		}	
		StlMeshImporter stlImporter = new StlMeshImporter();
		stlImporter.read(stlFile);
		TriangleMesh triangleMesh = stlImporter.getImport();
		stlImporter.close();
		return triangleMesh;
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
	
	public void delete(Group root) throws Exception {
		root.getChildren().remove(this);
	}
	
	public String toString() {
		return "Model Created";
	}
	
}