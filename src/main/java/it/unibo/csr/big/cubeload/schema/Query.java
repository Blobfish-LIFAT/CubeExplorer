package it.unibo.csr.big.cubeload.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Query {
    /**
     * Class fields
     */
    private List<GroupByElement> groupBySet = new ArrayList<GroupByElement>();
    private List<Measure> measures = new ArrayList<Measure>();
    private List<SelectionPredicate> predicates = new ArrayList<SelectionPredicate>();
    Random rand = new Random();

    /**
     * Getter method for this query's group-by set.
     *
     * @return The group-by set of the current query.
     */
    public List<GroupByElement> getGroupBySet() {
        return groupBySet;
    }

    /**
     * Getter method for the current query's measure list.
     *
     * @return The list of measures of this query.
     */
    public List<Measure> getMeasures() {
        return measures;
    }

    /**
     * Setter method for the current query's measure list.
     *
     * @param measureList The list of measures we set for this query.
     */
    public void setMeasures(List<Measure> measureList) {
        this.measures = measureList;
    }

    /**
     * Getter method for the current query's selection predicate list.
     *
     * @return The list of selection predicates of this query.
     */
    public List<SelectionPredicate> getPredicates() {
        return predicates;
    }

    /**
     * This method adds a group-by element to the current query's group-by set.
     *
     * @param hierarchy The group-by element's hierarchy.
     * @param level     The group-by element's level.
     */
    public void addGroupByElement(Hierarchy hierarchy, Level level) {
        groupBySet.add(new GroupByElement(hierarchy, level));
    }

    /**
     * This method adds a measure to the current query's measure list.
     *
     * @param measure The measure to be added to the measure list.
     */
    public void addMeasure(Measure measure) {
        measures.add(measure);
    }

    /**
     * This method adds a new selection predicate to the query's predicates' list.
     *
     * @param selPred The selection predicate to be added.
     */
    public void addSelectionPredicate(SelectionPredicate selPred) {
        predicates.add(selPred);
    }

    /**
     * This method checks if the current query contains a
     * selection predicate on the given hierarchy.
     *
     * @param hierarchy The name of the hierarchy we want to check for selection predicates.
     * @return True if a selection predicate on the given hierarchy exists, False otherwise.
     */
    public boolean containsPredicateOn(String hierarchy) {
        boolean present = false;

        for (SelectionPredicate sel : predicates) {
            if (sel.getHierarchy().equals(hierarchy)) {
                present = true;
                break;
            }
        }

        return present;
    }

    /**
     * This method looks for a selection predicate set on a given hierarchy name.
     *
     * @param hierarchy The hierarchy on which is set the selection predicate we need.
     * @return The selection predicate set on the given hierarchy.
     */
    public SelectionPredicate getSelectionPredicate(String hierarchy) {
        for (SelectionPredicate sel : predicates) {
            if (sel.getHierarchy().equals(hierarchy)) {
                return sel;
            }
        }

        return null;
    }

    /**
     * This method creates a literal representation of a query
     * (group-by elements, selection predicates and measures).
     *
     * @return The concatenation of all components of the query.
     */
    public String printQuery() {
        return (buildGroupBy() + "\n" + buildSelectionPredicates() + "\n" + buildMeasures() + "\n");
    }

    /**
     * This method iterates through the group-by set
     * to concatenate all the hierarchy levels involved.
     *
     * @return The concatenation of the current query's visible group-by elements.
     */
    private String buildGroupBy() {
        String finalString = "";

        for (int i = 0; i < groupBySet.size(); ++i) {
            GroupByElement gb = groupBySet.get(i);

            if (gb.getVisible()) {
                finalString = finalString + gb.getHierarchy() + "." + gb.getLevel();

                if (i != groupBySet.size() - 1) {
                    finalString = finalString + ", ";
                }
            }
        }

        return finalString;
    }

    /**
     * This method iterates through the set of selection
     * predicates to concatenate them in a string.
     *
     * @return The concatenation of the current query's selection predicates.
     */
    private String buildSelectionPredicates() {
        String finalString = "";

        for (int i = 0; i < predicates.size(); ++i) {
            finalString += predicates.get(i).getSelectionPredicate();

            if (i != predicates.size() - 1) {
                finalString += ", ";
            }
        }

        return finalString;
    }

    /**
     * This method iterates through the measures to concatenate all of them.
     *
     * @return The concatenation of the current query's measures.
     */
    private String buildMeasures() {
        String finalString = "";

        for (int i = 0; i < measures.size(); ++i) {
            finalString += measures.get(i).getName();

            if (i != measures.size() - 1) {
                finalString += ", ";
            }
        }

        return finalString;
    }

    /**
     * This method looks for the current query's group-by
     * element on a particular hierarchy, to get its level.
     *
     * @param hierarchy The hierarchy whose level is needed.
     * @return The current query's level on the given hierarchy.
     */
    public String findLevel(String hierarchy) {
        for (GroupByElement gb : this.groupBySet) {
            if (gb.getHierarchy().equals(hierarchy)) {
                return gb.getLevel();
            }
        }

        return null;
    }

    /**
     * This method sets the level for a particular hierarchy.
     *
     * @param hierarchy The hierarchy that will be modified.
     * @param level     The name of the level we set on the hierarchy;
     */
    public void setLevel(String hierarchy, String level) {
        for (GroupByElement gb : this.groupBySet) {
            if (gb.getHierarchy().equals(hierarchy)) {
                gb.setLevel(level);
            }
        }
    }

    /**
     * This method gets the visibility for a particular hierarchy.
     *
     * @param hierarchy The hierarchy we're checking for visibility.
     * @return The visibility of the given hierarchy.
     */
    public boolean getVisibility(String hierarchy) {
        for (GroupByElement gb : this.groupBySet) {
            if (gb.getHierarchy().equals(hierarchy)) {
                return gb.getVisible();
            }
        }

        return false;
    }

    /**
     * This method sets the visibility for the given hierarchy
     *
     * @param hierarchy The hierarchy that will be modified.
     * @param value     The flag we're setting for the given hierarchy.
     */
    private void setVisibility(String hierarchy, boolean value) {
        for (GroupByElement gb : this.groupBySet) {
            if (gb.getHierarchy().equals(hierarchy)) {
                gb.setVisible(value);
            }
        }
    }

    /**
     * This method checks if a particular Hierarchy level
     * in the current query is the root of the Hierarchy.
     *
     * @param hierarchy The hierarchy we want to drill-down on.
     * @return True if drill-down is possible, False otherwise.
     */
    public boolean isDescendible(Hierarchy hierarchy) {
        String level = findLevel(hierarchy.getName());
        int position = hierarchy.findPosition(level);

        return (position == 0 ? false : true);
    }

    /**
     * The method modifies a query by applying a drill-down
     * operator on one of its group-by elements.
     *
     * @param hierarchy The hierarchy to be modified.
     */
    public void descendHierarchy(Hierarchy hierarchy) {
        String level = findLevel(hierarchy.getName());
        int position = hierarchy.findPosition(level);

        String newLevel = hierarchy.getLevel(--position).getName();
        this.setLevel(hierarchy.getName(), newLevel);
    }

    /**
     * This method checks if a query can be modified increasing the aggregation on a given
     * hierarchy, considering the query's levels and the presence of selection predicates.
     *
     * @param hierarchy The hierarchy we want to roll-up on.
     * @return True if the roll-up is possible, False otherwise.
     */
    public boolean isAscendable(Hierarchy hierarchy) {
        boolean ascendable = false;
        String hierarchyName = hierarchy.getName();

        String queryLevel = findLevel(hierarchy.getName());
        int queryPosition = hierarchy.findPosition(queryLevel);

        if (hierarchy.isMaxLevel(queryLevel)) // Maximum aggregation, can't be ascended
        {
            ascendable = false;
        } else // Aggregation level not maximum
        {
            if (containsPredicateOn(hierarchyName)) // A selection predicate exists
            {
                SelectionPredicate selPred = getSelectionPredicate(hierarchyName); // Get the predicate

                String selectionLimit = selPred.getLevel(); // Get the predicate's level
                int predicatePosition = hierarchy.findPosition(selectionLimit); // Get the level's position

                // If at least an attribute exists between the query's
                // hierarchy level and the selection predicate level
                if (queryPosition < predicatePosition - 1) {
                    ascendable = true;
                } else // No room for aggregation
                {
                    ascendable = false;
                }
            } else // No selection predicate on the hierarchy, can be ascended
            {
                ascendable = true;
            }
        }

        return ascendable;
    }

    /**
     * The method modifies a query by applying a roll-up
     * operator on one of its group-by elements.
     *
     * @param hierarchy The hierarchy on which we apply a roll-up.
     */
    public void ascendHierarchy(Hierarchy hierarchy) {
        String level = findLevel(hierarchy.getName());
        int position = hierarchy.findPosition(level);

        String newLevel = hierarchy.getLevel(++position).getName();
        this.setLevel(hierarchy.getName(), newLevel);
    }


    /**
     * This method sets the external visibility of the members
     * of a query's group-by set. In order to avoid functional
     * dependencies, only "safe" levels are flagged as visible.
     *
     * @param cube The object containing the cube's information and data.
     */
    public void checkValidity(Cube cube) {
        List<Hierarchy> hieList = null;
        Hierarchy tempHierarchy;
        boolean isVisible;

        for (Dimension dim : cube.getDimensions()) {
            if (dim.getHierarchyCount() == 1) {
                this.setVisibility(dim.getHierarchy(0).getName(), true);
            } else {
                hieList = dim.getHierarchies();

                for (int i = 0; i < hieList.size(); ++i) {
                    isVisible = true;
                    tempHierarchy = hieList.get(i);

                    for (int j = 0; j < hieList.size() && j != i; ++j) {
                        if (tempHierarchy.isAncestor(this.findLevel(hieList.get(i).getName()),
                                this.findLevel(hieList.get(j).getName()))) {
                            this.setVisibility(tempHierarchy.getName(), false);
                            isVisible = false;
                            break;
                        }
                    }

                    if (isVisible) {
                        this.setVisibility(tempHierarchy.getName(), true);
                    }
                }
            }
        }
    }

    /**
     * This method gets the size of the current queryreport
     * by multiplying the cardinality of the single levels
     * involved in the query's visible hierarchies.
     *
     * @param cube The object containing the cube's information and data.
     * @return The size of the current query report.
     */
    public int getReportSize(Cube cube) {
        String currentLevel;
        int position, reportSize = 1;

        for (Hierarchy hie : cube.getHierarchies()) {
            if (this.getVisibility(hie.getName())) {
                if (this.containsPredicateOn(hie.getName())) {
                    SelectionPredicate selPred = this.getSelectionPredicate(hie.getName());
                    String level = selPred.getLevel();
                    int selectivePosition = hie.findPosition(level);
                    int selectiveDistinctValues = hie.getLevel(selectivePosition).getValuesCount();

                    currentLevel = this.findLevel(hie.getName());
                    position = hie.findPosition(currentLevel);
                    int distinctValues = hie.getLevel(position).getValuesCount();
                    reportSize *= (int) (distinctValues / selectiveDistinctValues);
                } else {
                    currentLevel = this.findLevel(hie.getName());
                    position = hie.findPosition(currentLevel);
                    reportSize *= hie.getLevel(position).getValuesCount();
                }
            }
        }

        return reportSize;
    }

    /**
     * This method calculates a distance between the current query
     * and a given one: two queries are "distant" by means of
     * hierarchy level difference and measure sets: moreover, if the
     * selection predicates do not match, the distance increases.
     *
     * @param query The query we want the current query to compare with.
     * @param cube  The object containing the cube's information and data.
     * @return The calculated distance between the current query and the given one.
     */
    public int distanceFrom(Query query, Cube cube) {
        int distance = 0, position_1, position_2;
        String hierarchyName;
        Hierarchy tempHie;

        // The distance increases for every inequality
        // between the queries' hierarchy levels
        for (GroupByElement gb : this.groupBySet) {
            hierarchyName = gb.getHierarchy();

            if (this.getVisibility(hierarchyName) &&
                    query.getVisibility(hierarchyName)) {
                tempHie = cube.findHierarchy(hierarchyName);
                position_1 = tempHie.findPosition(this.findLevel(hierarchyName));
                position_2 = tempHie.findPosition(query.findLevel(hierarchyName));

                distance += Math.abs(position_1 - position_2);
            }
        }

        // The distance is further increased by measures discrepancy
        if (measuresMismatch(query.measures)) {
            ++distance;
        }

        // The distance is also increased if the selection predicates do not match
        if (predicatesMismatch(query)) {
            ++distance;
        }

        return distance;
    }

    /**
     * This method checks if the current query's measures
     * and the given measure list contain the same measures.
     *
     * @param measList The list of measures to compare with the current query's measure list.
     * @return True if the lists do not match, False otherwise.
     */
    private boolean measuresMismatch(List<Measure> measList) {
        for (Measure meas : this.measures) {
            if (!measList.contains(meas)) {
                return true;
            }
        }

        return false;
    }

    /**
     * This method checks if the current query's selection
     * predicate list can match (or quasi-match) the given list.
     *
     * @param queryToMatch The query we want to compare to
     *                     the current one by means of selection predicates.
     * @return True if the lists do not match, False otherwise.
     */
    private boolean predicatesMismatch(Query queryToMatch) {
        boolean different = false;
        List<SelectionPredicate> initialPredicates = new ArrayList<SelectionPredicate>();
        List<SelectionPredicate> finalPredicates = new ArrayList<SelectionPredicate>();
        String segregatedHierarchy = null, tempHierarchy;
        SelectionPredicate tempSel;

        for (SelectionPredicate selPred : this.predicates) {
            initialPredicates.add(selPred);
        }

        for (SelectionPredicate selPred : queryToMatch.predicates) {
            finalPredicates.add(selPred);
        }

        if (this.isSegregated() && !queryToMatch.isSegregated()) // I'm in a template Explorative
        {
            // I don't want to consider the segregation predicate...
            for (SelectionPredicate sel : initialPredicates) {
                if (sel.getSegregation()) {
                    segregatedHierarchy = sel.getHierarchy();
                    initialPredicates.remove(sel);
                    break;
                }
            }

            // ... nor a selection predicate on its hierarchy (if any)
            if (queryToMatch.containsPredicateOn(segregatedHierarchy)) {
                finalPredicates.remove(queryToMatch.getSelectionPredicate(segregatedHierarchy));
            }
        }

        if (initialPredicates.size() != finalPredicates.size()) // The predicates' lists differ in size
        {
            different = true;
        } else {
            for (SelectionPredicate sel : initialPredicates) // Scan the first query's predicates
            {
                tempHierarchy = sel.getHierarchy();

                if (!queryToMatch.containsPredicateOn(tempHierarchy)) // No predicates on such hierarchy
                {
                    different = true;
                    break;
                } else {
                    tempSel = queryToMatch.getSelectionPredicate(tempHierarchy);

                    if (sel.matchesWith(tempSel)) // These predicates match completely
                    {
                        continue;
                    } else // Mismatch
                    {
                        different = true;
                        break;
                    }
                }
            }
        }

        return different;
    }

    /**
     * This method tries to alter a query to increase its similarity
     * with another given query. The alteration can be a change in the
     * measures set or in a hierarchy (both group-by and predicates).
     *
     * @param queryToConverge The query towards which we want the current query to evolve.
     * @param cube            The object containing the cube's information and data.
     * @return True if an alteration is possible, False otherwise.
     */
    public boolean convergeTo(Query queryToConverge, Cube cube) {
        // "choice" will decide the alteration: 0 for group-by set/predicates, 1 for measures
        int choice = rand.nextInt(2);
        boolean modified = false, equalMeasures = false, equalGroupBy = false;
        SelectionPredicate currentPredicate, matchPredicate;

        if (!measuresMismatch(queryToConverge.measures)) // If measures already match
        {
            choice = 0;
            equalMeasures = true;
        }

        List<Hierarchy> modifiableHierarchies = new ArrayList<Hierarchy>();

        for (Hierarchy hie : cube.getHierarchies()) // Scanning total hierarchies
        {
            if (!this.hierarchyMatch(queryToConverge, hie)) // If there's room for improvement
            {
                modifiableHierarchies.add(hie);
            }
        }

        if (modifiableHierarchies.size() == 0) // Group-by set cannot be improved
        {
            equalGroupBy = true;
            choice = 1;
        }

        if (equalMeasures && equalGroupBy)// The current query is already closest to the given one
        {
            modified = false;
        } else {
            if (choice == 0) // Group-by set/predicates change
            {
                Hierarchy hie = modifiableHierarchies.get(rand.nextInt(modifiableHierarchies.size()));
                String hieName = hie.getName();

                String levelToSet = queryToConverge.findLevel(hieName);
                this.setLevel(hieName, levelToSet);

                /*
                 * Cases:
                 *
                 *      Current query			   Query to match
                 * -----------------------------------------------------
                 * (1)  Segregation Predicate      Selection Predicate
                 * (2)  Segregation Predicate      Segregation Predicate
                 * (3)  Selection Predicate		   Selection Predicate
                 * (4)  Segregation Predicate
                 * (5)  Selection Predicate
                 * (6)         					   Selection Predicate
                 * (7)
                 *
                 * Does the current query have a predicate on the hierarchy?
                 * - Yes (1, 2, 3, 4, 5) -> Is it a segregation predicate?
                 * 							- Yes (1, 2, 4) -> Do nothing.
                 * 							- No (3, 5) -> Remove predicate.
                 * 										   Does the query to match have a predicate?
                 * 									  				  - Yes (3) -> Add it in the current query.
                 * 													  - No (5) -> Do nothing.
                 * - No (6, 7) -> Does the query to match have a predicate?
                 * 				  - Yes (6) -> Add it in the current query.
                 * 				  - No (7) -> Do nothing.
                 */

                if (this.containsPredicateOn(hieName)) {
                    currentPredicate = this.getSelectionPredicate(hieName);

                    if (!currentPredicate.getSegregation()) {
                        this.predicates.remove(currentPredicate);

                        if (queryToConverge.containsPredicateOn(hieName)) {
                            matchPredicate = queryToConverge.getSelectionPredicate(hieName);

                            this.predicates.add(matchPredicate);
                        }
                    }
                } else {
                    if (queryToConverge.containsPredicateOn(hieName)) {
                        matchPredicate = queryToConverge.getSelectionPredicate(hieName);

                        this.predicates.add(matchPredicate);
                    }
                }

                modified = true;
            } else // Measures change
            {
                measures = queryToConverge.measures;
                modified = true;
            }
        }

        return modified;
    }

    /**
     * This method checks if a query contains an year prompt.
     *
     * @return True if there's an year prompt, False otherwise.
     */
    public boolean isPrompted() {
        for (SelectionPredicate sel : this.predicates) {
            if (sel.getPrompt()) {
                return true;
            }
        }

        return false;
    }

    /**
     * This method checks if the current query contains a segregation predicate.
     *
     * @return True if there's a segregation predicate, False otherwise.
     */
    public boolean isSegregated() {
        for (SelectionPredicate sel : predicates) {
            if (sel.getSegregation()) {
                return true;
            }
        }

        return false;
    }

    /**
     * This method checks if the current query contains
     * a segregation predicate on the given hierarchy.
     *
     * @param hierarchy The hierarchy we're checking for segregation.
     * @return True if there's segregation on the given hierarchy, False otherwise.
     */
    public boolean isSegregatedOn(Hierarchy hierarchy) {
        for (SelectionPredicate sel : predicates) {
            if (sel.getHierarchy().equals(hierarchy.getName()) && sel.getSegregation()) {
                return true;
            }
        }

        return false;
    }

    /**
     * This method modifies a query group-by set. It selects a random hierarchy and alters
     * the query's level in that hierarchy. Finally, a validity check is performed.
     *
     * @param cube The object containing the cube's information and data.
     */
    private void changeGroupBy(Cube cube) {
        List<Hierarchy> modifiableHierarchies = new ArrayList<Hierarchy>();

        for (Hierarchy hie : cube.getHierarchies()) {
            if (this.isAscendable(hie) || this.isDescendible(hie)) {
                modifiableHierarchies.add(hie);
            }
        }

        if (modifiableHierarchies.size() == 0) {
            return;
        }

        // Getting a random modifiable hierarchy
        Hierarchy hie = modifiableHierarchies.get(rand.nextInt(modifiableHierarchies.size()));

        if (!isDescendible(hie)) // Minimum aggregation
        {
            ascendHierarchy(hie); // => roll-up
        } else if (!isAscendable(hie)) // Maximum aggregation
        {
            descendHierarchy(hie); // => drill-down
        } else // Hierarchy's level is intermediate
        {
            if (rand.nextBoolean()) // Random choice
            {
                ascendHierarchy(hie); // => roll-up
            } else {
                descendHierarchy(hie); // => drill-down
            }
        }

        checkValidity(cube); // Integrity check on the group-by set
    }

    /**
     * This method alters the set of selection predicates in the current query. If no
     * predicates can be removed, one is added; if no predicates can be added, one is removed.
     *
     * @param cube The object containing the cube's information and data.
     */
    private void changePredicates(Cube cube) {
        boolean addable = true, removable = true, choice;

        // If no predicates exist, or there's only a segregation
        // predicate, a predicate must be added
        if (predicates.isEmpty() ||
                predicates.size() == 1 && predicates.get(0).getSegregation()) {
            removable = false;
        }

        // If there is a maximum number of selection predicates (equal
        // to the number of hierarchies), a predicate must be removed
        if (predicates.size() == cube.getTotalHierarchyCount()) {
            addable = false;
        }

        List<Hierarchy> selectableHierarchies = new ArrayList<Hierarchy>();
        for (Hierarchy hie : cube.getHierarchies()) {
            if (!this.containsPredicateOn(hie.getName())) {
                String level = this.findLevel(hie.getName());
                int position = hie.findPosition(level);

                if (position <= hie.getLevelCount() - 3) {
                    selectableHierarchies.add(hie);
                }
            }
        }

        // If no selection predicates can be added due to query level
        // positions, a predicate must be removed
        if (selectableHierarchies.size() == 0) {
            addable = false;
        }

        // Based on the previous conditions, the 'choice' is set
        if (removable && addable) // I can both remove and add a predicate, choice is randomly set
        {
            choice = rand.nextBoolean();
        } else if (removable && !addable) // I can only remove a predicate
        {
            choice = false;
        } else if (!removable && addable) // I can only add a predicate
        {
            choice = true;
        } else // I can't remove nor add predicates, must return
        {
            return;
        }

        if (choice) // A selection predicate is added
        {
            Hierarchy hierarchy = selectableHierarchies.get(rand.nextInt(selectableHierarchies.size()));
            String queryLevel = this.findLevel(hierarchy.getName());
            int position = hierarchy.findPosition(queryLevel);
            Level level = hierarchy.getLevel(hierarchy.getValidPredicatePosition(position));

            predicates.add(new SelectionPredicate(hierarchy.getName(),
                    level.getName(),
                    level.getRandomValue(),
                    false,
                    false));

        } else // A selection predicate is removed (can't be a segregation predicate)
        {
            SelectionPredicate tempSel;

            do {
                tempSel = predicates.get(rand.nextInt(predicates.size()));
            } while (tempSel.getSegregation());

            predicates.remove(tempSel);
        }
    }

    /**
     * This method alters a query measure list, removing
     * one and adding another, previously absent.
     *
     * @param cube The object containing the cube's information and data.
     */
    private void changeMeasures(Cube cube) {
        Measure oldMeasure = measures.get(rand.nextInt(measures.size()));
        measures.remove(oldMeasure);

        List<String> measureList = new ArrayList<String>();

        for (Measure meas : measures) {
            measureList.add(meas.getName());
        }

        Measure newMeasure;

        do {
            newMeasure = cube.getMeasure(rand.nextInt(cube.getMeasureCount()));
        } while (newMeasure.getName().equals(oldMeasure.getName()) ||
                measureList.contains(newMeasure.getName()));

        measures.add(newMeasure);
    }

    /**
     * This method checks whether the current query can
     * converge to the given query on the selected hierarchy.
     *
     * @param queryToMatch The query we want the current query to evolve into.
     * @param hierarchy    The hierarchy on which we're checking if a match is possible.
     */

    private boolean hierarchyMatch(Query queryToMatch, Hierarchy hierarchy) {
        boolean match = false;
        String hierarchyName = hierarchy.getName();
        int position_1 = hierarchy.findPosition(this.findLevel(hierarchyName));
        int position_2 = hierarchy.findPosition(queryToMatch.findLevel(hierarchyName));
        SelectionPredicate currentPredicate, matchPredicate;

        /*
         * Are there predicates?
         * - Yes -> Are there two predicates?
         *   		- Yes -> Are both predicates segregation predicates?
         *   				 - Yes -> Are levels in the same position?
         *   						  - Yes -> Match
         *   						  - No -> Mismatch
         *   				 - No -> Is the current query's predicate a segregation predicate?
         *   						 - Yes -> Is it possible to navigate the hierarchy towards
         *  						 		  the level of the query to match?
         *  								  - Yes -> Mismatch
         *  								  - No -> Forced match
         *   						 - No -> Are levels in the same position?
         *   						  		 - Yes -> Match
         *   						  		 - No -> Mismatch
         *  	    - No -> Is the predicate a segregation predicate in the current query?
         *  				- Yes -> Is it possible to navigate the hierarchy towards
         *  						 the level of the query to match?
         *  						 - Yes -> Mismatch
         *  						 - No -> Forced match
         *  				- No -> Mismatch
         * - No -> Are levels in the same position?
         *         - Yes -> Match
         *         - No -> Mismatch
         */

        if (this.containsPredicateOn(hierarchyName) ||
                queryToMatch.containsPredicateOn(hierarchyName)) // There's at least a selection predicate
        {
            if (this.containsPredicateOn(hierarchyName) &&
                    queryToMatch.containsPredicateOn(hierarchyName)) // Both queries have predicates
            {
                currentPredicate = this.getSelectionPredicate(hierarchyName);
                matchPredicate = queryToMatch.getSelectionPredicate(hierarchyName);

                // Both predicates are segregation predicates (if so, they're identical)
                if (currentPredicate.getSegregation() && matchPredicate.getSegregation()) {
                    match = (position_1 == position_2 ? true : false);
                } else // At least one predicate is not a segregation predicate
                {
                    // The current query's selection predicate is a segregation predicate
                    if (currentPredicate.getSegregation()) {
                        if (position_1 < position_2 && this.isAscendable(hierarchy) ||
                                position_1 > position_2) {
                            match = false;
                        } else {
                            match = true;
                        }
                    } else {
                        if (position_1 == position_2 && currentPredicate.matchesWith(matchPredicate)) {
                            match = true;
                        } else {
                            match = false;
                        }
                    }
                }
            } else // Only one selection predicate among queries
            {
                if (this.containsPredicateOn(hierarchyName) &&
                        this.getSelectionPredicate(hierarchyName).getSegregation()) {
                    if (position_1 < position_2 && this.isAscendable(hierarchy) ||
                            position_1 > position_2) {
                        match = false;
                    } else {
                        match = true;
                    }
                } else {
                    match = false;
                }
            }
        } else // There are no selection predicates
        {
            match = (position_1 == position_2 ? true : false);
        }

        return match;
    }

    /**
     * This method performs a random alteration in the body of a query:
     * group-by set, measures or predicates. The modified query is then returned.
     *
     * @param cube The object containing the cube's information and data.
     * @return The query generated modifying the current one.
     */
    public Query randomEvolution(Cube cube) {
        Query newQuery = clone(this);

        int choice = rand.nextInt(3);

        if (choice == 0) // Group-by change
        {
            newQuery.changeGroupBy(cube);
        } else if (choice == 1) // Selection predicate change
        {
            newQuery.changePredicates(cube);
        } else // Measure change
        {
            newQuery.changeMeasures(cube);
        }

        return newQuery;
    }

    /**
     * This method generates a query identical to the given one (but with a different reference).
     *
     * @param queryToClone The query to replicate.
     * @return The new query, identical to the given one.
     */
    public static Query clone(Query queryToClone) {
        Query newQuery = new Query();

        for (GroupByElement gb : queryToClone.getGroupBySet()) {
            newQuery.addGroupByElement(gb.h, gb.l);
        }

        for (Measure meas : queryToClone.getMeasures()) {
            newQuery.addMeasure(meas);
        }

        for (SelectionPredicate selPred : queryToClone.getPredicates()) {
            newQuery.addSelectionPredicate(selPred);
        }

        return newQuery;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((groupBySet == null) ? 0 : groupBySet.hashCode());
        result = prime * result
                + ((measures == null) ? 0 : measures.hashCode());
        result = prime * result
                + ((predicates == null) ? 0 : predicates.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Query)) {
            return false;
        }

        Query other = (Query) obj;

        if (groupBySet == null) {
            if (other.groupBySet != null) {
                return false;
            }
        } else {
            List<GroupByElement> thisGroupBySet = new ArrayList<GroupByElement>();
            List<GroupByElement> otherGroupBySet = new ArrayList<GroupByElement>();

            // Consider only visible group-by elements
            for (GroupByElement element : groupBySet) {
                if (element.getVisible()) {
                    thisGroupBySet.add(element);
                }
            }
            for (GroupByElement element : other.groupBySet) {
                if (element.getVisible()) {
                    otherGroupBySet.add(element);
                }
            }

            if (!(thisGroupBySet.containsAll(otherGroupBySet) && otherGroupBySet.containsAll(thisGroupBySet))) {
                return false;
            }
        }

        if (measures == null) {
            if (other.measures != null) {
                return false;
            }
        } else if (!(measures.containsAll(other.measures) && other.measures.containsAll(measures))) {
            return false;
        }

        if (predicates == null) {
            if (other.predicates != null) {
                return false;
            }
        } else if (!(predicates.containsAll(other.predicates) && other.predicates.containsAll(predicates))) {
            return false;
        }
        return true;
    }

}