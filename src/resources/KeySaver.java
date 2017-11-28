package resources;

import java.io.Serializable;
import java.math.BigInteger;

public class KeySaver implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private BigInteger sqr;

	public KeySaver() {

	}

	public KeySaver(BigInteger sq) {
		this.sqr = sq;
	}
	
	public BigInteger getSqr() {
		return sqr;
	}
	

}
