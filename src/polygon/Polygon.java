package polygon;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Set;

import VertPair.XLess;
import VertPair.XMore;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Comparator;
import java.util.HashMap;
import java.awt.Graphics2D;
import java.awt.Color;
import acp.*;
import pv.*;
import hull.State;
import segment.Drawer;
import pv.ABintersectCD;

public class Polygon {

	class PState implements State {
		int nverts; // number of verts in out to display
		int nedges; // number of edges in out to display
		List<Edge> sedges = new ArrayList<Edge>(); // Sweep edges
		Real y;
		Edge a, b;

		PState(Real y, Edge a, Edge b) {
			if (out != null)
				nverts = out.verts.size();
			if (out != null)
				nedges = out.edges.size();
			for (SweepNode n = sweep.getFirst(); n != null; n = n.getNext())
				sedges.add((Edge) n.getData());
			this.y = y;
			this.a = a;
			this.b = b;
		}

		public void draw(Graphics2D g) {
			int j = 0;
			for (Edge e : sedges)
				Drawer.drawEdge(g, e.tail.p.xyz(), e.head.p.xyz(), Color.orange, "" + j++);

			if (a != null)
				Drawer.drawEdge(g, a.tail.p.xyz(), a.head.p.xyz(), Color.red, "");

			if (b != null)
				Drawer.drawEdge(g, b.tail.p.xyz(), b.head.p.xyz(), Color.red, "");

			for (int i = 0; i < nverts; i++)
				Drawer.drawPoint(g, out.verts.get(i).p.xyz(), Color.black, "");

			for (int i = 0; i < nedges; i++)
				Drawer.drawEdge(g, out.edges.get(i).tail.p.xyz(), out.edges.get(i).head.p.xyz(), Color.black, "");

			if (y != null) {
				PV2 pm = new PV2(Real.constant(-1000), y);
				PV2 pp = new PV2(Real.constant(1000), y);
				Drawer.drawEdge(g, pm, pp, Color.blue, "");
			}
		}
	}

	class MState implements State {
		int nchords; // number of chords to display
		List<Edge> sedges = new ArrayList<Edge>(); // Sweep edges
		Real y;

		String message;

		MState(Real y) {
			nchords = chords.size();
			for (SweepNode n = sweep.getFirst(); n != null; n = n.getNext())
				sedges.add((Edge) n.getData());
			this.y = y;
			this.message = null;
		}

		MState(Real y, String message) {
			this(y);
			this.message = message;
		}

		public void draw(Graphics2D g) {
			int j = 0;
			for (Edge e : sedges)
				Drawer.drawEdge(g, e.tail.p.xyz(), e.head.p.xyz(), Color.orange, "" + j++);

			for (int i = 0; i < nchords; i++)
				Drawer.drawEdge(g, chords.get(i).tail.p.xyz(), chords.get(i).head.p.xyz(), Color.black, "");

			if (y != null) {
				PV2 pm = new PV2(Real.constant(-1000), y);
				PV2 pp = new PV2(Real.constant(1000), y);
				Drawer.drawEdge(g, pm, pp, Color.blue, "");
			}

			if (message != null)
				System.out.println(message);
		}
	}

	class TState implements State {
		int nchords; // number of chords to display
		List<Edge> bottoms;
		Edge last, left, right;

		TState(List<Edge> bottoms) {
			nchords = chords.size();
			this.bottoms = bottoms;
			this.last = null;
			this.left = null;
			this.right = null;
		}

		TState(List<Edge> bottoms, Edge last) {
			nchords = chords.size();
			this.bottoms = bottoms;
			this.last = last;
			this.left = null;
			this.right = null;
		}

		TState(List<Edge> bottoms, Edge last, Edge left, Edge right) {
			nchords = chords.size();
			this.bottoms = bottoms;
			this.last = last;
			this.left = left;
			this.right = right;
		}

