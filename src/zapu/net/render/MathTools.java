package zapu.net.render;

public class MathTools {
	public static double Clamp(double val, double min, double max) {
		if(val > max)
			return max;
		else if(val < min)
			return min;
		else
			return val;
	}
	
	public static int Clamp(int val, int min, int max) {
		if(val > max)
			return max;
		else if(val < min)
			return min;
		else
			return val;
	}
	
	public static float Mix(float x, float y, float w) {
		return x * (1 - w) + y * w;
	}
}
