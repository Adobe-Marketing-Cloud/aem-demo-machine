#! /bin/bash
# ----------------------------------------------------------
# A shell script to manage an AEM demo instance snapshots
# Written by: Marcel Boucher
# Last updated on: August/13/2014
# ----------------------------------------------------------

CURRENT_FOLDER=$PWD								# Store current folder.
AEM_HOME=$CURRENT_FOLDER  						# Default to the current folder.
SNAPSHOT_FILE=crx-quickstart-demo-snapshot.tar	# Default name for the snapshot file
CRX_FOLDER=crx-quickstart 						# Default path to crx-quickstart
force=0											# Default to interactive mode.

function USAGE ()
{
    echo ""
    echo "USAGE: "
    echo "    demosnapshot.sh [-cr?] [-p aemhomepath]"
    echo ""
    echo "COMMANDS:"
    echo "    -c  create a new demo snapshot"
    echo "    -r  restore demo snapshot"
    echo "    -f  force mode. WARNING: using this parameter forces deletion of existing snapshots."
    echo "    -?  this usage information"
    echo ""
    echo "OPTIONS:"
    echo "    -p  path to parent folder of cq-quickstart, current folder used if not supplied."
    echo ""
    echo "EXAMPLE:"
    echo "    demosnapshot.sh -c -p $path -l myLabel"
    echo ""
    exit $E_OPTERROR    # Exit and explain usage, if no argument(s) given.
}


if [ $# -eq 0 ]; then
	USAGE; exit 1;
fi	

while getopts ":crfp:l:" OPT; do
	case $OPT in
	      c) action=0;;
	      r) action=1;;
		  f) force=1;;
	      p) last_arg=${!#}; AEM_HOME=${last_arg%/};;
	      *) USAGE >&2; exit 1 ;;
	esac
done

cd $AEM_HOME

# Check if AEM is running.
res=`curl -s --head http://localhost:4502 | head -n 1 | grep -c HTTP/1.1`

if [ $res -eq 1 ]; then
	echo "***********************************************************************************"
	echo "WARNING!!!! Your AEM server is running. Please shut it down and re-run this script."
	echo "***********************************************************************************"
	exit 1
fi

if [[ action -eq 0 ]]; then 	# Create a new snapshot.
	if [ -e $SNAPSHOT_FILE ] && [ $force -eq 0 ]; then
		read -p "Existing AEM Snapshot file found. Are you sure? [y/n]" -n 1
		echo
		if [[ $REPLY =~ ^[Yy]$ ]]; then
		 	echo "Deleting existing snapshot file."
		 	rm $SNAPSHOT_FILE
		else
			exit
		fi
	fi
	if [ -e $SNAPSHOT_FILE ] && [ $force -eq 1 ]; then
			rm $SNAPSHOT_FILE
	fi	
	echo "Creating AEM Snapshot, please be patient..."
	jar -cf $SNAPSHOT_FILE crx-quickstart/
	echo "Demo snapshot created."
	exit 1
fi

if [[ action -eq 1 ]]; then     # Restore an existing snapshot
	if [ -e $SNAPSHOT_FILE ] && [ -e $CRX_FOLDER ]; then
		if [[ $force -eq 0 ]]; then
			read -p "Are you sure you want to restore your AEM demo? [y/n]" -n 1
			echo
			if [[ $REPLY =~ ^[Yy]$ ]]; then
				echo "Deleting current instance."
				rm -Rf $CRX_FOLDER 
				echo "Restoring from snapshot, please be patient."
				cd $AEM_HOME
				tar -xf $SNAPSHOT_FILE
			fi
		else
			rm -Rf $CRX_FOLDER
			tar -xf $SNAPSHOT_FILE
			rm -Rf $AEM_HOME/META-INF #Clean up after the tar extraction.
		fi
		echo "Demo instance restored."
	else
		echo "There does not seem to be a snapshot to restore."
	fi
fi

cd $CURRENT_FOLDER