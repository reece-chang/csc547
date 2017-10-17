package hull;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import acp.GO;
import pv.AreaABC;
import pv.DiffX;
import pv.PV2;

public class Fast implements Huller {
	private static class Edge {
		GO<PV2> tail, head;

		Edge(GO<PV2> tail, GO<PV2> head) {
			this.tail = tail;
			this.head = head;
		}
	}

	private static class FState implements hull.State {
		List<GO<PV2>> lower;
		List<GO<PV2>> upper;
		GO<PV2> p;

		FState(List<GO<PV2>> lower, List<GO<PV2>> upper, GO<PV2> p) {
			this.lower = new ArrayList<GO<PV2>>(lower);
			this.upper = new ArrayList<GO<PV2>>(upper);
			this.p = p;
		}

		public void draw(Graphics2D g) {
			for (int i = 0; i < lower.size() - 1; i++) {
				Drawer.drawEdge(g, lower.get(i).xyz(), lower.get(i + 1).xyz(), Color.BLUE, "");
			}

			if (lower.size() > 0 && p != null) {
				if (upper.size() > 0) {
					Drawer.drawEdge(g, upper.get(upper.size() - 1).xyz(), p.xyz(), Color.RED, "maybe");
				} else {
					Drawer.drawEdge(g, lower.get(lower.size() - 1).xyz(), p.xyz(), Color.RED, "maybe");
				}
			}

			for (int i = 0; i < upper.size() - 1; i++) {
				Drawer.drawEdge(g, upper.get(i).xyz(), upper.get(i + 1).xyz(), Color.BLACK, "");
			}

		}
	}

	List<State> states = new ArrayList<State>();

	public int numStates() {
		return states.size();
	}

	public State getState(int i) {
		return states.get(i);
	}

	public List<GO<PV2>> hull(List<GO<PV2>> in) {
		states.clear();

		List<Edge> edges = new ArrayList<Edge>();

		// states.add(new FState());

		List<GO<PV2>> out = new ArrayList<GO<PV2>>();

		if (in.size() < 3) {
			out.addAll(in);
			return out;
		}

		List<GO<PV2>> incopy = new ArrayList<GO<PV2>>();
		incopy.addAll(in);

		class PV2Comparator implements Comparator<GO<PV2>> {
			public int compare(GO<PV2> p1, GO<PV2> p2) {
				return (DiffX.sign(p1, p2));
			}
		}

		Collections.sort(incopy, new PV2Comparator());

		List<GO<PV2>> lower = new ArrayList<GO<PV2>>();
		List<GO<PV2>> upper = new ArrayList<GO<PV2>>();

		Set<GO<PV2>> lowerSet = new HashSet<GO<PV2>>();
		
		lower.add(incopy.get(0));
		for(int i = 1; i < incopy.size(); i++) {
		    GO<PV2> p = incopy.get(i);
			states.add(new FState(lower, upper, p));
			while ((lower.size() > 1)
					&& (AreaABC.sign(lower.get(lower.size() - 1), lower.get(lower.size() - 2), p) > 0)) {
				lowerSet.remove(lower.get(lower.size()-1));
				lower.remove(lower.size() - 1);
				states.add(new FState(lower, upper, p));
			}
			lower.add(p);
			if(i != 0 && i != incopy.size() -1)
				lowerSet.add(p);
			states.add(new FState(lower, upper, null));
		}

		upper.add(incopy.get(incopy.size()-1));
		for (int i = incopy.size() - 2; i >= 0; i--) {
			GO<PV2> p = incopy.get(i);
			
			if(lowerSet.contains(p)) continue;
			
			states.add(new FState(lower, upper, p));
			while ((upper.size() > 1)
					&& (AreaABC.sign(upper.get(upper.size() - 1), upper.get(upper.size() - 2), p) > 0)) {
				upper.remove(upper.size() - 1);
				states.add(new FState(lower, upper, p));
			}
			upper.add(p);
			states.add(new FState(lower, upper, null));
		}

		lower.remove(lower.size() - 1);
		upper.remove(upper.size() - 1);

		out.addAll(lower);
		out.addAll(upper);

		return out;

	}
}
