package zapu.net.render;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Comparator;

public class TriFiller {
	public class MutableInt {
		public int val;
	}
	
	private Matrix screenMatrices[];
	private Triangle tri;
	private BufferedImage texture;
	private Vector3[] lights;
	private Matrix normalMatrix;
	
	public TriFiller(Triangle t, Matrix[] mtxs, BufferedImage tex, Vector3[] ls, Matrix normalMtx) {
		reset(t, mtxs, tex, ls, normalMtx);
	}
	
	public void reset(Triangle t, Matrix[] mtxs, BufferedImage tex, Vector3[] ls, Matrix normalMtx) {
		screenMatrices = mtxs;
		tri = t;
		texture = tex;
		lights = ls;
		normalMatrix = normalMtx;
	}
	
	private static double GetXComponentFromMatrix(Matrix m) {
		return m.value(0, 0);
	}
	
	private static double GetYComponentFromMatrix(Matrix m) {
		return m.value(0, 1);
	}
	
	private static class TopVectorComp implements Comparator<Matrix> {
		@Override
		public int compare(Matrix o1, Matrix o2) {
			return GetYComponentFromMatrix(o1) < GetYComponentFromMatrix(o2) ? 1 : 0;
		}
	}
	
	private void FindTopBottom(MutableInt topRef, MutableInt bottomRef) {
		TopVectorComp comp = new TopVectorComp();
		if(comp.compare(screenMatrices[0], screenMatrices[1]) > 0 && comp.compare(screenMatrices[0], screenMatrices[2]) > 0) {
			topRef.val = 0;
			if(comp.compare(screenMatrices[1], screenMatrices[2]) > 0) {
				bottomRef.val = 2;
			} else {
				bottomRef.val = 1;
			}
		} else if(comp.compare(screenMatrices[1], screenMatrices[2]) > 0) {
			topRef.val = 1;
			if(comp.compare(screenMatrices[0], screenMatrices[2]) > 0) {
				bottomRef.val = 2;
			} else {
				bottomRef.val = 0;
			}
		} else {
			topRef.val = 2;
			if(comp.compare(screenMatrices[0], screenMatrices[1]) > 0) {
				bottomRef.val = 1;
			} else {
				bottomRef.val = 0;
			}
		}
	}
	
	private Matrix topMtx;
	private Matrix bottomMtx;
	private Matrix mtx3;
	
	private boolean FindFillDirection() {
		double xsum = bottomMtx.value(0, 0) - topMtx.value(0, 0);
		if(xsum == 0) {
			return bottomMtx.value(0, 0) < mtx3.value(0, 0);
		}
		double a = (bottomMtx.value(0, 1) - topMtx.value(0, 1)) / xsum;
		if(a == 0)
			return false;
		double b = topMtx.value(0, 1) - a * topMtx.value(0, 0);
		
		double x = (mtx3.value(0, 1) - b) / a;
		return x < mtx3.value(0, 0); //fill right
	}

	private Graphics g;
	private int[] framebuffer;
	private double[] ZBuffer;
	
	private MutableInt topMtxIndex = new MutableInt();
	private MutableInt bottomMtxIndex = new MutableInt();
	private int index3;
	
	public void FillTriangle(Graphics g, int[] framebuffer, double[] ZBuffer) {
		this.g = g;
		this.framebuffer = framebuffer;
		this.ZBuffer = ZBuffer;
		
		FindTopBottom(topMtxIndex, bottomMtxIndex);
		
		index3 = (topMtxIndex.val + 1) % 3;
		if(index3 == bottomMtxIndex.val)
			index3 = (index3 + 1) % 3;
		
		topMtx = screenMatrices[topMtxIndex.val];
		bottomMtx = screenMatrices[bottomMtxIndex.val];
		mtx3 = screenMatrices[index3];
	
		boolean fillDirectionRight = FindFillDirection();
		
		FindTangents();
		BrenFill(fillDirectionRight);
	}
	
	private Matrix TangentMatrix;
	