		public void draw(Graphics2D g) {
			int j = 0;
			for (Edge b : bottoms) {
				Drawer.drawEdge(g, b.tail.p.xyz(), b.head.p.xyz(), Color.magenta, "bottom" + j++);
			}

			for (int i = 0; i < nchords; i++)
				Drawer.drawEdge(g, chords.get(i).tail.p.xyz(), chords.get(i).head.p.xyz(), Color.black, "added");

			if (last != null)
				Drawer.drawEdge(g, last.tail.p.xyz(), last.head.p.xyz(), Color.blue, "last");
			if (left != null)
				Drawer.drawEdge(g, left.tail.p.xyz(), left.head.p.xyz(), Color.red, "left");
			if (right != null)
				Drawer.drawEdge(g, right.tail.p.xyz(), right.head.p.xyz(), Color.orange, "right");
		}
	}

	List<State> states = new ArrayList<State>();

	public int numStates() {
		return states.size();
	}

	public State getState(int i) {
		return states.get(i);
	}

	class Vert {
		GO<PV2> p;
		Edge incoming, outgoing;
		Vert twin;

		Vert(GO<PV2> p, Edge incoming, Edge outgoing) {
			this.p = p;
			this.incoming = incoming;
			this.outgoing = outgoing;
		}

		Vert(Vert that) {
			this.p = that.p;
			this.incoming = that.incoming;
			this.outgoing = that.outgoing;
		}

		Polygon getPolygon() {
			return Polygon.this;
		}

		void draw(Graphics2D g) {
			Drawer.drawPoint(g, p.xyz(), Color.green, "");
		}

		void informEdges() {
			this.incoming.head = this;
			this.outgoing.tail = this;
		}
	}

	class Edge implements SweepData {
		Vert tail, head;
		SweepNode node;
		Set<Edge> checked = new HashSet<Edge>();

		int inout;

		Edge next, prev, twin, helper;

		Edge(Vert tail, Vert head) {
			this.tail = tail;
			this.head = head;
		}

		Edge(Edge that) {
			this.tail = that.tail;
			this.head = that.head;
		}

		Polygon getPolygon() {
			return Polygon.this;
		}

		void addChord(Edge that) {
			Edge e = new Edge(this.head, that.head);
			Edge f = new Edge(that.head, this.head);

			e.twin = f;
			f.twin = e;

			e.next = that.next;
			f.next = this.next;
			this.next = e;
			that.next = f;

			chords.add(e);
			chords.add(f);

		}

		void addChordHeads(Edge that) {
			Edge e = new Edge(this.head, that.head);
			Edge f = new Edge(that.head, this.head);

			e.twin = f;
			f.twin = e;

			e.next = that.next;
			e.prev = this;
			f.next = this.next;
			f.prev = that;
			e.next.prev = e;
			f.next.prev = f;

			this.next = e;
			that.next = f;

			chords.add(e);
			chords.add(f);
		}

		void addChordTails() {
			// ?
		}

		boolean isStalagmite() {

			return (this.head == this.maxY() && this.next.tail == this.next.maxY()
					&& AreaABC.sign(this.tail.p, this.head.p, this.next.head.p) < 0);

		}

		boolean isStalactite() {

			return (this.head == this.minY() && this.next.tail == this.next.minY()
					&& AreaABC.sign(this.tail.p, this.head.p, this.next.head.p) < 0);

		}

		void setHelper(Edge newHelper) {
			if (helper != null && (helper.isStalagmite() || newHelper.isStalactite()))
				helper.addChord(newHelper);
			helper = newHelper;
		}

		void informVerts() {
			head.incoming = this;
			tail.outgoing = this;
		}

		void setMinY(Vert v) {
			if (DiffY.sign(head.p, tail.p) < 0)
				head = v;
			else
				tail = v;
		}

		void setMaxY(Vert v) {
			if (DiffY.sign(head.p, tail.p) > 0)
				head = v;
			else
				tail = v;
		}

		void draw(Graphics2D g) {
			Drawer.drawEdge(g, tail.p.xyz(), head.p.xyz(), Color.blue, "");
		}

		void draw(Graphics2D g, Color c) {
			Drawer.drawEdge(g, tail.p.xyz(), head.p.xyz(), c, "");
		}

		boolean intersects(Edge that) {
			return (AreaABC.sign(tail.p, that.tail.p, that.head.p) != AreaABC.sign(head.p, that.tail.p, that.head.p)
					&& AreaABC.sign(that.tail.p, tail.p, head.p) != AreaABC.sign(that.head.p, tail.p, head.p));
		}

