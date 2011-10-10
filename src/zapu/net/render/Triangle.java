package zapu.net.render;

import java.awt.Color;

public class Triangle {
	private Vertex vertices[];
	
	public Triangle(Vertex a, Vertex b, Vertex c) {
		this();
		vertices[0] = new Vertex(a);
		vertices[1] = new Vertex(b);
		vertices[2] = new Vertex(c);
	}
	
	public Triangle() {
		vertices = new Vertex[3];
	}
	
	public Vertex getVertex(int num) {
		return vertices[num];
	}

	public void setVertex(int num, Vertex v) {
		vertices[num] = v;
	}
}
