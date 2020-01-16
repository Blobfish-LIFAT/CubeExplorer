/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.olap3.cubeexplorer.model.legacy;

import com.olap3.cubeexplorer.model.MeasureFragment;
import com.olap3.cubeexplorer.model.ProjectionFragment;
import com.olap3.cubeexplorer.model.Qfset;
import com.olap3.cubeexplorer.model.SelectionFragment;
import com.olap3.cubeexplorer.mondrian.CubeUtils;
import com.olap3.cubeexplorer.mondrian.MondrianConfig;
import mondrian.olap.Query;
import mondrian.olap.*;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author mahfoud and Alex who made this working without the dependency fiesta
 */
public class MdxToTripletConverter {
    CubeUtils utils;

    public MdxToTripletConverter(CubeUtils utils) {
        this.utils = utils;
    }

    //Don't use this not working obviously
    @Deprecated
    public Qfset convert(String q_mdx) {
        Qfset result = new Qfset();
        
        this.extractProjectionFragments(q_mdx, result);
        this.extractMeasures(q_mdx, result);
        //this.extractProjectionFragmentsWithoutExecution(q_mdx, result); // only based on query text
        this.extractSelectionFragments(q_mdx, result);

        return result;
    }
    
    /**
     * Extracts measures from query text of qrg_qmdx, and add it to arg_qt.
     * 
     * @param arg_qmdx
     * @param arg_qt 
     */
    public void extractMeasures(String arg_qmdx, Qfset arg_qt) {
        
        Set<String> patterns   = this.extractPatterns(arg_qmdx);
        
        for(String p_tmp : patterns) {
            
            if(p_tmp.contains("Measures")) {
                
                Integer start   = p_tmp.lastIndexOf("[");
                Integer end     = p_tmp.lastIndexOf("]");
                
                String measureName  = p_tmp.substring(start+1, end);
                
//                System.out.println("Measure name retrouve: " + measureName);
                
                // make this check just to ensure that
                Member m = utils.getMeasure(measureName);
                if(m != null) {
                    MeasureFragment mf      = MeasureFragment.newInstance(m);
                    arg_qt.addMeasure(mf);
                }
                
            }
                
        }

        // add the default measure if not specified
        if(arg_qt.getMeasures().isEmpty()) {
            MeasureFragment mf_tmp  = MeasureFragment.newInstance(utils.getDefaultMeasure());
            arg_qt.addMeasure(mf_tmp);
        }
    }

    
    /**
     * 
     * @param arg_qmdx
     * @param arg_qt 
     */
    public void extractProjectionFragments(String arg_qmdx, Qfset arg_qt) {

        Connection con = MondrianConfig.getMondrianConnection();
        Query q =con.parseQuery(arg_qmdx);
        mondrian.olap.Result mondrianResult = con.execute(q);
        
        Axis slicerAxis = mondrianResult.getSlicerAxis();
        Axis columns    = mondrianResult.getAxes()[0];
        
        // if columns has no position, it means result is empty
        if(columns.getPositions().isEmpty()) {
            return;
        }

//        System.out.println("En colonnes...");
        Position p_column   = columns.getPositions().iterator().next();
        for(Member m_tmp : p_column) {
//            System.out.println("Member: " + m_tmp);
            if(m_tmp.getHierarchy().getDimension().isMeasures()) {
                this.addMeasure(arg_qt, m_tmp);
            } else {
                this.addProjection(arg_qt, m_tmp);
            }
        }
        
        // if there is anothe axis (i.e. line axis)
        if (mondrianResult.getAxes().length == 2) {
//            System.out.println("En lignes...");
            Axis rows       = mondrianResult.getAxes()[1];
            Position p_row  = rows.getPositions().iterator().next();
            for(Member m_tmp : p_row) {
//                System.out.println("Member: " + m_tmp);
                if(m_tmp.getHierarchy().getDimension().isMeasures()) {
                    this.addMeasure(arg_qt, m_tmp);
                } else {
                    this.addProjection(arg_qt, m_tmp);
                }
            }
        }
    }
    

    public Set<String> extractPatterns(String mdx) {
        Set<String> result = new HashSet<>();

        
        // pattern for extracting cube name
        Pattern p = Pattern.compile("((\\[.*?\\])(\\.\\[.*?\\]){0,})");
//        System.out.println("Pattern: " + p.pattern());
        
        // Normally, the pattern is always found!
        Matcher m = p.matcher(mdx);
        
        while (m.find()) {
            for (int i = 0; i < m.groupCount(); i++) {
                result.add(m.group(i));
            }
        }
        
//        System.out.println("Hash set:");
//        System.out.println(result);
        
        return result;
    }
    
    /**
     * 
     * @param arg_qmdx
     * @param arg_qt 
     */
    public void extractSelectionFragments(String arg_qmdx, Qfset arg_qt) {
        
        Set<String> patterns   = this.extractPatterns(arg_qmdx);
        
        for(String p_tmp : patterns) {
            // if parsed string represents a member
            Member m = utils.getMember(p_tmp);
            if(m != null) {
                SelectionFragment sf    = SelectionFragment.newInstance(m);
                arg_qt.addSelection(sf);
            }
        }
        
    }
    
    /**
     * 
     * @param arg_qt
     * @param arg_mondrianMember 
     */
    public void addProjection(Qfset arg_qt, mondrian.olap.Member arg_mondrianMember) {
        ProjectionFragment pf   = ProjectionFragment.newInstance(arg_mondrianMember.getLevel());
        // only add the projection if it is more detailed
        arg_qt.addProjection(pf);
    }

    /**
     *
     * @param arg_qt
     * @param arg_mondrianMember
     */
    public void addSelection(Qfset arg_qt, mondrian.olap.Member arg_mondrianMember) {
        SelectionFragment sf    = SelectionFragment.newInstance(arg_mondrianMember);
        arg_qt.addSelection(sf);
    }

    /**
     *
     * @param arg_qt
     * @param arg_mondrianMember
     */
    public void addMeasure(Qfset arg_qt, mondrian.olap.Member arg_mondrianMember) {
        MeasureFragment mf  = MeasureFragment.newInstance(arg_mondrianMember);
        arg_qt.addMeasure(mf);
    }
    
}
