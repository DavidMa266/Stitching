package main;

public class Tuple<L, R> {

	
	private L left;
	private R right;
	
	
	public Tuple(L left, R right){
		this.left = left;
		this.right = right;
	}
	
	public L get_left(){
		return left;
	}
	
	public R get_right(){
		return right;
	}
	
	@Override
	public int hashCode(){
		return left.hashCode() ^ right.hashCode();
	}
	
	public boolean equals(Object other){
		if(other instanceof Tuple){
			Tuple o = (Tuple)other;
			return left.equals(o.left) && right.equals(o.right);
		}
		return false;
	}
}
