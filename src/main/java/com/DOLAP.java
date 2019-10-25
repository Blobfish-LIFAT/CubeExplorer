package com;

import com.olap3.cubeexplorer.Compatibility;
import com.olap3.cubeexplorer.StudentParser;
import com.olap3.cubeexplorer.castor.session.CrSession;
import com.olap3.cubeexplorer.castor.session.QueryRequest;
import com.olap3.cubeexplorer.im_olap.model.Query;
import com.olap3.cubeexplorer.im_olap.model.QueryPart;
import com.olap3.cubeexplorer.im_olap.model.Session;
import com.olap3.cubeexplorer.info.CheckParents;
import com.olap3.cubeexplorer.info.CheckRestriction;
import com.olap3.cubeexplorer.info.MDXAccessor;
import com.olap3.cubeexplorer.info.MLModelFactory;
import com.olap3.cubeexplorer.julien.ProjectionFragment;
import com.olap3.cubeexplorer.julien.Qfset;
import com.olap3.cubeexplorer.julien.SelectionFragment;
import com.olap3.cubeexplorer.mondrian.CubeUtils;
import com.olap3.cubeexplorer.mondrian.MondrianConfig;
import mondrian.olap.Connection;
import mondrian.olap.Level;
import mondrian.olap.Member;

import java.util.*;
import java.util.logging.Logger;

public class DOLAP {
    private static final Logger LOGGER = Logger.getLogger(DOLAP.class.getName());
    static final String testData = "./data/studentSessions";
    static CubeUtils utils;

    public static void main(String[] args) {
        LOGGER.info("Init Starting");
        Connection olap = MondrianConfig.getMondrianConnection();
        utils = new CubeUtils(olap, "Cube1MobProInd");
        CubeUtils.setDefault(utils);
        LOGGER.info("DB Connection init complete");

        LOGGER.info("Loading test data from " + testData);
        var rawSessions = StudentParser.loadDir(testData);
        var sessions = convertFromCr(rawSessions);

        LOGGER.info("Computing interestigness scores");
        //TODO

        LOGGER.info("[BEGIN] Tests");

    }

    public static Set<MDXAccessor> generateCandidates(Qfset q0){
        Set<MDXAccessor> queries = new HashSet<>();

        // Build roll-ups
        for (ProjectionFragment f : q0.getAttributes()){
            ProjectionFragment p  = ProjectionFragment.newInstance(f.getLevel().getParentLevel());

            HashSet<ProjectionFragment> tmp = new HashSet<>(q0.getAttributes());
            tmp.remove(f); tmp.add(p);

            Qfset query = new Qfset(tmp, new HashSet<>(q0.getSelectionPredicates()), new HashSet<>(q0.getMeasures()));
            queries.add(new MDXAccessor(query));
        }

        // Build drill-downs
        for (var sf : q0.getAttributes()){
            Level target = sf.getLevel().getChildLevel();
            var tmp = new HashSet<>(q0.getAttributes());
            tmp.add(new ProjectionFragment(target));

            Qfset req = new Qfset(tmp, new HashSet<>(), new HashSet<>(q0.getMeasures()));
            queries.add(new MDXAccessor(req));
        }

        // Build siblings
        for (var sel : q0.getSelectionPredicates()){
            Level target = sel.getLevel();
            Member original = sel.getValue();

            for (var other : utils.fetchMembers(target)){
                if (other.equals(original))
                    continue;
                var tmp = new HashSet<>(q0.getSelectionPredicates());
                tmp.remove(original);
                tmp.add(new SelectionFragment(other));
                Qfset req = new Qfset(new HashSet<>(q0.getAttributes()), tmp, new HashSet<>(q0.getMeasures()));
                queries.add(new MDXAccessor(req));
            }
        }

        return queries;
    }

    public static List<Session> convertFromCr(List<CrSession> sessions){
        ArrayList<Session> outSess = new ArrayList<>(sessions.size());

        for (CrSession in : sessions){
            ArrayList<Query> queries = new ArrayList<>(in.getQueries().size());
            for (QueryRequest qr : in.getQueries()){
                queries.add(new Query(Compatibility.partsFromQfset(Compatibility.QfsetFromMDX(qr.getQuery()))));
            }
            Session current = new Session(queries, in.getUser().getName(), in.getTitle()); // TODO check properties we want
            outSess.add(current);
        }

        return outSess;
    }
}