		GO<PV2> intersection(Edge that) {
			return new ABintersectCD(tail.p, head.p, that.tail.p, that.head.p);
		}

		Vert minY() {
			return DiffY.sign(tail.p, head.p) < 0 ? tail : head;
		}

		Vert maxY() {
			return DiffY.sign(tail.p, head.p) > 0 ? tail : head;
		}

		public int compareTo(SweepData data) {
			Edge that = (Edge) data;
			// EXERCISE 1
			// Use minY and maxY instead of tail and head.
			// Include the case that this and that have the same minY vertex.

			if (this.minY() == that.minY()) {
				return AreaABC.sign(this.minY().p, this.maxY().p, that.maxY().p);
			}

			Edge lower, upper;

			if (DiffY.sign(this.minY().p, that.minY().p) > 0) {
				lower = that;
				upper = this;
			} else {
				lower = this;
				upper = that;
			}

			return ((this == lower) ? 1 : -1) * AreaABC.sign(lower.minY().p, lower.maxY().p, upper.minY().p);

		}

		public SweepNode getNode() {
			return node;
		}

		public void setNode(SweepNode node) {
			this.node = node;
		}
	}

	List<Vert> verts = new ArrayList<Vert>();
	List<Edge> edges = new ArrayList<Edge>();
	List<Edge> chords = new ArrayList<Edge>();

	int inout;

	int getInOut() {
		if (inout == 0) {
			Vert v = verts.get(0);
			for (int i = 1; i < verts.size(); i++) {
				if (DiffY.sign(v.p, verts.get(i).p) > 0)
					v = verts.get(i);
			}
			if (v.incoming.compareTo(v.outgoing) < 0)
				inout = 2;
			else
				inout = 1;
		}
		return inout;

	}

	void copyEdge(Edge e, Vert newMaxY) {
		if (e.inout != 2)
			return;
		if (e.minY().getPolygon() != out) {
			e.setMinY(out.new Vert(e.minY()));
			e.minY().informEdges();
			out.verts.add(e.minY());
		}

		if (newMaxY == null && e.maxY().getPolygon() != out) {
			e.setMaxY(out.new Vert(e.maxY()));
			e.maxY().informEdges();
			out.verts.add(e.maxY());
		}
		Edge copye = out.new Edge(e);
		if (newMaxY != null) {
			copye.setMaxY(newMaxY);
		}
		out.edges.add(copye);

	}

	private Polygon() {
	}

	public Polygon(List<GO<PV2>> points) {
		for (GO<PV2> p : points)
			verts.add(new Vert(p, null, null));

		Vert prev = verts.get(verts.size() - 1);
		for (Vert v : verts) {
			Edge e = new Edge(prev, v);
			edges.add(e);
			e.tail.outgoing = e;
			e.head.incoming = e;
			prev = v;
		}
	}

