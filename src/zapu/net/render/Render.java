package zapu.net.render;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class Render {
	Triangle tris[];
	Matrix ViewMatrix;
	Matrix ProjectionMatrix;
	Matrix ModelMatrix;

	private double[] ZBuffer = new double[MainComponent.width * MainComponent.height]; 
		
	private double Zfar = 50;
	private double Znear = 1.0;
	
	private BufferedImage[] textures;
	
	private Vector3[] sceneLights;
	
	public Render() {
		try {
			textures = new BufferedImage[]{
				ImageIO.read(Render.class.getResource("/resources/fieldstone-c.jpg")),
				ImageIO.read(Render.class.getResource("/resources/fieldstone-n.jpg")),
			};
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		sceneLights = new Vector3[] {
				new Vector3(1,1.2,1)
		};
		
		Triangle[] predefTris = new Triangle[] {
			new Triangle(
					new Vertex(new Vector3(0,0,0), new Vector3(0,0,1), Color.BLUE, 0, 0),
					new Vertex(new Vector3(0,1,0), new Vector3(0,0,1), Color.WHITE, 0, 1),
					new Vertex(new Vector3(1,0,0), new Vector3(0,0,1), Color.WHITE, 1, 0)),
			
			new Triangle(
					new Vertex(new Vector3(0,1,0), new Vector3(0,0,1), Color.WHITE, 0, 1), 
					new Vertex(new Vector3(1,1,0), new Vector3(0,0,1), Color.WHITE, 1, 1), 
					new Vertex(new Vector3(1,0,0), new Vector3(0,0,1), Color.WHITE, 1, 0)),
					
					
			
			new Triangle(
					new Vertex(new Vector3(1,0,0), new Vector3(1,0,0), Color.CYAN, 0, 0), 
					new Vertex(new Vector3(1,1,0), new Vector3(1,0,0), Color.CYAN, 1, 0), 
					new Vertex(new Vector3(1,0,-1), new Vector3(1,0,0), Color.CYAN, 0, 1)),
					
			new Triangle(
					new Vertex(new Vector3(1,1,0), new Vector3(1,0,0), Color.ORANGE, 1, 0),
					new Vertex(new Vector3(1,1,-1), new Vector3(1,0,0), Color.ORANGE, 1, 1), 
					new Vertex(new Vector3(1,0,-1), new Vector3(1,0,0), Color.ORANGE, 0, 1)),
			
					
					
			new Triangle(
					new Vertex(new Vector3(0,0,0), new Vector3(-1,0,0), Color.GREEN, 0, 0), 
					new Vertex(new Vector3(0,1,0), new Vector3(-1,0,0), Color.GREEN, 1, 0), 
					new Vertex(new Vector3(0,0,-1), new Vector3(-1,0,0), Color.GREEN, 0, 1)),
					
			new Triangle(
					new Vertex(new Vector3(0,1,0), new Vector3(-1,0,0), Color.GREEN, 1, 0),
					new Vertex(new Vector3(0,1,-1), new Vector3(-1,0,0), Color.WHITE, 1, 1), 
					new Vertex(new Vector3(0,0,-1), new Vector3(-1,0,0), Color.GREEN, 0, 1)),
					
					
					
			new Triangle(
					new Vertex(new Vector3(0,0,-1), new Vector3(0,0,-1), Color.YELLOW, 0, 0), 
					new Vertex(new Vector3(0,1,-1), new Vector3(0,0,-1), Color.YELLOW, 1, 0), 
					new Vertex(new Vector3(1,0,-1), new Vector3(0,0,-1), Color.WHITE, 0, 1)),
					
			new Triangle(
					new Vertex(new Vector3(0,1,-1), new Vector3(0,0,-1), Color.YELLOW, 1, 0),
					new Vertex(new Vector3(1,1,-1), new Vector3(0,0,-1), Color.YELLOW, 1, 1), 
					new Vertex(new Vector3(1,0,-1), new Vector3(0,0,-1), Color.WHITE, 0, 1)),
					
					
			new Triangle(
					new Vertex(new Vector3(0,1,-1), new Vector3(0,1,0), Color.RED, 0, 1), 
					new Vertex(new Vector3(1,1,-1), new Vector3(0,1,0), Color.RED, 1, 1), 
					new Vertex(new Vector3(0,1,0), new Vector3(0,1,0), Color.RED, 0, 0)),
					
			new Triangle(
					new Vertex(new Vector3(0,1,0), new Vector3(0,1,0), Color.RED, 0, 0),
					new Vertex(new Vector3(1,1,0), new Vector3(0,1,0), Color.RED, 1, 0), 
					new Vertex(new Vector3(1,1,-1), new Vector3(0,1,0), Color.RED, 1, 1)),
		};
		
		ArrayList<Triangle> triList = new ArrayList<Triangle>();
		for(int i = 0; i < predefTris.length; i++) {
			Triangle newTri = new Triangle(predefTris[i]);
			for(int v = 0; v < 3; v++) {
				Vertex vtx = newTri.getVertex(v);
				vtx.position = vtx.position.add(new Vector3(2,0,1));
				newTri.setVertex(v, vtx);
			}
			triList.add(newTri);

			newTri = new Triangle(predefTris[i]);
			for(int v = 0; v < 3; v++) {
				Vertex vtx = newTri.getVertex(v);
				vtx.position = vtx.position.add(new Vector3(3,0,2));
				newTri.setVertex(v, vtx);
			}
			triList.add(newTri);
		}
		
		tris = triList.toArray(new Triangle[triList.size()]);

		
		double w = 1.0 / Math.tan(Math.toRadians(90) / 2);
		double h = 1.0 / Math.tan(Math.toRadians(90) / 2);
		double Q = Zfar / (Zfar - Znear);
		ProjectionMatrix = new Matrix(new double[][] {
				{w, 0, 0, 0},
				{0, h, 0, 0},
				{0, 0, Q, -Q * Znear},
				{0, 0, -1, 0}
		});
		
		/*ProjectionMatrix = new Matrix(new double[][] {
				{2, 0, 0, 0},
				{0, 2, 0, 0},
				{0, 0, -1.22, -2.22},
				{0, 0, -1, 0}
		});*/
		ProjectionMatrix.show();
		
		ModelMatrix = new Matrix(new double[][]
 		 				{
 		 					{1, 0, 0, -3},
 		 					{0, 1, 0, -1},
 		 					{0, 0, 1, 3},
 		 					{0, 0, 0, 1}
 		 				});
		
		ViewMatrix = new Matrix(new double[][]
		 				{
		 					{1, 0, 0, 0},
		 					{0, 1, 0, 0},
		 					{0, 0, 1, 0},
		 					{0, 0, 0, 1}
		 				});
	}
	
	private void AnglesToAxes(Vector3 euler, Matrix view) {
	    double sx, sy, sz, cx, cy, cz, theta;

	    // rotation angle about X-axis (pitch)
	    theta = Math.toRadians(euler.x());
	    sx = Math.sin(theta);
	    cx = Math.cos(theta);

	    // rotation angle about Y-axis (yaw)
	    theta = Math.toRadians(euler.y());
	    sy = Math.sin(theta);
	    cy = Math.cos(theta);

	    // rotation angle about Z-axis (roll)
	    theta = Math.toRadians(euler.z());
	    sz = Math.sin(theta);
	    cz = Math.cos(theta);

	    //left
	    view.set(0, 0, cy*cz);
	    view.set(1, 0, sx*sy*cz + cx*sz);
	    view.set(2, 0,  -cx*sy*cz + sx*sz);

	    //up
	    view.set(0, 1, -cy*sz);
	    view.set(1, 1, -sx*sy*sz + cx*cz);
	    view.set(2, 1,  cx*sy*sz + sx*cz);

	    //forward
	    view.set(0, 2, sy);
	    view.set(1, 2, -sx*cy);
	    view.set(2, 2, cx*cy);
	}
	
	private Vector3 RotateVector(Vector3 vec, double angle) {
		return new Vector3(vec.x() * Math.cos(Math.toRadians(angle)) - vec.y() * Math.sin(Math.toRadians(angle)),
				vec.x() * Math.sin(Math.toRadians(angle)) + vec.y() * Math.cos(Math.toRadians(angle)),
				vec.z());
	}
	
	private double viewHorizontal = 0;
	private double viewVertical = 0;
	
	private double rotateSpeed = 0.5;
	private double moveSpeed = 0.1;
	
	public void rotateView(int dx, int dy) {
		viewHorizontal += rotateSpeed * dx;
		viewVertical += rotateSpeed * dy;
		
		AnglesToAxes(new Vector3(viewVertical, viewHorizontal, 0), ViewMatrix);
	}
	
	public void moveView(int dx, int dy) {		
		Vector3 vec = new Vector3(ModelMatrix.value(0, 3), ModelMatrix.value(1, 3), ModelMatrix.value(2, 3));
		
		vec = vec.add(new Vector3(Math.cos(Math.toRadians(viewHorizontal)), 0, Math.sin(Math.toRadians(viewHorizontal))).mul(moveSpeed * dx));
		vec = vec.add(new Vector3(Math.sin(Math.toRadians(viewVertical)), Math.cos(Math.toRadians(viewVertical)), 0).mul(moveSpeed * dy));
		
		ModelMatrix.set(0, 3, vec.x());
		ModelMatrix.set(1, 3, vec.y());
		ModelMatrix.set(2, 3, vec.z());
	}
	
	double frame = 0;
	public void draw(Graphics g, int[] framebuffer) {
		
		for(int i = 0; i < MainComponent.width * MainComponent.height; i++)
			ZBuffer[i] = Zfar;
		
		//double rot = 60 * Math.sin(frame++ / 50);
		//ViewMatrix.set(0, 3, -1 + rot);
		//AnglesToAxes(new Vector3(0, rot, 0), ViewMatrix);
		
		Matrix screen_matrices[] = new Matrix[3];
		Vector3 screen_points[] = new Vector3[3];
		double screen_points_w[] = new double[3];
		
		Matrix modelViewMatrix = ViewMatrix.times(ModelMatrix);
		//Matrix normalMatrix = modelViewMatrix.solve(Matrix.identity(4)).transpose();
		Matrix normalMatrix = new Matrix(modelViewMatrix);
		
		Vector3[] lights = new Vector3[sceneLights.length];
		
		frame++;
		
		for(int i = 0; i < sceneLights.length; i++) {
			Matrix pointMatrix = Matrix.FromVector3(sceneLights[i]);
			pointMatrix = pointMatrix.plus(new Matrix(new double[][]{ {5 * Math.sin(frame / 20)}, {0 * Math.sin(frame / 20)}, {0 * Math.sin(frame / 20)}, {0} }));
			//pointMatrix = modelViewMatrix.times(pointMatrix);
			//pointMatrix = ProjectionMatrix.times(pointMatrix);
			
			lights[i] = pointMatrix.ToVector3ByW();
		}
		
		for(Triangle tri : tris) {
			boolean clipTri = false;
			
			for(int i = 0; i < 3; i++) {
				Vertex point = tri.getVertex(i);
				
				Matrix pointMatrix = new Matrix(
						new double[][] { { point.position.x() }, { point.position.y() } , { point.position.z() }, { 1 } });
				
				//Matrix eyeMatrix = ModelMatrix.times(pointMatrix);
				//eyeMatrix = ViewMatrix.times(eyeMatrix);
				Matrix eyeMatrix = modelViewMatrix.times(pointMatrix);
				Matrix clipMatrix = ProjectionMatrix.times(eyeMatrix);
				
				double w = clipMatrix.value(3, 0);
				double normalizedX = clipMatrix.value(0, 0) / w;
				double normalizedY = clipMatrix.value(1, 0) / w;
				double normalizedZ = clipMatrix.value(2, 0) / w;
				
				if(normalizedZ < -1 || normalizedZ > 1) {
					clipTri = true;
					break;
				}
			
				int screenX = (int) (MainComponent.width / 2 * normalizedX + MainComponent.width / 2);
				int screenY = (int) (MainComponent.height / 2 *  normalizedY + MainComponent.height / 2);
				double screenZ = (Zfar - Znear) / 2 * normalizedZ + (Zfar + Znear) / 2;

				screen_points[i] = new Vector3(screenX, screenY, screenZ);
				screen_points_w[i] = w;
				
				screen_matrices[i] = new Matrix(new double[][]{ {screenX, screenY, screenZ, w},
						{normalizedX, normalizedY, normalizedZ, w}});
			
			}
			
			if(clipTri)
				continue;
			
			TriFiller filler = new TriFiller(tri, screen_matrices, textures, lights, normalMatrix);
			filler.FillTriangle(g, framebuffer, ZBuffer);
		}
	}
	
	void postRender(Graphics g) {
		g.setColor(Color.WHITE);
		g.drawString("Fov: " + currentFov, 10, 10);
	}

	int currentFov = 90;
	public void zoomView(int wheelRotation) {
		currentFov = TriFiller.clamp(currentFov + wheelRotation, 30, 110);
		ProjectionMatrix.set(0, 0, 1.0 / Math.tan(Math.toRadians(currentFov) / 2));
		ProjectionMatrix.set(1, 1, 1.0 / Math.tan(Math.toRadians(currentFov) / 2));
	}
}
