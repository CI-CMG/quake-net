#!/bin/bash
#
# Retrieve all National Earthquake Information Center (NEIC) Preliminary Determination of Epicenters (PDE) files [QuakeML] from USGS
# Note: If the file already exists at the destination directory, it will not be downloaded again.
# 
# Date created: 2018-03-29
#
# Author: Aaron Sweeney (adapted from Austin Curtis's runMe script documentation)
#

# Define URL of source data:
URL_SRC="ftp://hazards.cr.usgs.gov/NEICPDE/quakeml/"

# Define destination directory on waterlevel:
DEST="/nfs/hazards_ingest/earthquakes/PDEs/data"

echo "wget -A zip -m -p -E -k -K -np -P $DEST $URL_SRC" 
wget -A zip -m -p -E -k -K -np -P $DEST $URL_SRC

