#!/bin/bash

#File: runMe
#Purpose: Run the parseQuakeML.groovy script over all Files in the Directory.
#Author:Austin Curtis
#Required to update txt files: In the directory you want to execute this scipt you need: This script, parseQuakeML.groovy, any exsisting .txt files already created, the newest .zip you want to add and the Complete directory with Files.txt in it.
#If you are running this from scratch you need: This script, parseQuakeML.groovy and all zip files.
#!!!!Warning!!!! If you run this script with the already exsisting txt files in the same directory it will add to them rather than create new Files.
# To download all zip files use command $ wget -A zip -m -p -E -k -K -np ftp://hazards.cr.usgs.gov/NEICPDE/quakeml/

#Extract function
function extract () {
     if [ -f $1 ] ; then
         case $1 in
             *.tar.bz2)   tar xjf $1        ;;
             *.tar.gz)    tar xzf $1     ;;
             *.bz2)       bunzip2 $1       ;;
             *.rar)       rar x $1     ;;
             *.gz)        gunzip $1     ;;
             *.tar)       tar xf $1        ;;
             *.tbz2)      tar xjf $1      ;;
             *.tgz)       tar xzf $1       ;;
             *.zip)       unzip $1     ;;
             *.Z)         uncompress $1  ;;
             *.7z)        7z x $1    ;;
             *)           echo "'$1' cannot be extracted via extract()" ;;
         esac
     else
         echo "'$1' is not a valid file"
     fi
}

#Unzip, run all files, remove all files, move zip to Complete
for i in *.zip; do
	extract $i
	echo "$i Unzipped!"
	for j in *.quakeml_Verified; do
		groovy parseQuakeML.groovy $j
	done
	echo "*********************************************$i Complete!*********************************************"
	[ -d Complete] || mkdir Complete
	mv -v $i Complete
	rm -v *.quakeml_Verified
	echo "*********************************************Files Moved and Removed*********************************************"
done

#File Files.txt is a list of the .zip files used to create the txt files assosiated with it.

cd Complete
ls >> Files.txt
rm -v *.zip
cd -

echo "*********************************************All .zip Files Have Been Run!*********************************************"

cat << "EOF"

               AAA               LLLLLLLLLLL             LLLLLLLLLLL                                                                                   
              A:::A              L:::::::::L             L:::::::::L                                                                                   
             A:::::A             L:::::::::L             L:::::::::L                                                                                   
            A:::::::A            LL:::::::LL             LL:::::::LL                                                                                   
           A:::::::::A             L:::::L                 L:::::L                                                                                     
          A:::::A:::::A            L:::::L                 L:::::L                                                                                     
         A:::::A A:::::A           L:::::L                 L:::::L                                                                                     
        A:::::A   A:::::A          L:::::L                 L:::::L                                                                                     
       A:::::A     A:::::A         L:::::L                 L:::::L                                                                                     
      A:::::AAAAAAAAA:::::A        L:::::L                 L:::::L                                                                                     
     A:::::::::::::::::::::A       L:::::L                 L:::::L                                                                                     
    A:::::AAAAAAAAAAAAA:::::A      L:::::L         LLLLLL  L:::::L         LLLLLL                                                                      
   A:::::A             A:::::A   LL:::::::LLLLLLLLL:::::LLL:::::::LLLLLLLLL:::::L                                                                      
  A:::::A               A:::::A  L::::::::::::::::::::::LL::::::::::::::::::::::L                                                                      
 A:::::A                 A:::::A L::::::::::::::::::::::LL::::::::::::::::::::::L                                                                      
AAAAAAA                   AAAAAAALLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLL                                                                      
                                                                                                                                                       
                                                                                                                                                       
                                                                                                                                                       
                                                                                                                                                       
                                                                                                                                                       
                                                                                                                                                       
                                                                                                                                                       
                                                                                                                                                       
                                                                                                                                                       
                                                                 DDDDDDDDDDDDD                                                                     !!! 
                                                                 D::::::::::::DDD                                                                 !!:!!
                                                                 D:::::::::::::::DD                                                               !:::!
                                                                 DDD:::::DDDDD:::::D                                                              !:::!
                                                                   D:::::D    D:::::D    ooooooooooo   nnnn  nnnnnnnn        eeeeeeeeeeee         !:::!
                                                                   D:::::D     D:::::D oo:::::::::::oo n:::nn::::::::nn    ee::::::::::::ee       !:::!
                                                                   D:::::D     D:::::Do:::::::::::::::on::::::::::::::nn  e::::::eeeee:::::ee     !:::!
                                                                   D:::::D     D:::::Do:::::ooooo:::::onn:::::::::::::::ne::::::e     e:::::e     !:::!
                                                                   D:::::D     D:::::Do::::o     o::::o  n:::::nnnn:::::ne:::::::eeeee::::::e     !:::!
                                                                   D:::::D     D:::::Do::::o     o::::o  n::::n    n::::ne:::::::::::::::::e      !:::!
                                                                   D:::::D     D:::::Do::::o     o::::o  n::::n    n::::ne::::::eeeeeeeeeee       !!:!!
                                                                   D:::::D    D:::::D o::::o     o::::o  n::::n    n::::ne:::::::e                 !!! 
                                                                 DDD:::::DDDDD:::::D  o:::::ooooo:::::o  n::::n    n::::ne::::::::e                    
                                                                 D:::::::::::::::DD   o:::::::::::::::o  n::::n    n::::n e::::::::eeeeeeee        !!! 
                                                                 D::::::::::::DDD      oo:::::::::::oo   n::::n    n::::n  ee:::::::::::::e       !!:!!
                                                                 DDDDDDDDDDDDD           ooooooooooo     nnnnnn    nnnnnn    eeeeeeeeeeeeee        !!! 

EOF