	private void FindTangents() {
		Vertex vtx1 = tri.getVertex(topMtxIndex.val);
		Vertex vtx2 = tri.getVertex(bottomMtxIndex.val);
		Vertex vtx3 = tri.getVertex(index3);
		
		Vector3 normal = vtx1.position.cross(vtx2.position);
		double coef = 1 / (vtx1.U * vtx2.V - vtx2.U * vtx1.V);
		if(Double.isInfinite(coef)) {
			vtx2 = vtx3;
			
			normal = vtx1.position.cross(vtx2.position);
			coef = 1 / (vtx1.U * vtx2.V - vtx2.U * vtx1.V);
		}
		
		Vector3 tangent = vtx1.position.mul(vtx2.V).add(vtx2.position.mul(-vtx1.V)).mul(coef);
		Vector3 binormal = normal.cross(tangent);
		System.out.println(normal + " " + tangent + " " + binormal);
		
		
		
		TangentMatrix = new Matrix(new double[][] {
				{normal.xyz[0], normal.xyz[1], normal.xyz[2]},
				{binormal.xyz[0], binormal.xyz[1], binormal.xyz[2]},
				{tangent.xyz[0], tangent.xyz[1], tangent.xyz[2]},
			});
		
		/*Vector3 e21 = vtx2.position.sub(vtx1.position);
		Vector3 e31 = vtx3.position.sub(vtx1.position);
		double u21 = vtx2.U - vtx1.U;
		double v21 = vtx2.V - vtx1.V;
		
		double u31 = vtx3.U - vtx1.U;
		double v31 = vtx3.V - vtx1.V;
		
		Vector3 T;
		if(u21 != 0) {
			T = new Vector3(e21).div(u21);
		} else {
			T = new Vector3(e31).div(u31);
		}
		
		T = T.normalize();
		Vector3 N = vtx1.normal.normalize();
		Vector3 BN = N.cross(T);
		System.out.println(T.toString() + " " + N + " " + BN);*/
		
	}
	
	private void BrenFill(boolean fillRight) {
		int x0 = (int)GetXComponentFromMatrix(topMtx);
		int y0 = (int)GetYComponentFromMatrix(topMtx);
		int x1 = (int)GetXComponentFromMatrix(bottomMtx);
		int y1 = (int)GetYComponentFromMatrix(bottomMtx);
		int tmp;
		
		boolean steep = Math.abs(y1 - y0) > Math.abs(x1 - x0);
		
		if(steep) {
			//swap x0 y0
			tmp = x0;
			x0 = y0;
			y0 = tmp;
			//swap x1 y1
			tmp = x1;
			x1 = y1;
			y1 = tmp;
		}
		if(x0 > x1) {
			//swap x0 x1
			tmp = x0;
			x0 = x1;
			x1 = tmp;
			//swap y0 y1
			tmp = y0;
			y0 = y1;
			y1 = tmp;
		}
		
		int deltaX = x1 - x0;
		int deltaY = Math.abs(y1 - y0);
		int error = deltaX / 2;
		int ystep;
		int y = y0;
		if(y0 < y1) {
			ystep = 1;
		} else {
			ystep = -1;
		}
		
		for(int x = x0; x <= x1; x++) {
			if(steep) {
				g.drawLine(y, x, y, x);
				drawScanline(y, x, fillRight);	
			} else {
				g.drawLine(x, y, x, y);
				drawScanline(x, y, fillRight);
			}
			
			error = error - deltaY;
			if(error < 0) {
				y += ystep;
				error += deltaX;
			}
		}
	}
	
	private void drawScanline(int x, int y, boolean directionRight) {		
		boolean first = true; 
		for(;;) {			
			if(!drawPixel(x, y)) {
				if(!first)
					break;
			}
			
			if(directionRight)
				x++;
			else
				x--;
			
			first = false;
		}
	}
	
	public static double clamp(double val, double min, double max) {
		if(val > max)
			return max;
		else if(val < min)
			return min;
		else
			return val;
	}
	
	public static int clamp(int val, int min, int max) {
		if(val > max)
			return max;
		else if(val < min)
			return min;
		else
			return val;
	}
	
