package zapu.net.render;

import java.awt.Color;

public class Vertex {
	public Vector3 position;
	public Vector3 normal;
	public Color color;
	public double U;
	public double V;
	
	public Vertex(Vector3 pos, Vector3 n, Color c, double u, double v) {
		position = pos;
		normal = n;
		color = c;
		U = u;
		V = v;
	}
	
	public Vertex(Vertex v) {
		this(new Vector3(v.position), new Vector3(v.normal), v.color, v.U, v.V); 
	}

}