	public void monotonize() {
		states.clear();
		events.clear();
		chords.clear();

		sweep = new SlowList();

		for (Edge e : edges) {
			e.helper = null;
		}

		// Set Edge nexts
		for (Edge e : edges) {
			e.next = e.head.outgoing;
		}

		for (Vert v : verts) {
			events.offer(v);
		}

		while (events.size() > 0) {
			Vert v = events.poll();

			if (v == v.incoming.minY() && v == v.outgoing.minY()) {
				SweepNode iNode = sweep.add(v.incoming);
				SweepNode oNode = sweep.add(v.outgoing);

				if (oNode.getNext() == iNode) {
					((Edge) iNode.getNext().getData()).setHelper(v.incoming);
					v.outgoing.setHelper(v.incoming.next.twin);

					states.add(new MState(v.p.xyz().y, "min min stalactite"));

				} else {
					v.outgoing.setHelper(v.incoming);
					states.add(new MState(v.p.xyz().y, "min min"));
				}

			} else if (v == v.incoming.maxY() && v == v.outgoing.minY()) {

				v.incoming.node.remove();
				sweep.add(v.outgoing);

				v.incoming.setHelper(v.incoming);

				if (v.incoming.next != v.outgoing) {
					v.outgoing.setHelper(v.incoming.next.twin);
					states.add(new MState(v.p.xyz().y, "max min add chord"));
				} else {
					v.outgoing.setHelper(v.incoming);
					states.add(new MState(v.p.xyz().y, "max min"));
				}

			} else if (v == v.incoming.minY() && v == v.outgoing.maxY()) {

				v.outgoing.node.remove();
				SweepNode iNode = sweep.add(v.incoming);

				((Edge) iNode.getNext().getData()).setHelper(v.incoming);

				states.add(new MState(v.p.xyz().y, "min max"));

			} else if (v == v.incoming.maxY() && v == v.outgoing.maxY()) {

				v.incoming.setHelper(v.incoming);

				if (v.incoming.node.getNext() == v.outgoing.node) {

					if (v.incoming.next != v.outgoing) {
						((Edge) v.outgoing.node.getNext().getData()).setHelper(v.incoming.next.twin);
						states.add(new MState(v.p.xyz().y, "max max stalagmite add chord"));
					} else {
						((Edge) v.outgoing.node.getNext().getData()).setHelper(v.incoming);
						states.add(new MState(v.p.xyz().y, "max max stalagmite"));
					}

				} else {
					if (v.incoming.next != v.outgoing) {
						states.add(new MState(v.p.xyz().y, "max max chord added"));
					} else {
						states.add(new MState(v.p.xyz().y, "max max"));
					}
				}

				v.incoming.node.remove();
				v.outgoing.node.remove();

			}
		}

	}

	List<Edge> bottoms = new ArrayList<Edge>();

	public void triangulate() {

		monotonize();
		states.clear();

		for (Edge e : edges) {
			e.next.prev = e;
			if (e.minY() == e.next.minY() && e.next.next.next != e) {
				bottoms.add(e);
			}
		}

		for (Edge c : chords) {
			c.next.prev = c;
			if (c.minY() == c.next.minY() && c.next.next.next != c) {
				bottoms.add(c);
			}
		}

		for (Edge bottom : bottoms) {
			triangulate(bottom);
			states.add(new TState(bottoms));
		}

		/*
		 * while (events.size() > 0) { Vert v = events.poll();
		 * 
		 * if (v == v.incoming.minY() && v == v.outgoing.minY() &&
		 * !v.incoming.isStalactite()) { bottoms.add(v.incoming); }
		 * 
		 * }
		 * 
		 * for (Edge bottom : bottoms) { triangulate(bottom); states.add(new
		 * TState(bottoms)); }
		 */

	}

	public void triangulate(Edge bottom) {
		Edge left, right;
		left = bottom;
		right = bottom.next;

		if (DiffY.sign(left.maxY().p, right.maxY().p) < 0) {
			triangulateLeft(left, right);
		} else {
			triangulateRight(left, right);
		}
	}
	

	public void triangulateLeft(Edge left, Edge right) {
		Edge last = left;
		states.add(new TState(bottoms, last, left, right));
		Edge rightmax = right;

		/*
		 * if (right.next.next.next != right && right.maxY() ==
		 * right.next.maxY()) { last = last.prev; right = right.prev; }
		 */

		while ((last.prev.maxY() != right.maxY()) && (DiffY.sign(last.prev.maxY().p, right.maxY().p) < 0)) {
			while ((last.next != right) && (AreaABC.sign(last.tail.p, last.head.p, last.next.head.p) > 0)) {
				last.prev.addChordHeads(last.next);
				states.add(new TState(bottoms, last, left, right));
				last = last.prev.twin;
				states.add(new TState(bottoms, last, left, right));
			}
			last = last.prev;
			states.add(new TState(bottoms, last, left, right));
		}

		Edge lastprev = last.prev;

		while (right.prev != lastprev) {
			if (lastprev.maxY() == right.maxY()) {
				left.addChord(left.prev.prev);
				states.add(new TState(bottoms, last, left, right));
				right = left.next;
				states.add(new TState(bottoms, last, left, right));
			}

			right.addChordHeads(right.prev.prev);
			states.add(new TState(bottoms, last, left, right));
			right = right.next.twin;
			states.add(new TState(bottoms, last, left, right));
		}

		if (lastprev.maxY() != right.maxY())
			triangulateRight(right.prev, right);
		else
			return;

	}

