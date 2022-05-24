package org.eqasim.core.scenario.cutter.network;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.LinkQuadTree;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.SearchableNetwork;
import org.matsim.core.network.algorithms.TransportModeNetworkFilter;
import org.matsim.core.utils.collections.QuadTree;
import org.matsim.utils.objectattributes.attributable.Attributes;

public class RoadNetwork implements Network, SearchableNetwork {
	private Network delegate;

	public RoadNetwork(Network network) {
		delegate = NetworkUtils.createNetwork();
		new TransportModeNetworkFilter(network).filter(delegate, Collections.singleton(TransportMode.car));
	}

    public RoadNetwork_car_bike(final Network network) {
        this.delegate = NetworkUtils.createNetwork();
        final NetworkFactory factory = this.delegate.getFactory();
        final HashSet<String> extractModes = new HashSet<String>();
        extractModes.add("bike");
        extractModes.add("car");
        for (final Node node : network.getNodes().values()) {
            final Node newNode = factory.createNode(node.getId(), node.getCoord());
            AttributesUtils.copyAttributesFromTo((Attributable)node, (Attributable)newNode);
            this.delegate.addNode(newNode);
        }
        final Set<Id<Node>> nodesToInclude = new HashSet<Id<Node>>();
        for (final Link link : network.getLinks().values()) {
            final Set<String> intersection = new HashSet<String>(extractModes);
            intersection.retainAll(link.getAllowedModes());
            if (intersection.size() > 1) {
                final Id<Node> fromId = (Id<Node>)link.getFromNode().getId();
                final Id<Node> toId = (Id<Node>)link.getToNode().getId();
                final Node fromNode2 = this.delegate.getNodes().get(fromId);
                final Node toNode2 = this.delegate.getNodes().get(toId);
                nodesToInclude.add(fromId);
                nodesToInclude.add(toId);
                final Link link2 = factory.createLink(link.getId(), fromNode2, toNode2);
                link2.setAllowedModes((Set)intersection);
                link2.setCapacity(link.getCapacity());
                link2.setFreespeed(link.getFreespeed());
                link2.setLength(link.getLength());
                link2.setNumberOfLanes(link.getNumberOfLanes());
                NetworkUtils.setType(link2, NetworkUtils.getType(link));
                AttributesUtils.copyAttributesFromTo((Attributable)link, (Attributable)link2);
                this.delegate.addLink(link2);
            }
        }
        final Set<Id<Node>> nodesToRemove = new HashSet<Id<Node>>();
        for (final Node node2 : network.getNodes().values()) {
            if (!nodesToInclude.contains(node2.getId())) {
                nodesToRemove.add((Id<Node>)node2.getId());
            }
        }
        for (final Id<Node> nodeId : nodesToRemove) {
            this.delegate.removeNode((Id)nodeId);
        }
    }

	public Attributes getAttributes() {
		return delegate.getAttributes();
	}

	public NetworkFactory getFactory() {
		return delegate.getFactory();
	}

	public Map<Id<Node>, ? extends Node> getNodes() {
		return delegate.getNodes();
	}

	public Map<Id<Link>, ? extends Link> getLinks() {
		return delegate.getLinks();
	}

	public double getCapacityPeriod() {
		return delegate.getCapacityPeriod();
	}

	public double getEffectiveLaneWidth() {
		return delegate.getEffectiveLaneWidth();
	}

	public void addNode(Node nn) {
		delegate.addNode(nn);
	}

	public void addLink(Link ll) {
		delegate.addLink(ll);
	}

	public Node removeNode(Id<Node> nodeId) {
		return delegate.removeNode(nodeId);
	}

	public Link removeLink(Id<Link> linkId) {
		return delegate.removeLink(linkId);
	}

	public void setCapacityPeriod(double capPeriod) {
		delegate.setCapacityPeriod(capPeriod);
	}

	public void setEffectiveCellSize(double effectiveCellSize) {
		delegate.setEffectiveCellSize(effectiveCellSize);
	}

	public void setEffectiveLaneWidth(double effectiveLaneWidth) {
		delegate.setEffectiveLaneWidth(effectiveLaneWidth);
	}

	public void setName(String name) {
		delegate.setName(name);
	}

	public String getName() {
		return delegate.getName();
	}

	public double getEffectiveCellSize() {
		return delegate.getEffectiveCellSize();
	}

	@Override
	public Link getNearestLinkExactly(Coord coord) {
		return ((SearchableNetwork) delegate).getNearestLinkExactly(coord);
	}

	@Override
	public Node getNearestNode(Coord coord) {
		return ((SearchableNetwork) delegate).getNearestNode(coord);
	}

	@Override
	public Collection<Node> getNearestNodes(Coord coord, double distance) {
		return ((SearchableNetwork) delegate).getNearestNodes(coord, distance);
	}

	@Override
	public QuadTree<Node> getNodeQuadTree() {
		return ((SearchableNetwork) delegate).getNodeQuadTree();
	}

	@Override
	public LinkQuadTree getLinkQuadTree() {
		return ((SearchableNetwork) delegate).getLinkQuadTree();
	}
}
