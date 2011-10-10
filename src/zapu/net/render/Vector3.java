//www.cs.princeton.edu/~cdecoro/.../Vector3.java

package zapu.net.render;

public class Vector3
{
	public Vector3()
	{
		xyz[0] = 0;
		xyz[1] = 0;
		xyz[2] = 0;
	}

	public Vector3(double x, double y, double z)
	{
		xyz[0] = x;
		xyz[1] = y;
		xyz[2] = z;
	}

	public Vector3(double[] array)
	{
		if(array.length != 3)
			throw new RuntimeException("Must create vector with 3 element array");

		xyz[0] = array[0];
		xyz[1] = array[1];
		xyz[2] = array[2];
	}
	
	public Vector3(Vector3 a) {
		xyz[0] = a.xyz[0];
		xyz[1] = a.xyz[1];
		xyz[2] = a.xyz[2];
	}

	public double[] array()
	{
		return (double[])xyz.clone();
	}

	public Vector3 add(Vector3 rhs)
	{
		return new Vector3(
			xyz[0] + rhs.xyz[0],
			xyz[1] + rhs.xyz[1],
			xyz[2] + rhs.xyz[2] );
	}

	public Vector3 sub(Vector3 rhs)
	{
		return new Vector3(
			xyz[0] - rhs.xyz[0],
			xyz[1] - rhs.xyz[1],
			xyz[2] - rhs.xyz[2] );
	}
	
	public Vector3 neg()
	{
		return new Vector3(-xyz[0], -xyz[1], -xyz[2]);
	}

	public Vector3 mul(double c)
	{
		return new Vector3(c*xyz[0], c*xyz[1], c*xyz[2]);
	}

	public Vector3 div(double c)
	{
		return new Vector3(xyz[0]/c, xyz[1]/c, xyz[2]/c);
	}

	public double dot(Vector3 rhs)
	{
		return xyz[0]*rhs.xyz[0] +
			xyz[1]*rhs.xyz[1] +
			xyz[2]*rhs.xyz[2];
	}

	public Vector3 cross(Vector3 rhs)
	{
		return new Vector3(
			xyz[1]*rhs.xyz[2] - xyz[2]*rhs.xyz[1],
			xyz[2]*rhs.xyz[0] - xyz[0]*rhs.xyz[2],
			xyz[0]*rhs.xyz[1] - xyz[1]*rhs.xyz[0]
		);
	}

	public boolean equals(Object obj)
	{
		if( obj instanceof Vector3 )
		{
			Vector3 rhs = (Vector3)obj;

			return xyz[0]==rhs.xyz[0] &&
			       xyz[1]==rhs.xyz[1] &&
			       xyz[2]==rhs.xyz[2];
		}
		else
		{
			return false;
		}
		
	}

	public double norm()
	{
		return Math.sqrt(this.dot(this));	
	}

	public Vector3 normalize()
	{
		return this.div(norm());
	}

	public double x()
	{
		return xyz[0];
	}
	
	public double y()
	{
		return xyz[1];
	}

	public double z()
	{
		return xyz[2];
	}

	public String toString()
	{
		return "( " + xyz[0] + " " + xyz[1] + " " + xyz[2] + " )"; 
	}
	
	/*package*/ double xyz[] = new double[3];
	
	public static Vector3 ZERO = new Vector3(0,0,0); 
}