	public void triangulateRight(Edge left, Edge right) {
		Edge last = right;
		states.add(new TState(bottoms, last, left, right));

		Edge leftmax = left;

		/*
		 * if (left.next.next.next != left && left.maxY() == left.prev.maxY()) {
		 * left = left.next; last = last.next; }
		 */

		while ((last.next.maxY() != left.maxY()) && (DiffY.sign(last.next.maxY().p, left.maxY().p) < 0)) {
			while ((last.prev != left) && (AreaABC.sign(last.prev.tail.p, last.tail.p, last.head.p) > 0)) {
				last.prev.prev.addChordHeads(last);
				states.add(new TState(bottoms, last, left, right));
				last = last.next.twin;
				states.add(new TState(bottoms, last, left, right));
			}
			last = last.next;
			states.add(new TState(bottoms, last, left, right));
		}

		Edge lastnext = last.next;

		while (left.next != lastnext) {
			/*
			 * if (lastnext.maxY() == left.maxY()) {
			 * left.addChordHeads(left.next.next); states.add(new
			 * TState(bottoms, last, left, right)); left = left.next;
			 * states.add(new TState(bottoms, last, left, right)); }
			 */
			left.prev.addChordHeads(left.next);
			states.add(new TState(bottoms, last, left, right));
			left = left.prev.twin;
			states.add(new TState(bottoms, last, left, right));
		}

		if (lastnext.maxY() != leftmax.maxY())
			triangulateLeft(left, left.next);
		else
			return;
	}

	public void draw(Graphics2D g) {
		for (Edge e : edges)
			e.draw(g);

		for (Edge c : chords)
			c.draw(g, Color.green);

		if (that != null)
			for (Edge e : that.edges)
				e.draw(g);
		if (out != null)
			for (Vert v : out.verts)
				v.draw(g);
	}

	class CompareVerts implements Comparator<Vert> {
		public int compare(Vert a, Vert b) {
			return DiffY.sign(a.p, b.p);
		}
	}

	Polygon that;
	PriorityQueue<Vert> events = new PriorityQueue<Vert>(100, new CompareVerts());
	SweepList sweep = new SlowList();
	Polygon out;

	void check(SweepNode a, SweepNode b) {
		if (a == null || b == null)
			return;

		Edge e = (Edge) a.getData();
		Edge f = (Edge) b.getData();

		if (e.checked.contains(f))
			return;

		// EXERCISE 2
		// Check if from same Polygon too.
		// Add a state after each check.
		states.add(new PState(null, e, f));

		if (e.getPolygon() == f.getPolygon()) {
			return;
		}

		if (!e.intersects(f))
			return;

		GO<PV2> p = new ABintersectCD(e.tail.p, e.head.p, f.tail.p, f.head.p);
		Vert v = out.new Vert(p, e, f);
		out.verts.add(v);
		events.add(v);
		states.add(new PState(null, e, f));
	}

