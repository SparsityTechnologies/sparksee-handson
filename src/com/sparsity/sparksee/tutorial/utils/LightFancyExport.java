/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sparsity.sparksee.tutorial.utils;

import com.sparsity.sparksee.gdb.EdgeExport;
import com.sparsity.sparksee.gdb.ExportManager;
import com.sparsity.sparksee.gdb.Graph;
import com.sparsity.sparksee.gdb.GraphExport;
import com.sparsity.sparksee.gdb.NodeExport;
import com.sparsity.sparksee.gdb.NodeShape;
import com.sparsity.sparksee.gdb.Type;
import com.sparsity.sparksee.gdb.Value;
import java.awt.Color;
import java.text.Normalizer;

/**
     * Update this Export implementation to update the resulting
     * visualization.
     *
     * @see ExportManager
     * @see NodeExport
     * @see EdgeExport
     * @see GraphExport
     */
    public class LightFancyExport extends ExportManager {

        private Graph g = null;
        private int userType = Type.InvalidType;
        private int messageType = Type.InvalidType;
        private int tagType = Type.InvalidType;
        private int nicknameType = Type.InvalidType;
        private int bodyType = Type.InvalidType;
        private int tagNameType = Type.InvalidType;
        private int knowsType = Type.InvalidType;

        @Override
        public void prepare(Graph graph) {
            g = graph;
            userType = g.findType("User");
            messageType = g.findType("Message");
            tagType = g.findType("Hashtag");
            knowsType = g.findType("knows");
            
            if( userType != Type.InvalidType ) {
                nicknameType = g.findAttribute(userType, "nickname");
            }
            
            if( messageType != Type.InvalidType ) {
                bodyType = g.findAttribute(messageType, "body");
            }
                               
            if( tagType != Type.InvalidType ) {
                tagNameType = g.findAttribute(tagType, "name");
            }
            
        }

        @Override
        public void release() {
        }

        @Override
        public boolean getGraph(GraphExport ge) {
            ge.setDefaults();
            // call setters
            return true;
        }

        @Override
        public boolean getNodeType(int nodetype, NodeExport ne) {
            ne.setDefaults();
            // call setters
            return true;
        }

        @Override
        public boolean getEdgeType(int i, EdgeExport ee) {
            ee.setDefaults();
            
            // call setters
            return true;
        }

        @Override
        public boolean getNode(long nodeOID, NodeExport ne) {
            ne.setDefaults();
            // call setters
            int nodeType = g.getObjectType(nodeOID);
            if (userType == nodeType ) {
                Value v = g.getAttribute(nodeOID, nicknameType );
                if(v!=null)
                    ne.setLabel(normalize(v.getString()));
                ne.setShape(NodeShape.Round);
                ne.setColor(Color.RED);
                return true;
            }
            
            if (messageType == nodeType ) {
                Value v = g.getAttribute(nodeOID, bodyType );
                if(v!=null)
                    ne.setLabel(normalize(v.getString()));
                ne.setShape(NodeShape.Box);
                ne.setColor(Color.ORANGE);
                return true;
            }
            
            if (tagType == nodeType ) {
                Value v = g.getAttribute(nodeOID, tagNameType );
                if(v!=null)
                    ne.setLabel(normalize(v.getString()));
                ne.setShape(NodeShape.Round);
                ne.setColor(Color.CYAN);
                return true;
            }
            
            return false;
        }

        @Override
        public boolean getEdge(long edgeOID, EdgeExport ee) {
            ee.setDefaults();
            // call setters
            Type t = g.getType(g.getObjectType(edgeOID));
            ee.setLabel("  ");
            ee.setWidth(1);
            ee.setAsDirected(false);
            return true;
        }

        @Override
        public boolean enableType(int i) {
            // enable all node and edge types
            return i == userType || i == knowsType;
        }
        
        private String normalize(String s) {
            s = Normalizer.normalize(s,Normalizer.Form.NFD);
            s= s.replaceAll("[^a-zA-Z0-9\\s@#\\/]", "");
            return s;
        }
    }
