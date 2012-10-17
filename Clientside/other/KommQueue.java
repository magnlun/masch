package other;

import java.util.LinkedList;
import java.util.Queue;

public class KommQueue extends LinkedList<Element> implements Queue<Element>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -9082910935644912829L;
	
	public void add(String add){
		add(add, 5);
	}
	
	public void add(String add, long time){
		Element el = new Element(add, time, this);
		add(el);
		el.start();
	}
	

}

class Element extends Thread{
	String elem;
	long time;
	Queue<Element> queue;
	
	Element(String elem, long time, Queue<Element> queue){
		super();
		this.elem = elem;
		this.time = time * 1000;
		this.queue = queue;
	}
	
	public String toString(){
		return elem;
	}
	
	public void run(){
		try {
			sleep(time);
		} catch (InterruptedException e) {}
		queue.remove(this);
	}
}