	public Polygon union(Polygon that) {
		states.clear();

		for (Edge e : this.edges)
			e.checked.clear();
		for (Edge e : that.edges)
			e.checked.clear();

		this.inout = 0;
		that.inout = 0;

		this.that = that;
		out = new Polygon();

		for (Vert v : this.verts)
			events.offer(v);
		for (Vert v : that.verts)
			events.offer(v);

		while (events.size() > 0) {
			Vert v = events.poll();
			states.add(new PState(v.p.xyz().y, null, null));

			if (v.getPolygon() == out) {
				SweepNode left = v.incoming.getNode();
				SweepNode right = v.outgoing.getNode();

				// EXERCISE 3
				// v is intersection of a this edge with a that edge.
				left.swapWithNext();

				copyEdge(v.incoming, v);
				v.incoming.setMinY(v);
				copyEdge(v.outgoing, v);
				v.outgoing.setMinY(v);

				v.incoming.inout = 3 - v.incoming.inout;
				v.outgoing.inout = 3 - v.outgoing.inout;

				check(left.getPrevious(), left);
				check(right, right.getNext());

				states.add(new PState(v.p.xyz().y, null, null));
			} else if (v == v.incoming.minY() && v == v.outgoing.minY()) {
				SweepNode iNode = sweep.add(v.incoming);
				SweepNode oNode = sweep.add(v.outgoing);
				// EXERCISE 4
				// v is minY of both its edges.
				// iNode is to the left or right of oNode
				SweepNode left, right;
				if (iNode.getData().compareTo(oNode.getData()) < 0) {
					left = iNode;
					right = oNode;
				} else {
					left = oNode;
					right = iNode;
				}

				SweepNode next = right.getNext();
				if (next == null) {
					if (v.getPolygon() == this) {
						((Edge) left.getData()).inout = that.getInOut();
						((Edge) right.getData()).inout = that.getInOut();
					} else {
						((Edge) left.getData()).inout = this.getInOut();
						((Edge) right.getData()).inout = this.getInOut();
					}
				} else if (((Edge) next.getData()).getPolygon() == v.getPolygon()) {
					((Edge) left.getData()).inout = ((Edge) next.getData()).inout;
					((Edge) right.getData()).inout = ((Edge) next.getData()).inout;
				} else {
					if (DiffY.sign(((Edge) next.getData()).tail.p, ((Edge) next.getData()).head.p) < 0) {
						((Edge) left.getData()).inout = 1;
						((Edge) right.getData()).inout = 1;
					} else {
						((Edge) left.getData()).inout = 2;
						((Edge) right.getData()).inout = 2;
					}
				}

				check(left.getPrevious(), left);
				check(right, right.getNext());

				states.add(new PState(v.p.xyz().y, v.incoming, v.outgoing));

			} else if (v == v.incoming.minY() && v == v.outgoing.maxY()) {

				v.incoming.inout = v.outgoing.inout;
				copyEdge(v.outgoing, null);
				v.outgoing.node.setData(v.incoming);
				SweepNode iNode = v.incoming.node;

				check(iNode.getPrevious(), iNode);
				check(iNode, iNode.getNext());

				states.add(new PState(v.p.xyz().y, v.incoming, v.outgoing));

			} else if (v == v.incoming.maxY() && v == v.outgoing.minY()) {

				v.outgoing.inout = v.incoming.inout;
				copyEdge(v.incoming, null);
				v.incoming.node.setData(v.outgoing);
				SweepNode iNode = v.outgoing.node;

				check(iNode.getPrevious(), iNode);
				check(iNode, iNode.getNext());

				states.add(new PState(v.p.xyz().y, v.incoming, v.outgoing));

			} else if (v == v.incoming.maxY() && v == v.outgoing.maxY()) {

				SweepNode iNode = v.incoming.node;
				SweepNode oNode = v.outgoing.node;

				SweepNode left, right;
				if (iNode.getData().compareTo(oNode.getData()) < 0) {
					left = iNode;
					right = oNode;
				} else {
					left = oNode;
					right = iNode;
				}

				SweepNode prev = left.getPrevious();
				SweepNode next = right.getNext();

				left.remove();
				right.remove();
				check(prev, next);

				copyEdge(v.incoming, null);
				copyEdge(v.outgoing, null);

				states.add(new PState(v.p.xyz().y, v.incoming, v.outgoing));

			}
			// EXERCISE 5
			// EXERCISE 6
			// EXERCISE 7
			// Three other cases.

		}

		for (Edge e : this.edges)
			e.checked.clear();
		for (Edge e : that.edges)
			e.checked.clear();

		for (Vert v : this.verts) {
			v.informEdges();
		}

		for (Vert v : that.verts) {
			v.informEdges();
		}

		for (Edge e : out.edges) {
			e.informVerts();
		}

		return out;
	}

	public Polygon intersection(Polygon that) {

		Polygon thiscopy = copy(this);
		Polygon thatcopy = copy(that);
		invert(thiscopy);
		invert(thatcopy);

		Polygon onion = thiscopy.union(thatcopy);
		invert(onion);
		return onion;

	}

	public Polygon difference(Polygon that) {

		Polygon thiscopy = copy(this);
		Polygon thatcopy = copy(that);
		invert(thiscopy);

		Polygon onion = thiscopy.union(thatcopy);
		invert(onion);
		return onion;

	}