	public static float mix(float x, float y, float w) {
		return x * (1 - w) + y * w;
	}
	
	private boolean drawPixel(int x, int y) {		
		int x1 = (int)GetXComponentFromMatrix(topMtx);
		int y1 = (int)GetYComponentFromMatrix(topMtx);
		int x2 = (int)GetXComponentFromMatrix(bottomMtx);
		int y2 = (int)GetYComponentFromMatrix(bottomMtx);
		int x3 = (int)GetXComponentFromMatrix(mtx3);
		int y3 = (int)GetYComponentFromMatrix(mtx3);
		
		Vertex vertex1 = tri.getVertex(topMtxIndex.val);
		Vertex vertex2 = tri.getVertex(bottomMtxIndex.val);
		Vertex vertex3 = tri.getVertex(index3);
		
		double detT = ((y2 - y3) * (x1 - x3)) + ((x3 - x2) * (y1 - y3));
		double b1 = (((y2 - y3) * (x - x3)) + ((x3 - x2) * (y - y3))) / detT;
		double b2 = (((y3 - y1) * (x - x3)) + ((x1 - x3) * (y - y3))) / detT;
		double b3 = 1 - b1 - b2;
		
		if((b1 >= 0 && b1 <= 1) && (b2 >= 0 && b2 <= 1) && (b3 >= 0 && b3 <= 1)) {
			if(x < 0 || y < 0 || x >= 800 || y >= 600) {
				return true;
			}
			
			double z1 = topMtx.value(0, 2);
			double z2 = bottomMtx.value(0, 2);
			double z3 = mtx3.value(0, 2);
			
			double z = b1 * (1/z1) + b2 * (1/z2) + b3 * (1/z3);
			//z = 1 / z;
			
			int bufferPos = x + y * 800;
			
			if(z >= ZBuffer[bufferPos])
				return true;
			
			double colorR = clamp(
					b1 * ((double)vertex1.color.getRed() / 255) + 
					b2 * ((double)vertex2.color.getRed() / 255) + 
					b3 * ((double)vertex3.color.getRed() / 255), 
					0.0, 1.0);
			double colorG = clamp(
					b1 * ((double)vertex1.color.getGreen() / 255) + 
					b2 * ((double)vertex2.color.getGreen() / 255) + 
					b3 * ((double)vertex3.color.getGreen() / 255), 
					0.0, 1.0);
			double colorB = clamp(
					b1 * ((double)vertex1.color.getBlue() / 255) + 
					b2 * ((double)vertex2.color.getBlue() / 255) + 
					b3 * ((double)vertex3.color.getBlue() / 255), 
					0.0, 1.0);
			
			double w1 = topMtx.value(0, 3);
			double w2 = bottomMtx.value(0, 3);
			double w3 = mtx3.value(0, 3);
			
			double w = b1 * (1/w1) + b2 * (1/w2) + b3 * (1/w3);
			double u = (b1 * (vertex1.U / w1) + b2 * (vertex2.U / w2) + b3 * (vertex3.U / w3))/w;
			double v = (b1 * (vertex1.V / w1) + b2 * (vertex2.V / w2) + b3 * (vertex3.V / w3))/w;
			
			int texelX = clamp((int)(u * texture.getWidth()), 0, texture.getWidth()-1);
			int texelY = clamp((int)(v * texture.getHeight()), 0, texture.getHeight()-1);
						
			Color colorColor = new Color((float)colorR, (float)colorG, (float)colorB);
			Color texColor = new Color(texture.getRGB(texelX, texelY));
			float weighto = 0.7f;
			
			Color fragColor = new Color(
					mix((float)colorColor.getRed() / 255, (float)texColor.getRed() / 255, weighto),
					mix((float)colorColor.getGreen() / 255, (float)texColor.getGreen() / 255, weighto),
					mix((float)colorColor.getBlue() / 255, (float)texColor.getBlue() / 255, weighto));

			framebuffer[bufferPos] = fragColor.getRGB();
			ZBuffer[bufferPos] = z;

			return true;
		} else {
			return false;
		}
	}
}
	
	
