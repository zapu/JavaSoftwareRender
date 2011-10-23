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
	private BufferedImage[] textures;
	private Vector3[] lights;
	
	public TriFiller() {
		
	}
	
	public TriFiller(Triangle t, Matrix[] mtxs, BufferedImage[] tex, Vector3[] ls) {
		reset(t, mtxs, tex, ls);
	}
	
	public void reset(Triangle t, Matrix[] mtxs, BufferedImage[] tex, Vector3[] ls) {
		screenMatrices = mtxs;
		tri = t;
		textures = tex;
		lights = ls;
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
		//Przeciecie prostej y=y3 z prosta (x1,y1),(x2,y2)
		
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
		
		//Znalezienie wierzcholka "najwyzszego" (y najmniejsze) oraz "najnizszego" (y najwieksze)
		FindTopBottom(topMtxIndex, bottomMtxIndex);
		
		//Trzeci wierzcholek:
		index3 = (topMtxIndex.val + 1) % 3;
		if(index3 == bottomMtxIndex.val)
			index3 = (index3 + 1) % 3;
		
		topMtx = screenMatrices[topMtxIndex.val];
		bottomMtx = screenMatrices[bottomMtxIndex.val];
		mtx3 = screenMatrices[index3];
	
		//Znalezienie kierunku wypelniania
		boolean fillDirectionRight = FindFillDirection();
		
		//Znalezienie przestrzeni stycznej
		FindTangents();
		
		//Wypelnianie
		BrenFill(fillDirectionRight);
	}
	
	private Vector3[] lightVec;
	private Vector3[] halfVec;
	
	private void FindTangents() {
		Vertex vtx1 = tri.getVertex(topMtxIndex.val);
		Vertex vtx2 = tri.getVertex(bottomMtxIndex.val);
		Vertex vtx3 = tri.getVertex(index3);
		
		Vector3 edge1 = vtx2.position.sub(vtx1.position);
		Vector3 edge2 = vtx3.position.sub(vtx1.position);
		double edge1u = vtx2.U - vtx1.U;
		double edge1v = vtx2.V - vtx1.V;
		double edge2u = vtx3.U - vtx1.U;
		double edge2v = vtx3.V - vtx1.V;
		
		double cp = edge1v * edge2u - edge1u * edge2v;
		if(cp == 0.0)
			cp = 0.1;
		double mul = 1.0 / cp;
		
		//T,B,N dla trojkata
		Vector3 tangent = (edge1.mul(-edge2v).add(edge2.mul(edge1v))).mul(mul);
		Vector3 binormal = (edge1.mul(-edge2u).add(edge2.mul(edge1u))).mul(mul);
		Vector3 normal = vtx1.normal;
		
		tangent = tangent.normalize();
		binormal = binormal.normalize();
		
		Vertex vertices[] = new Vertex[]{vtx1, vtx2, vtx3};

		//LightVec i HalfVec dla kazdego wierzcholka
		lightVec = new Vector3[3];
		halfVec = new Vector3[3];
		
		for(int i = 0; i < 3; i++) {
			Vector3 vertexPos = vertices[i].position;
			
			Vector3 lightDir = lights[0].sub(vertexPos).normalize();
			lightVec[i] = new Vector3(
					lightDir.dot(tangent),
					lightDir.dot(binormal),
					lightDir.dot(normal)).normalize();
					
			vertexPos = vertexPos.normalize();
			
			halfVec[i] = lightDir.add(vertexPos).normalize();
			halfVec[i] = new Vector3(
					halfVec[i].dot(tangent),
					halfVec[i].dot(binormal),
					halfVec[i].dot(normal)).normalize();
		}
	}
	
	private void BrenFill(boolean fillRight) {
		//Algorytm bresenhama
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
	
	private double PerspectiveInterpolate(double val1, double val2, double val3, double b1, double b2, double b3, double w) {
		double w1 = topMtx.value(0, 3);
		double w2 = bottomMtx.value(0, 3);
		double w3 = mtx3.value(0, 3);
		
		return (b1 * (val1 / w1) + b2 * (val2 / w2) + b3 * (val3 / w3))/w;
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
		
		//Wspolrzedne barycentryczne
		double detT = ((y2 - y3) * (x1 - x3)) + ((x3 - x2) * (y1 - y3));
		double b1 = (((y2 - y3) * (x - x3)) + ((x3 - x2) * (y - y3))) / detT;
		double b2 = (((y3 - y1) * (x - x3)) + ((x1 - x3) * (y - y3))) / detT;
		double b3 = 1 - b1 - b2;
		
		//Sprawdzenie czy piksel jest w trojkacie
		if((b1 >= 0 && b1 <= 1) && (b2 >= 0 && b2 <= 1) && (b3 >= 0 && b3 <= 1)) {
			//Czy piksel jest na ekranie
			if(x < 0 || y < 0 || x >= MainComponent.width || y >= MainComponent.height) {
				return true;
			}
			
			double z1 = topMtx.value(0, 2);
			double z2 = bottomMtx.value(0, 2);
			double z3 = mtx3.value(0, 2);
			
			//Interpolacja wspolrzednej z
			double z = b1 * (1/z1) + b2 * (1/z2) + b3 * (1/z3);
			
			//Pozycja w framebufferze oraz zbufferze
			int bufferPos = x + y * MainComponent.width;
			
			//ztest
			if(z >= ZBuffer[bufferPos])
				return true;
									
			double w1 = topMtx.value(0, 3);
			double w2 = bottomMtx.value(0, 3);
			double w3 = mtx3.value(0, 3);
			
			double w = b1 * (1/w1) + b2 * (1/w2) + b3 * (1/w3);
			
			//Interpolacja kolorow
			double colorR = MathTools.Clamp(
					PerspectiveInterpolate(((double)vertex1.color.getRed() / 255), 
					((double)vertex2.color.getRed() / 255),
					((double)vertex3.color.getRed() / 255),
					b1, b2, b3, w),
					0.0, 1.0);
			double colorG = MathTools.Clamp(
					PerspectiveInterpolate(((double)vertex1.color.getGreen() / 255), 
							((double)vertex2.color.getGreen() / 255),
							((double)vertex3.color.getGreen() / 255),
							b1, b2, b3, w),
					0.0, 1.0);
			double colorB = MathTools.Clamp(
					PerspectiveInterpolate(((double)vertex1.color.getBlue() / 255), 
							((double)vertex2.color.getBlue() / 255),
							((double)vertex3.color.getBlue() / 255),
							b1, b2, b3, w),
					0.0, 1.0);
			
			Color colorColor = new Color((float)colorR, (float)colorG, (float)colorB);
			
			//Interpolacja UV
			double u = PerspectiveInterpolate(vertex1.U, vertex2.U, vertex3.U, b1, b2, b3, w);
			double v = PerspectiveInterpolate(vertex1.V, vertex2.V, vertex3.V, b1, b2, b3, w);
			
			//XY piksela w teksturze
			int texelX = MathTools.Clamp((int)(u * textures[0].getWidth()), 0, textures[0].getWidth()-1);
			int texelY = MathTools.Clamp((int)(v * textures[0].getHeight()), 0, textures[0].getHeight()-1);
			
			//Kolor teksela
			Color texColor = new Color(textures[0].getRGB(texelX, texelY));
			
			//Stosunek koloru teksela do koloru wierzcholka
			float weighto = .5f;
			
			//Kolor z normal mapy
			Color normalMapColor = new Color(textures[1].getRGB(texelX, texelY));
			//Wektor normalny w tanym punkcie
			Vector3 localNormal = new Vector3(
					2.0 * (normalMapColor.getRed() / 255.0) - 1.0,
					2.0 * (normalMapColor.getGreen() / 255.0) - 1.0,
					2.0 * (normalMapColor.getBlue() / 255.0) - 1.0
					).normalize();
			
			//Wlasnosci materialu
			double diffuseMaterial = 0.5;
			double diffuseLight = 0.5;
			
			//Interpolacja lightVector, halfVector i natezenia swiatla
			Vector3 lightVector = new Vector3(
					PerspectiveInterpolate(lightVec[0].x(), lightVec[1].x(), lightVec[2].x(), b1, b2, b3, w),
					PerspectiveInterpolate(lightVec[0].y(), lightVec[1].y(), lightVec[2].y(), b1, b2, b3, w),
					PerspectiveInterpolate(lightVec[0].z(), lightVec[1].z(), lightVec[2].z(), b1, b2, b3, w)).normalize();
			double lamberFactor = Math.max(localNormal.dot(lightVector), 0.0);
			
			Vector3 halfVector = new Vector3(
					PerspectiveInterpolate(halfVec[0].x(), halfVec[1].x(), halfVec[2].x(), b1, b2, b3, w),
					PerspectiveInterpolate(halfVec[0].y(), halfVec[1].y(), halfVec[2].y(), b1, b2, b3, w),
					PerspectiveInterpolate(halfVec[0].z(), halfVec[1].z(), halfVec[2].z(), b1, b2, b3, w)).normalize();
			double specularFactor = Math.max(localNormal.dot(halfVector), 0.0) * 0.1f;

			float diffuse = (float)MathTools.Clamp((diffuseMaterial * diffuseLight * lamberFactor),0.,1.0);
			
			//Wynikowy kolor piksela
			Color fragColor = new Color(
					(float)MathTools.Clamp(diffuse*MathTools.Mix((float)colorColor.getRed() / 255, (float)texColor.getRed() / 255, weighto) + specularFactor, 0, 1),
					(float)MathTools.Clamp(diffuse*MathTools.Mix((float)colorColor.getGreen() / 255, (float)texColor.getGreen() / 255, weighto) + specularFactor, 0, 1),
					(float)MathTools.Clamp(diffuse*MathTools.Mix((float)colorColor.getBlue() / 255, (float)texColor.getBlue() / 255, weighto) + specularFactor, 0, 1));

			framebuffer[bufferPos] = fragColor.getRGB();
			ZBuffer[bufferPos] = z;

			return true;
		} else {
			return false;
		}
	}
}
	
	
