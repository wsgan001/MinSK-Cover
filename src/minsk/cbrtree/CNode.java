/**
 * 
 */
package minsk.cbrtree;

import java.util.ArrayList;

import minsk.Env;
import minsk.Words;
import minsk.rtree.Pair;
import minsk.util.Bitmap;


/**
 * CNode
 * @author Dong-Wan Choi
 * 2015. 9. 2.
 */
public class CNode {
	public boolean isleaf;   // leaf = true
	ArrayList<CEntry> entryList;
	CNode parent;
	Pair x;
	Pair y;
	public Bitmap bmp; // keyword bitmap
	public int minC, maxC; // minimum and maximum cardinality
	
	public CNode(boolean flag){
		isleaf = flag;
		parent = null;
		x = new Pair();
		y = new Pair();
		initEntries();
	}
	public CNode(){
		isleaf = false;
		parent = null;
		x = new Pair();
		y = new Pair();
		initEntries();
	}
	public void initEntries(){
		entryList = new ArrayList<CEntry>();
		x.l = Env.MaxCoord;
		x.h = 0;
		y.l = Env.MaxCoord;
		y.h = 0;
		bmp = null;
		minC = Integer.MAX_VALUE;
		maxC = Integer.MIN_VALUE;
	}
	public int size(){
		return entryList.size();
	}
	public CEntry get(int i){
		return entryList.get(i);
	}
	public void add(CEntry e){
		entryList.add(e);
		updateMBR(e);
		updateBitmap(e, CBRTree.W);
		updateMinMaxC(e);
	}
	private void updateMBR(CEntry e) {
		x.l = Math.min(x.l, e.x.l);
		x.h = Math.max(x.h, e.x.h);
		y.l = Math.min(y.l, e.y.l);
		y.h = Math.max(y.h, e.y.h);
	}
	public void updateBitmap(CEntry e, Words w) {
		Bitmap tbmp;
		if (e instanceof CLEntry) tbmp = w.getBitmap(((CLEntry)e).obj.text);
		else tbmp = e.child.bmp;
		
		if (bmp == null) bmp = new Bitmap(tbmp);
		else bmp.or(tbmp);
	}
	public void updateMinMaxC(CEntry e) {
		if (e instanceof CLEntry) {
			minC = Math.min(minC, ((CLEntry)e).obj.text.size());
			maxC = Math.max(maxC, ((CLEntry)e).obj.text.size());
		} else {
			minC = Math.min(minC, e.child.minC);
			maxC = Math.max(maxC, e.child.maxC);
		}
	}
	public void remove(CEntry e){
		entryList.remove(e);
		updateMBR(e);
	}
	public CEntry remove(int a){
		CEntry e = entryList.remove(a);
		updateMBR(e);
		return e;
	}
	public double overlap(int i, CEntry e){// overlap cost if e is inserted into i-th entry
		double xl, xh, yl, yh;
		xl = Math.min(get(i).x.l, e.x.l);
		xh = Math.max(get(i).x.h, e.x.h);
		yl = Math.min(get(i).y.l, e.y.l);
		yh = Math.max(get(i).y.h, e.y.h);
		CEntry k = new CEntry(xl, xh, yl, yh);
		
		double cost = 0;
		for (int j = 0; j < size(); j++)
		{
			if (i == j) continue;
			cost += k.overlap(get(j));
		}
		return cost;
	}
	public double diffArea(CEntry e) { // area difference if e is inserted
		double s = area();
		double xl, xh, yl, yh;
		xl = Math.min(x.l, e.x.l);
		xh = Math.max(x.h, e.x.h);
		yl = Math.min(y.l, e.y.l);
		yh = Math.max(y.h, e.y.h);
		double r = (xh-xl)*(yh-yl);
		return r-s;
	}
	public CEntry find(CNode n){
		for (int i=0; i<size(); i++)
			if (get(i).child.equals(n)) return get(i);
		return null;
	}	
	public double area(){        // return area
		return (x.h-x.l)*(y.h-y.l); 

	}
	public double margin(){
		return 2*((x.h-x.l)+(y.h-y.l));
	}
	
	public String toString() {
		return "("+ this.x.l + "," + this.x.h + ")" + "("+ this.y.l + "," + this.y.h + ")";
	}
}
