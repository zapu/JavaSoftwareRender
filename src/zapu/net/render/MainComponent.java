package zapu.net.render;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class MainComponent extends Canvas implements Runnable, MouseListener
{
	protected JFrame frame;
	
	private BufferedImage img;
	private int[] imgPixels;
	private Render render;
	
	public MainComponent()
	{
		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		imgPixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
		
		render = new Render();
	}
	
	public static void main(String[] args) {
		MainComponent component = new MainComponent();
		
		JFrame frame = new JFrame("render");
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(component, BorderLayout.CENTER);
	
		frame.setContentPane(panel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 600);
		frame.setVisible(true);
		
		component.frame = frame;
		component.start();
		
		component.addMouseListener(component);
	}
	
	private boolean running = false;
	private int width = 800;
	private int height = 600;

	private void start() {
		Thread thread = new Thread(this);
		running = true;
		thread.start();
	}

	@Override
	public void run() {
		long prevTime = System.nanoTime();
		
		while(running) {
			long now = System.nanoTime();
			long deltaTime = now - prevTime;
			prevTime = now;
			
			long yieldTime = 1000000000 / 60 - deltaTime;
			long sleepTime = 1; //always sleep at least 1 mili
			if(yieldTime > 0){
				sleepTime += yieldTime / 1000000;
			}
			
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			
			tick(deltaTime);
			render();
			
			int fps = (int)(1000000000 / deltaTime);
			frame.setTitle("Render (fps:" + fps + ")");
		}
	}
	
	private void render() {
		BufferStrategy bs = getBufferStrategy();
		if(bs == null) {
			createBufferStrategy(2);
			return;
		}
		
		for(int i = 0; i < 480000; i++) {
			imgPixels[i] = 0x00000000;
		}
		
		Graphics g = bs.getDrawGraphics();
		g.fillRect(0, 0, width, height);
		render.draw(g, imgPixels);
		g.drawImage(img, 0, 0, width, height, null);
		g.dispose();
		bs.show();
	}
	
	int oldScreenX, oldScreenY;
	boolean screenMovedYet = false;
	boolean rotating = false;
	boolean moving = false;
	
	private void tick(long deltaTime) {
		if(!rotating && !moving)
			return;
		
		PointerInfo info = MouseInfo.getPointerInfo();
		Point p = info.getLocation();
		Point p2 = frame.getLocationOnScreen();
		
		int screenX = (int)(p.getX() - p2.getX());
		int screenY = (int)(p.getY() - p2.getY());
		
		if(screenX < 0 || screenX > width || screenY < 0 || screenY > height)
			return;
		
		if(screenMovedYet) {
			int dx = screenX - oldScreenX;
			int dy = screenY - oldScreenY;
			
			if(rotating) {
				render.rotateView(dx, -dy);
			}
			if(moving) {
				render.moveView(dx, dy);
			}
		} else {
			screenMovedYet = true;
		}
		
		oldScreenX = screenX;
		oldScreenY = screenY;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON1) {
			rotating = true;
		} else if(e.getButton() == MouseEvent.BUTTON3) {
			moving = true;
		}
		screenMovedYet = false;		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON1) {
			rotating = false;
		} else if(e.getButton() == MouseEvent.BUTTON3) {
			moving = false;
		}	
	}
}
