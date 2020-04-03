/**
 * This is the main entry point for interfacing with mondrian "v3", it handles parsing a config file for the schema and DB connection.
 * It's required to have an open DB connection for this to work due to mondrian's design and the  reliance on dimension tables inherent to OLAP cubes.
 * Long term this should be abstracted to a generic data source but this would require an internal schema structure
 */
package com.olap3.cubeexplorer.mondrian;