	public Polygon complement() {

		Polygon comp = copy(this);
		invert(comp);
		return comp;

	}

	public Polygon copy(Polygon in) {

		Polygon copyout = new Polygon();

		for (Vert v : in.verts) {
			Vert vcopy = copyout.new Vert(v);
			vcopy.informEdges();
			copyout.verts.add(vcopy);
		}

		for (Edge e : in.edges) {
			Edge ecopy = copyout.new Edge(e);
			ecopy.informVerts();
			copyout.edges.add(ecopy);
		}

		for (Vert v : in.verts) {
			v.informEdges();
		}

		return copyout;
	}

	public void invert(Polygon p) {
		for (Edge e : p.edges) {
			Vert temp;
			temp = e.tail;
			e.tail = e.head;
			e.head = temp;
			e.informVerts();
		}
	}

	public void draw(Graphics2D g, Color color) {
		for (Edge e : edges)
			e.draw(g, color);
	}

	public void draw(Graphics2D g, Color color, PV2 trans, double scalar) {
		for (Edge e : edges) {
			PV2 t = trans.plus(e.tail.p.xyz().times(scalar));
			PV2 h = trans.plus(e.head.p.xyz().times(scalar));
			Drawer.drawEdge(g, t, h, color, "");
		}
	}

	class VertPair {
		final Vert a, b;

		VertPair(Vert a, Vert b) {
			this.a = a;
			this.b = b;
		}

		public int hashCode() {
			return a.hashCode() + b.hashCode();
		}

		public boolean equals(Object other) {
			VertPair that = (VertPair) other;
			return a == that.a && b == that.b;
		}
	}

	Map<VertPair, Vert> vertMap;
	List<Vert> sumVerts;

	Vert getSum(Vert a, Vert b) {
		if (a.getPolygon() != this)
			return getSum(b, a);

		VertPair pair = new VertPair(a, b);
		if (vertMap.containsKey(pair))
			return vertMap.get(pair);

		Vert v = out.new Vert(new AplusB(a.p, b.p), null, null);
		vertMap.put(pair, v);
		return v;
	}

	List<Edge> sumEdges;

	void getSumEdges(Polygon a, Polygon b) {
		for (Vert v : a.verts) {
			for (Edge e : b.edges) {
				GO<PV2> p = v.incoming.tail.p;
				GO<PV2> q = v.p;
				GO<PV2> r = v.outgoing.head.p;

				GO<PV2> s = e.tail.p;
				GO<PV2> t = e.head.p;

				// Continue if three conditions are not satisfied.
				// EXERCISE
				if (!((AreaABC.sign(p, q, r) > 0) && (ABxCD.sign(p, q, s, t) > 0) && (ABxCD.sign(s, t, q, r) > 0))) {
					continue;
				}

				Edge sumEdge = null;
				// Create the sum Edge.
				// Use getSum to add Verts together.
				// EXERCISE
				sumEdge = new Edge(getSum(v, e.tail), getSum(v, e.head));
				sumEdges.add(sumEdge);
			}
		}
	}

	List<List<Vert>> intVerts;
	List<Edge> subEdges;

