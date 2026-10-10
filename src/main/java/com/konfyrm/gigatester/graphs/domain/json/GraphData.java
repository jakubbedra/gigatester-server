package com.konfyrm.gigatester.graphs.domain.json;

import java.util.List;

/**
 * Mirrors the Graph Lab frontend's exported JSON shape (vertices/edges/
 * hyperedges) exactly, so the stored blob can be read back without a
 * bespoke translation layer. Plain public fields — Jackson (de)serializes
 * these by reflection with no annotations needed.
 */
public class GraphData {

    public List<Vertex> vertices;
    public List<Edge> edges;
    public List<Hyperedge> hyperedges;
    /** Display flag: whether vertex circles are drawn (labels always are). Null on older graphs, treated as true. */
    public Boolean showVertices;
    /** "straight" or "curved"; null on older graphs, treated as straight. */
    public String edgeStyle;

    public static class Vertex {
        public String id;
        public double x;
        public double y;
        public String label;
    }

    public static class Edge {
        public String id;
        public String kind; // "edge" | "directedEdge"
        public String from;
        public String to;
    }

    public static class Hyperedge {
        public String id;
        public String kind; // "hyperedge"
        public List<String> sources;
        public List<String> targets;
        public String label;
        public Double hubX;
        public Double hubY;
    }
}
