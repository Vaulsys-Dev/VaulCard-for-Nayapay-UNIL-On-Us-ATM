package vaulsys.util;

public class MathUtil {
	public static long lcm(long a, long b, long c, long d){
		long dc = lcm(d,c);
		long ab = lcm(a,b);
		return lcm(ab, dc);
	}

	public static long lcm(long a, long b, long c){
		long bc = lcm(b,c);
		return lcm(a, bc);
	}

	public static long lcm(long a, long b){
		if(a > b)
			return a * b / gcd(a,b);
		else
			return a * b / gcd(b,a);
	}
	
	public static long gcd(long m, long n){
		long x;
		long y;
		
		while(m%n != 0){
			x = n;
			y = m%n;
			m = x;
			n = y;
		}
		
		return n;
	}
}