	public Polygon sum(Polygon that) {
		out = new Polygon();
		vertMap = new HashMap<VertPair, Vert>();
		sumVerts = new ArrayList<Vert>();
		sumEdges = new ArrayList<Edge>();
		intVerts = new ArrayList<List<Vert>>();
		subEdges = new ArrayList<Edge>();

		getSumEdges(this, that);
		getSumEdges(that, this);

		if (false) {
			for (Edge e : sumEdges)
				out.edges.add(e);
			return out;
		}
		
		// intVerts.get(i) contains the Verts at which sum Edge i intersects the
		// other sum Edges
		for (int i = 0; i < sumEdges.size(); i++)
			intVerts.add(new ArrayList<Vert>());

		for (int i = 0; i < sumEdges.size(); i++) {
			Edge ei = sumEdges.get(i);
			for (int j = i + 1; j < sumEdges.size(); j++) {
				Edge ej = sumEdges.get(j);

				// EXERCISE

				// If ei and ej intersect, find their intersection point
				// (ABintersectCD) and create a out.new Vert. Set its
				// incoming and outgoing to ei and ej (temporary).
				// Add the Vert to both intVerts.get(i) and intVerts.get(j).
				if (ei.head.equals(ej.tail)) {
					Vert intervert = out.new Vert(ei.head.p, ei, ej);
					intVerts.get(i).add(intervert);
					intVerts.get(j).add(intervert);		
				} else if (ej.head.equals(ei.tail)) {
					Vert intervert = out.new Vert(ej.head.p, ej, ei);
					intVerts.get(i).add(intervert);
					intVerts.get(j).add(intervert);	
				} else if (ei.intersects(ej)) {
					Vert intervert = out.new Vert(new ABintersectCD(ei.tail.p, ei.head.p, ej.tail.p, ej.head.p), ei, ej);
					intVerts.get(i).add(intervert);
					intVerts.get(j).add(intervert);		
				}			
				
			}
		}

		// Sort Verts on each edge:
		Comparator<Vert> xLess = new XLess();
		Comparator<Vert> xMore = new XMore();
		for (int i = 0; i < sumEdges.size(); i++) {
			Edge e = sumEdges.get(i);

			if (DiffX.sign(e.tail.p, e.head.p) < 0)
				intVerts.get(i).sort(xLess);
			else
				intVerts.get(i).sort(xMore);
		}

		for (int i = 0; i < sumEdges.size(); i++) {
			Edge e = sumEdges.get(i);
			List<Vert> vs = intVerts.get(i);

			// EXERCISE
			// Call checkSubEdge on every possible sub edge.
			for (int j = 0; j < vs.size()-1; j++) {
				checkSubEdge(e, vs.get(j), vs.get(j+1));
			}

		}

		// Gives intersection vertices the correct incoming and outgoing.
		for (List<Vert> l : intVerts)
			for (Vert v : l)
				v.incoming = v.outgoing = null;

		for (Edge e : subEdges)
			e.informVerts();

		// EXERCISE
		// Trim off dangling edges.
		// Set tail.outgoing and head.incoming for a dangling edge to null.
		// Set its tail and head to null.	
		
		for (Edge e : subEdges) {
			if (e.tail != null && e.tail.incoming == null) {
				Edge enext;
				do {
					e.tail.outgoing = null;
					e.head.incoming = null;
				 
					enext = e.head.outgoing;
					
					e.tail = null;
					e.head = null;
					e = enext;
				} while (enext != null);
			}
		}
		
		for (Edge e : subEdges) {
			if (e.tail == null)
				continue;
			out.edges.add(e);
			out.verts.add(e.tail);
		}

		return out;
	}

	class XLess implements Comparator<Vert> {
		public int compare(Vert a, Vert b) {
			if (a == b)
				return 0;
			return DiffX.sign(a.p, b.p);
		}
	}

	class XMore implements Comparator<Vert> {
		public int compare(Vert a, Vert b) {
			if (a == b)
				return 0;
			return -DiffX.sign(a.p, b.p);
		}
	}

	boolean blocked(Edge ab, Vert v, int sign) {
		if (!(v.p instanceof ABintersectCD))
			return false;

		if (ab != v.incoming && ab != v.outgoing) {
			System.out.println("blocked uh oh ");
		}

		Edge cd = v.incoming == ab ? v.outgoing : v.incoming;

		// EXERCISE
		// If sign==1, return true if ab is inside the Minkowski sum (in
		// blocked space) to the tail side of its intersection v with cd.
		// If sign==-1, head side.
		
		if (sign == 1 && AreaABC.sign(cd.tail.p, cd.head.p, ab.tail.p) > 0) {
			return true;
		}
		
		if (sign == -1 && AreaABC.sign(cd.tail.p, cd.head.p, ab.head.p) > 0){
			return true;
		}
		
		return false;
	}

	void checkSubEdge (Edge e, Vert a, Vert b) {
		
	    if (blocked(e, a, -1))
	      return;
	    if (blocked(e, b, 1))
	      return;

	    Edge s = out.new Edge(a, b);
	    subEdges.add(s);
	}
}
	  
